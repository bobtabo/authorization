// Package client はクライアントユースケースを提供します。
package client

import (
	domclient "authorization-go-beego/internal/domain/client"
	"authorization-go-beego/pkg/apperror"
	"bytes"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/binary"
	"encoding/hex"
	"encoding/pem"
	"fmt"
	"math/big"
	"strings"
	"time"
)

// Interactor はクライアントユースケースの実装です。
type Interactor struct {
	repo domclient.Repository
}

// NewInteractor は Interactor を生成します。
func NewInteractor(repo domclient.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// AuthenticateByToken はアクセストークンでクライアントを認証します。
func (uc *Interactor) AuthenticateByToken(token string) (bool, error) {
	c, err := uc.repo.FindByAccessToken(&domclient.Client{AccessToken: token})
	if err != nil {
		return false, err
	}
	return c != nil, nil
}

// FindByCondition は条件に合うクライアント一覧を取得します。
func (uc *Interactor) FindByCondition(cond domclient.Condition) ([]*domclient.ListItem, error) {
	clients, err := uc.repo.FindByCondition(cond)
	if err != nil {
		return nil, err
	}
	items := make([]*domclient.ListItem, 0, len(clients))
	for _, c := range clients {
		items = append(items, clientToListItem(c))
	}
	return items, nil
}

// FindByID はIDでクライアント詳細を取得します。
func (uc *Interactor) FindByID(dto FindByIDDto) (*domclient.DetailVo, error) {
	c, err := uc.repo.FindByID(&domclient.Client{ID: dto.ID})
	if err != nil {
		return nil, err
	}
	if c == nil {
		return nil, apperror.NotFound("client_not_found")
	}
	return clientToDetailVo(c), nil
}

// Store はクライアントを新規登録します。
func (uc *Interactor) Store(dto StoreDto) (*domclient.StoreVo, error) {
	privateKey, err := rsa.GenerateKey(rand.Reader, 4096)
	if err != nil {
		return nil, fmt.Errorf("rsa key generation: %w", err)
	}

	privPEM := pem.EncodeToMemory(&pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: x509.MarshalPKCS1PrivateKey(privateKey),
	})

	pubDER, err := x509.MarshalPKIXPublicKey(&privateKey.PublicKey)
	if err != nil {
		return nil, err
	}
	pubPEM := pem.EncodeToMemory(&pem.Block{Type: "PUBLIC KEY", Bytes: pubDER})

	fingerprint := rsaFingerprint(&privateKey.PublicKey)

	tokenBytes := make([]byte, 32)
	if _, err = rand.Read(tokenBytes); err != nil {
		return nil, err
	}
	accessToken := hex.EncodeToString(tokenBytes)

	now := time.Now()
	c := &domclient.Client{
		Name:        dto.Name,
		Identifier:  generateIdentifier(),
		PostCode:    dto.PostCode,
		Pref:        dto.Pref,
		City:        dto.City,
		Address:     dto.Address,
		Building:    dto.Building,
		Tel:         dto.Tel,
		Email:       dto.Email,
		AccessToken: accessToken,
		PrivateKey:  string(privPEM),
		PublicKey:   string(pubPEM),
		Fingerprint: fingerprint,
		Status:      domclient.StatusInactive,
		CreatedAt:   now,
		UpdatedAt:   now,
		CreatedBy:   &dto.ExecutorID,
		UpdatedBy:   &dto.ExecutorID,
	}

	saved, err := uc.repo.Save(c)
	if err != nil {
		return nil, err
	}
	return &domclient.StoreVo{
		ID:          saved.ID,
		Name:        saved.Name,
		Email:       saved.Email,
		AccessToken: saved.AccessToken,
	}, nil
}

