// Package gate はゲートウェイ認証ユースケースを提供します。
package gate

import (
	domclient "authorization-go-beego/internal/domain/client"
	domgate "authorization-go-beego/internal/domain/gate"
	"authorization-go-beego/internal/config"
	"authorization-go-beego/pkg/apperror"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// Interactor はゲートウェイ認証ユースケースの実装です。
type Interactor struct {
	clientRepo  domclient.Repository
	historyRepo domclient.JwtHistoryRepository
	cache       domgate.CacheRepository
	cfg         *config.Config
}

// NewInteractor は Interactor を生成します。
func NewInteractor(
	clientRepo domclient.Repository,
	historyRepo domclient.JwtHistoryRepository,
	cache domgate.CacheRepository,
	cfg *config.Config,
) *Interactor {
	return &Interactor{clientRepo: clientRepo, historyRepo: historyRepo, cache: cache, cfg: cfg}
}

// IssueToken はアクセストークンを検証してJWTを発行します。
func (uc *Interactor) IssueToken(dto IssueDto) (*domgate.IssueVo, error) {
	c, err := uc.clientRepo.FindByAccessToken(&domclient.Client{AccessToken: dto.AccessToken})
	if err != nil || c == nil {
		return nil, apperror.Unauthorized("client_not_found")
	}

	identifier := c.Identifier
	cached, err := uc.cache.GetJwt(identifier, dto.MemberID)
	if err == nil && cached != "" {
		return &domgate.IssueVo{Token: cached}, nil
	}

	token, err := uc.issueJwt(dto.MemberID, identifier, c.PrivateKey, c.Fingerprint)
	if err != nil {
		return nil, err
	}

	_ = uc.cache.PutJwt(identifier, dto.MemberID, token, uc.cfg.JWT.CacheTTL)
	_ = uc.historyRepo.Save(c.ID, dto.MemberID, time.Now(), token)
	return &domgate.IssueVo{Token: token}, nil
}

// Verify はJWTトークンを検証してクレームを返します。
func (uc *Interactor) Verify(dto VerifyDto) (*domgate.VerifyVo, error) {
	c, err := uc.clientRepo.FindByIdentifier(&domclient.Client{Identifier: dto.Identifier})
	if err != nil || c == nil {
		return nil, apperror.Forbidden("client_not_found")
	}

	claims, err := uc.verifyJwt(dto.Identifier, dto.Token, c.PublicKey)
	if err != nil {
		return nil, err
	}
	return &domgate.VerifyVo{Claims: claims}, nil
}

// issueJwt はRSA秘密鍵でJWTを署名して返します。
func (uc *Interactor) issueJwt(memberID, identifier, privateKeyPEM, fingerprint string) (string, error) {
	privKey, err := parseRSAPrivateKey(privateKeyPEM)
	if err != nil {
		return "", fmt.Errorf("parse private key: %w", err)
	}

	now := time.Now()
	claims := jwt.MapClaims{
		"iss": uc.cfg.JWT.Issuer,
		"sub": memberID,
		"aud": []string{identifier},
		"exp": now.Add(time.Duration(uc.cfg.JWT.TTL) * time.Second).Unix(),
		"iat": now.Unix(),
		"nbf": now.Unix(),
		"jti": uuid.New().String(),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = fingerprint
	return token.SignedString(privKey)
}

// verifyJwt はRSA公開鍵でJWTを検証してクレームを返します。
func (uc *Interactor) verifyJwt(identifier, tokenStr, publicKeyPEM string) (map[string]interface{}, error) {
	pubKey, err := parseRSAPublicKey(publicKeyPEM)
	if err != nil {
		return nil, apperror.Unauthorized("jwt_invalid")
	}

	token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, fmt.Errorf("unexpected signing method")
		}
		return pubKey, nil
	}, jwt.WithAudience(identifier))

	if err != nil || !token.Valid {
		return nil, apperror.Unauthorized("jwt_invalid")
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		return nil, apperror.Unauthorized("jwt_invalid")
	}

	iss, _ := claims["iss"].(string)
	if iss != uc.cfg.JWT.Issuer {
		return nil, apperror.Unauthorized("jwt_invalid")
	}

	return map[string]interface{}(claims), nil
}

// parseRSAPrivateKey はPEM文字列からRSA秘密鍵を解析します。
func parseRSAPrivateKey(pemStr string) (*rsa.PrivateKey, error) {
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, fmt.Errorf("failed to decode PEM")
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}

// parseRSAPublicKey はPEM文字列からRSA公開鍵を解析します。
func parseRSAPublicKey(pemStr string) (*rsa.PublicKey, error) {
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, fmt.Errorf("failed to decode PEM")
	}
	pub, err := x509.ParsePKIXPublicKey(block.Bytes)
	if err != nil {
		return nil, err
	}
	rsaPub, ok := pub.(*rsa.PublicKey)
	if !ok {
		return nil, fmt.Errorf("not RSA public key")
	}
	return rsaPub, nil
}
