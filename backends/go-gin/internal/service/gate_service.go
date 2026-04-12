package service

import (
	"authorization-go/internal/config"
	"authorization-go/internal/repository"
	"authorization-go/pkg/apperror"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

type GateService struct {
	clientRepo *repository.ClientRepository
	cache      *repository.GateCacheRepository
	cfg        *config.Config
}

func NewGateService(
	clientRepo *repository.ClientRepository,
	cache *repository.GateCacheRepository,
	cfg *config.Config,
) *GateService {
	return &GateService{clientRepo: clientRepo, cache: cache, cfg: cfg}
}

// IssueToken はクライアント会員向け JWT を発行します（キャッシュ付き）。
func (s *GateService) IssueToken(accessToken, memberID string) (string, error) {
	client, err := s.clientRepo.FindByAccessToken(accessToken)
	if err != nil || client == nil {
		return "", apperror.Unauthorized("client_not_found")
	}

	identifier := client.Identifier
	cached, err := s.cache.GetJwt(identifier, memberID)
	if err == nil && cached != "" {
		return cached, nil
	}

	token, err := s.issueJwt(memberID, identifier, client.PrivateKey, client.Fingerprint)
	if err != nil {
		return "", err
	}

	_ = s.cache.PutJwt(identifier, memberID, token, s.cfg.JWT.CacheTTL)
	return token, nil
}

// Verify は JWT を検証してペイロードを返します。
func (s *GateService) Verify(identifier, tokenStr string) (map[string]interface{}, error) {
	client, err := s.clientRepo.FindByIdentifier(identifier)
	if err != nil || client == nil {
		return nil, apperror.Forbidden("client_not_found")
	}

	return s.verifyJwt(identifier, tokenStr, client.PublicKey)
}

func (s *GateService) issueJwt(memberID, identifier, privateKeyPEM, fingerprint string) (string, error) {
	privKey, err := parseRSAPrivateKey(privateKeyPEM)
	if err != nil {
		return "", fmt.Errorf("parse private key: %w", err)
	}

	now := time.Now()
	claims := jwt.MapClaims{
		"iss": s.cfg.JWT.Issuer,
		"sub": memberID,
		"aud": []string{identifier},
		"exp": now.Add(time.Duration(s.cfg.JWT.TTL) * time.Second).Unix(),
		"iat": now.Unix(),
		"nbf": now.Unix(),
		"jti": uuid.New().String(),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = fingerprint
	return token.SignedString(privKey)
}

func (s *GateService) verifyJwt(identifier, tokenStr, publicKeyPEM string) (map[string]interface{}, error) {
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
	if iss != s.cfg.JWT.Issuer {
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