// Update はクライアント情報を更新します。
func (uc *Interactor) Update(dto UpdateDto) (*domclient.DetailVo, error) {
	c, err := uc.repo.FindByID(&domclient.Client{ID: dto.ID})
	if err != nil || c == nil {
		return nil, apperror.NotFound("client_not_found")
	}

	if dto.Name != nil {
		c.Name = *dto.Name
	}
	if dto.PostCode != nil {
		c.PostCode = *dto.PostCode
	}
	if dto.Pref != nil {
		c.Pref = *dto.Pref
	}
	if dto.City != nil {
		c.City = *dto.City
	}
	if dto.Address != nil {
		c.Address = *dto.Address
	}
	if dto.Building != nil {
		c.Building = *dto.Building
	}
	if dto.Tel != nil {
		c.Tel = *dto.Tel
	}
	if dto.Email != nil {
		c.Email = *dto.Email
	}

	if dto.Status != nil {
		c.Status = *dto.Status
		now := time.Now()
		if *dto.Status == domclient.StatusActive && c.StartAt == nil {
			c.StartAt = &now
			c.StopAt = nil
		}
		if *dto.Status == domclient.StatusSuspended {
			c.StopAt = &now
		}
	}

	now := time.Now()
	c.UpdatedAt = now
	c.UpdatedBy = &dto.ExecutorID

	saved, err := uc.repo.Save(c)
	if err != nil {
		return nil, err
	}
	return clientToDetailVo(saved), nil
}

// Destroy はクライアントを論理削除します。
func (uc *Interactor) Destroy(dto DestroyDto) error {
	c, err := uc.repo.FindByID(&domclient.Client{ID: dto.ID})
	if err != nil || c == nil {
		return apperror.NotFound("client_not_found")
	}

	now := time.Now()
	c.Status = domclient.StatusClosed
	c.UpdatedAt = now
	c.UpdatedBy = &dto.ExecutorID
	saved, err := uc.repo.Save(c)
	if err != nil {
		return err
	}

	return uc.repo.SoftDelete(&domclient.Client{ID: dto.ID, DeletedBy: &dto.ExecutorID, Version: saved.Version})
}

// clientToListItem はクライアントエンティティを一覧用VOに変換します。
func clientToListItem(c *domclient.Client) *domclient.ListItem {
	return &domclient.ListItem{
		ID:        c.ID,
		Name:      c.Name,
		Status:    c.Status,
		StartAt:   c.StartAt,
		StopAt:    c.StopAt,
		CreatedAt: c.CreatedAt,
		UpdatedAt: c.UpdatedAt,
	}
}

// clientToDetailVo はクライアントエンティティを詳細VOに変換します。
func clientToDetailVo(c *domclient.Client) *domclient.DetailVo {
	return &domclient.DetailVo{
		ID:         c.ID,
		Name:       c.Name,
		Identifier: c.Identifier,
		PostCode:   c.PostCode,
		Pref:       c.Pref,
		City:       c.City,
		Address:    c.Address,
		Building:   c.Building,
		Tel:        c.Tel,
		Email:      c.Email,
		Status:     c.Status,
		StartAt:    c.StartAt,
		StopAt:     c.StopAt,
		CreatedAt:  c.CreatedAt,
		UpdatedAt:  c.UpdatedAt,
	}
}

// rsaFingerprint はRSA公開鍵のSHA256フィンガープリントを返します。
func rsaFingerprint(pub *rsa.PublicKey) string {
	buf := new(bytes.Buffer)

	writeBytes := func(b []byte) {
		_ = binary.Write(buf, binary.BigEndian, uint32(len(b)))
		buf.Write(b)
	}

	writeBytes([]byte("ssh-rsa"))
	writeBytes(big.NewInt(int64(pub.E)).Bytes())
	writeBytes(pub.N.Bytes())

	h := sha256.Sum256(buf.Bytes())
	return "SHA256:" + strings.TrimRight(base64.StdEncoding.EncodeToString(h[:]), "=")
}

// generateIdentifier はランダムな16進数識別子を生成します。
func generateIdentifier() string {
	b := make([]byte, 8)
	_, _ = rand.Read(b)
	return hex.EncodeToString(b)
}
