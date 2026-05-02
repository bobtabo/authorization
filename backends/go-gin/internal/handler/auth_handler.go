// Package handler はHTTPリクエストを受け付けるハンドラー実装を提供します。
package handler

import (
	"authorization-go/internal/config"
	domstaff "authorization-go/internal/domain/staff"
	uauth "authorization-go/internal/usecase/auth"
	uinvitation "authorization-go/internal/usecase/invitation"
	"authorization-go/pkg/apperror"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strconv"

	"strings"

	"github.com/gin-gonic/gin"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/github"
	"golang.org/x/oauth2/google"
	"gorm.io/gorm"
)

// AuthHandler は認証関連のHTTPハンドラーを提供します。
type AuthHandler struct {
	db                 *gorm.DB
	newAuthUC          func(*gorm.DB) *uauth.Interactor
	newInviteUC        func(*gorm.DB) *uinvitation.Interactor
	cfg                *config.Config
	oauthConfig        *oauth2.Config
	githubOauthConfig  *oauth2.Config
}

// NewAuthHandler は AuthHandler を生成します。
//
// db: GORM DB インスタンス
// newAuthUC: 認証ユースケースファクトリ
// newInviteUC: 招待ユースケースファクトリ
// cfg: アプリケーション設定
func NewAuthHandler(
	db *gorm.DB,
	newAuthUC func(*gorm.DB) *uauth.Interactor,
	newInviteUC func(*gorm.DB) *uinvitation.Interactor,
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
	githubCfg := &oauth2.Config{
		ClientID:     cfg.OAuth.GithubClientID,
		ClientSecret: cfg.OAuth.GithubClientSecret,
		RedirectURL:  cfg.OAuth.GithubRedirectURL,
		Scopes:       []string{"user:email"},
		Endpoint:     github.Endpoint,
	}
	return &AuthHandler{
		db:                db,
		newAuthUC:         newAuthUC,
		newInviteUC:       newInviteUC,
		cfg:               cfg,
		oauthConfig:       oauthCfg,
		githubOauthConfig: githubCfg,
	}
}

