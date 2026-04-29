// Package handler はHTTPハンドラを提供します。
package handler

import (
	"authorization-go-beego/internal/config"
	domstaff "authorization-go-beego/internal/domain/staff"
	uauth "authorization-go-beego/internal/usecase/auth"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	"authorization-go-beego/pkg/apperror"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"gorm.io/gorm"
)

// AuthHandler は認証関連のHTTPハンドラです。
type AuthHandler struct {
	db          *gorm.DB
	newAuthUC   func(*gorm.DB) *uauth.Interactor
	newInviteUC func(*gorm.DB) *uinvitation.Interactor
	cfg         *config.Config
	oauthConfig *oauth2.Config
}

// NewAuthHandler は AuthHandler を生成します。
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
	return &AuthHandler{
		db:          db,
		newAuthUC:   newAuthUC,
		newInviteUC: newInviteUC,
		cfg:         cfg,
		oauthConfig: oauthCfg,
	}
}

// GetMyProfile はCookieからスタッフIDを取得してプロフィールを返します。
func (h *AuthHandler) GetMyProfile(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.newAuthUC(h.db).FindUser(uauth.FindUserDto{ID: staffID})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"staff_id": staff.ID,
		"name":     staff.Name,
		"avatar":   staff.Avatar,
		"role":     staff.Role,
	})
}

// Login はCookieのスタッフIDを検証してログイン状態を返します。
func (h *AuthHandler) Login(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}
	staff, err := h.newAuthUC(h.db).FindUser(uauth.FindUserDto{ID: staffID})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"staff_id": staff.ID,
		"name":     staff.Name,
		"avatar":   staff.Avatar,
		"role":     staff.Role,
	})
}

// Logout はスタッフCookieを削除してログアウトします。
func (h *AuthHandler) Logout(ctx *beecontext.Context) {
	secure := h.cfg.App.Env == "production"
	clearStaffCookie(ctx, secure)
	writeJSON(ctx, http.StatusOK, map[string]interface{}{})
}

// Invitation は招待トークンを検証してキャッシュに保存します。
func (h *AuthHandler) Invitation(ctx *beecontext.Context) {
	token := ctx.Input.Param(":token")
	result, err := h.newInviteUC(h.db).FindByToken(uinvitation.FindByTokenDto{Token: token})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"found":       true,
		"url":         result.URL,
		"display_url": result.DisplayURL,
		"token":       result.Token,
	})
}

// GoogleRedirect はGoogle OAuth認証ページへリダイレクトします。
func (h *AuthHandler) GoogleRedirect(ctx *beecontext.Context) {
	oauthState := ctx.Input.Query("token")
	if oauthState == "" {
		oauthState = "state"
	}
	url := h.oauthConfig.AuthCodeURL(oauthState, oauth2.AccessTypeOnline)
	http.Redirect(ctx.ResponseWriter, ctx.Request, url, http.StatusTemporaryRedirect)
}

// GoogleCallback はGoogle OAuthコールバックを受けてログイン処理を行います。
func (h *AuthHandler) GoogleCallback(ctx *beecontext.Context) {
	code := ctx.Input.Query("code")
	if code == "" {
		http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/error?code=500", http.StatusTemporaryRedirect)
		return
	}
	stateVal := ctx.Input.Query("state")
	invitationToken := ""
	if stateVal != "" && stateVal != "state" {
		invitationToken = stateVal
	}

	oauthToken, err := h.oauthConfig.Exchange(context.Background(), code)
	if err != nil {
		http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/error?code=500", http.StatusTemporaryRedirect)
		return
	}

	userInfo, err := fetchGoogleUserInfo(h.oauthConfig, oauthToken)
	if err != nil {
		http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/error?code=500", http.StatusTemporaryRedirect)
		return
	}

	var avatar *string
	if pic := userInfo["picture"]; pic != "" {
		avatar = &pic
	}

	dto := uauth.LoginDto{
		Provider:        1,
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
			http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/error?code=403", http.StatusTemporaryRedirect)
			return
		}
		http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/error?code=500", http.StatusTemporaryRedirect)
		return
	}

	secure := h.cfg.App.Env == "production"
	maxAge := h.cfg.App.StaffCookieLifetime * 60
	setStaffCookie(ctx, staff.ID, maxAge, secure)
	http.Redirect(ctx.ResponseWriter, ctx.Request, h.cfg.App.FrontendURL+"/clients", http.StatusTemporaryRedirect)
}

// fetchGoogleUserInfo はGoogle APIからユーザー情報を取得します。
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
