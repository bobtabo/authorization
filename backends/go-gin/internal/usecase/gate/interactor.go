package gate

import (
	domclient "authorization-go/internal/domain/client"
	domgate "authorization-go/internal/domain/gate"
	"authorization-go/internal/config"
	"authorization-go/pkg/apperror"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// Interactor は Gate のユースケースを実装します。
type Interactor struct {
	clientRepo domclient.Repository
	cache      domgate.CacheRepository
	cfg        *config.Config
}

func NewInteractor(
	clientRepo domclient.Repository,
	cache domgate.CacheRepository,
	cfg *config.Config,
) *Interactor {
	return &Interactor{clientRepo: clientRepo, cache: cache, cfg: cfg}
}

// IssueToken はクライアント会員向け JWT を発行します（キャッシュ付き）。
func (uc *Interactor) IssueToken(dto IssueDto) (string, error) {
	c, err := uc.clientRepo.FindByAccessToken(dto.AccessToken)
	if err != nil || c == nil {
		return "", apperror.Unauthorized("client_not_found")
	}

	identifier := c.Identifier
	cached, err := uc.cache.GetJwt(identifier, dto.MemberID)
	if err == nil && cached != "" {
		return cached, nil
	}

	token, err := uc.issueJwt(dto.MemberID, identifier, c.PrivateKey, c.Fingerprint)
	if err != nil {
		return "", err
	}

	_ = uc.cache.PutJwt(identifier, dto.MemberID, token, uc.cfg.JWT.CacheTTL)
	return token, nil
}

// Verify は JWT を検証してペイロードを返します。
func (uc *Interactor) Verify(dto VerifyDto) (map[string]interface{}, error) {
	c, err := uc.clientRepo.FindByIdentifier(dto.Identifier)
	if err != nil || c == nil {
		return nil, apperror.Forbidden("client_not_found")
	}

	return uc.verifyJwt(dto.Identifier, dto.Token, c.PublicKey)
}

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

func parseRSAPrivateKey(pemStr string) (*rsa.PrivateKey, error) {
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, fmt.Errorf("failed to decode PEM")
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}

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