// GetMyProfile は認証済みスタッフのプロフィールを返します。
// GET /api/auth/me
func (h *AuthHandler) GetMyProfile(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.newAuthUC(h.db).FindUser(staffID)
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

// Login は認証済みスタッフのプロフィールを返します。
// GET /api/auth/login
func (h *AuthHandler) Login(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.newAuthUC(h.db).FindUser(staffID)
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

// Logout はセッションクッキーを削除してログアウトします。
// GET /api/auth/logout
func (h *AuthHandler) Logout(c *gin.Context) {
	secure := h.cfg.App.Env == "production"
	c.SetCookie("staff_id", "", -1, "/", "", secure, true)
	c.JSON(http.StatusOK, gin.H{})
}

// Invitation はトークンで招待情報を返します。
// GET /api/auth/invitation/:token
func (h *AuthHandler) Invitation(c *gin.Context) {
	token := c.Param("token")
	result, err := h.newInviteUC(h.db).FindByToken(uinvitation.FindByTokenDto{Token: token})
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

// GoogleRedirect は Google OAuth 認証ページへリダイレクトします。
// GET /auth/google/redirect
func (h *AuthHandler) GoogleRedirect(c *gin.Context) {
	oauthState := c.Query("token")
	if oauthState == "" {
		oauthState = "state"
	}
	url := h.oauthConfig.AuthCodeURL(oauthState, oauth2.AccessTypeOnline)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

// GoogleCallback は Google OAuth コールバックを処理し、スタッフを作成または更新してセッションを発行します。
// GET /auth/google/callback
func (h *AuthHandler) GoogleCallback(c *gin.Context) {
	code := c.Query("code")
	if code == "" {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}
	stateVal := c.Query("state")
	invitationToken := ""
	if stateVal != "" && stateVal != "state" {
		invitationToken = stateVal
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

	dto := uauth.LoginDto{
		Provider:        1, // Google
		ProviderID:      userInfo["id"],
		Name:            userInfo["name"],
		Email:           userInfo["email"],
		Avatar:          avatar,
		InvitationToken: invitationToken,
	}

	var staff *domstaff.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		staff, e = h.newAuthUC(tx).Login(dto)
		return e
	}); txErr != nil {
		var appErr *apperror.AppError
		if errors.As(txErr, &appErr) && appErr.Code == http.StatusForbidden {
			c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=403")
			return
		}
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	secure := h.cfg.App.Env == "production"
	maxAge := h.cfg.App.StaffCookieLifetime * 60
	c.SetCookie("staff_id", strconv.Itoa(int(staff.ID)), maxAge, "/", "", secure, true)
	c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/clients")
}

// GithubRedirect は GitHub OAuth 認証ページへリダイレクトします。
// GET /auth/github/redirect
func (h *AuthHandler) GithubRedirect(c *gin.Context) {
	token := c.Query("token")
	state := h.cfg.OAuth.Runtime
	if token != "" {
		state = h.cfg.OAuth.Runtime + "|" + token
	}
	url := h.githubOauthConfig.AuthCodeURL(state, oauth2.AccessTypeOnline)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

// GithubCallback は GitHub OAuth コールバックを処理し、スタッフを作成または更新してセッションを発行します。
// GET /auth/github/callback
func (h *AuthHandler) GithubCallback(c *gin.Context) {
	code := c.Query("code")
	if code == "" {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}
	stateVal := c.Query("state")
	invitationToken := ""
	parts := strings.SplitN(stateVal, "|", 2)
	if len(parts) == 2 {
		invitationToken = parts[1]
	}

	oauthToken, err := h.githubOauthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	userInfo, err := fetchGithubUserInfo(h.githubOauthConfig, oauthToken)
	if err != nil {
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	var avatar *string
	if pic := userInfo["avatar"]; pic != "" {
		avatar = &pic
	}

	dto := uauth.LoginDto{
		Provider:        domstaff.ProviderGithub,
		ProviderID:      userInfo["id"],
		Name:            userInfo["name"],
		Email:           userInfo["email"],
		Avatar:          avatar,
		InvitationToken: invitationToken,
	}

	var staff *domstaff.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		staff, e = h.newAuthUC(tx).Login(dto)
		return e
	}); txErr != nil {
		var appErr *apperror.AppError
		if errors.As(txErr, &appErr) && appErr.Code == http.StatusForbidden {
			c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=403")
			return
		}
		c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/error?code=500")
		return
	}

	secure := h.cfg.App.Env == "production"
	maxAge := h.cfg.App.StaffCookieLifetime * 60
	c.SetCookie("staff_id", strconv.Itoa(int(staff.ID)), maxAge, "/", "", secure, true)
	c.Redirect(http.StatusTemporaryRedirect, h.cfg.App.FrontendURL+"/clients")
}

// ---------- プライベートヘルパー ----------

// fetchGoogleUserInfo は Google OAuth トークンからユーザー情報を取得します。
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

// fetchGithubUserInfo は GitHub OAuth トークンからユーザー情報を取得します。
func fetchGithubUserInfo(cfg *oauth2.Config, token *oauth2.Token) (map[string]string, error) {
	client := cfg.Client(context.Background(), token)

	// ユーザー基本情報
	resp, err := client.Get("https://api.github.com/user")
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

	id := fmt.Sprintf("%v", raw["id"])

	name := ""
	if v, ok := raw["name"]; ok && v != nil && fmt.Sprintf("%v", v) != "" && fmt.Sprintf("%v", v) != "<nil>" {
		name = fmt.Sprintf("%v", v)
	}
	if name == "" {
		if v, ok := raw["login"]; ok && v != nil {
			name = fmt.Sprintf("%v", v)
		}
	}

	avatar := ""
	if v, ok := raw["avatar_url"]; ok && v != nil {
		avatar = fmt.Sprintf("%v", v)
	}

	// メールアドレス（/user/emails から primary:true を取得）
	email := ""
	emailResp, err := client.Get("https://api.github.com/user/emails")
	if err == nil {
		defer emailResp.Body.Close()
		emailBody, err := io.ReadAll(emailResp.Body)
		if err == nil {
			var emails []map[string]interface{}
			if err = json.Unmarshal(emailBody, &emails); err == nil {
				for _, e := range emails {
					if primary, ok := e["primary"]; ok {
						if b, ok := primary.(bool); ok && b {
							if v, ok := e["email"]; ok && v != nil {
								email = fmt.Sprintf("%v", v)
							}
							break
						}
					}
				}
			}
		}
	}

	return map[string]string{
		"id":     id,
		"name":   name,
		"email":  email,
		"avatar": avatar,
	}, nil
}
