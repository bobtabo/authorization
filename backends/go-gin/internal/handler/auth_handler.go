package handler

import (
	"authorization-go/internal/config"
	"authorization-go/internal/service"
	"authorization-go/pkg/apperror"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
)

type AuthHandler struct {
	authSvc       *service.AuthService
	invitationSvc *service.InvitationService
	cfg           *config.Config
	oauthConfig   *oauth2.Config
}

func NewAuthHandler(
	authSvc *service.AuthService,
	invitationSvc *service.InvitationService,
	cfg *config.Config,
) *AuthHandler {
	oauthCfg := &oauth2.Config{
		ClientID:     cfg.OAuth.GoogleClientID,
		ClientSecret: cfg.OAuth.GoogleClientSecret,
		RedirectURL:  cfg.OAuth.GoogleRedirectURL,
		Scopes: []string{
			"https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile",
		},
		Endpoint: google.Endpoint,
	}
	return &AuthHandler{
		authSvc:       authSvc,
		invitationSvc: invitationSvc,
		cfg:           cfg,
		oauthConfig:   oauthCfg,
	}
}

// GET /api/auth/me
func (h *AuthHandler) GetMyProfile(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.authSvc.FindUser(staffID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"staff_id": staff.ID,
		"name":     staff.Name,
		"avatar":   staff.Avatar,
		"role":     staff.Role,
	})
}

// GET /api/auth/login
func (h *AuthHandler) Login(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.authSvc.FindUser(staffID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"staff_id": staff.ID,
		"name":     staff.Name,
		"avatar":   staff.Avatar,
		"role":     staff.Role,
	})
}

// GET /api/auth/logout
func (h *AuthHandler) Logout(c *gin.Context) {
	secure := h.cfg.App.Env == "production"
	c.SetCookie("staff_id", "", -1, "/", "", secure, true)
	c.JSON(http.StatusOK, gin.H{})
}

// GET /api/auth/invitation/:token
func (h *AuthHandler) Invitation(c *gin.Context) {
	token := c.Param("token")
	result, err := h.invitationSvc.FindByToken(token)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"found":       true,
		"url":         result.URL,
		"display_url": result.DisplayURL,
		"token":       result.Token,
	})
}

// GET /api/auth/google/redirect
func (h *AuthHandler) GoogleRedirect(c *gin.Context) {
	url := h.oauthConfig.AuthCodeURL("state", oauth2.AccessTypeOnline)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

// GET /api/auth/google/callback
func (h *AuthHandler) GoogleCallback(c *gin.Context) {
	code := c.Query("code")
	if code == "" {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	oauthToken, err := h.oauthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	userInfo, err := fetchGoogleUserInfo(h.oauthConfig, oauthToken)
	if err != nil {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	var avatar *string
	if pic := userInfo["picture"]; pic != "" {
		avatar = &pic
	}

	staff, err := h.authSvc.Login(1 /* Google */, userInfo["id"], userInfo["name"], userInfo["email"], avatar)
	if err != nil {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	secure := h.cfg.App.Env == "production"
	maxAge := h.cfg.App.StaffCookieLifetime * 60
	c.SetCookie("staff_id", strconv.Itoa(int(staff.ID)), maxAge, "/", "", secure, true)
	c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/clients")
}

func fetchGoogleUserInfo(cfg *oauth2.Config, token *oauth2.Token) (map[string]string, error) {
	client := cfg.Client(context.Background(), token)
	resp, err := client.Get("https://www.googleapis.com/oauth2/v2/userinfo")
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var raw map[string]interface{}
	if err = json.Unmarshal(body, &raw); err != nil {
		return nil, err
	}

	str := func(key string) string {
		if v, ok := raw[key]; ok {
			return fmt.Sprintf("%v", v)
		}
		return ""
	}

	return map[string]string{
		"id":      str("id"),
		"name":    str("name"),
		"email":   str("email"),
		"picture": str("picture"),
	}, nil
}
