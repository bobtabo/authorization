package client

import (
	domclient "authorization-go/internal/domain/client"
	"authorization-go/pkg/apperror"
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

// Interactor はクライアントのユースケースを実装します。
type Interactor struct {
	repo domclient.Repository
}

func NewInteractor(repo domclient.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// AuthenticateByToken はBearerトークンでクライアントを認証します。
func (uc *Interactor) AuthenticateByToken(token string) (bool, error) {
	c, err := uc.repo.FindByAccessToken(token)
	if err != nil {
		return false, err
	}
	return c != nil, nil
}

// FindByCondition は検索条件に合致するクライアント一覧を返します。
func (uc *Interactor) FindByCondition(cond domclient.Condition) ([]*domclient.Client, error) {
	return uc.repo.FindByCondition(cond)
}

// FindByID は ID でクライアントを取得します。
func (uc *Interactor) FindByID(id uint64) (*domclient.Client, error) {
	c, err := uc.repo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if c == nil {
		return nil, apperror.NotFound("client_not_found")
	}
	return c, nil
}

// FindByAccessToken はアクセストークンでクライアントを取得します。
func (uc *Interactor) FindByAccessToken(token string) (*domclient.Client, error) {
	return uc.repo.FindByAccessToken(token)
}

// FindByIdentifier は identifier でクライアントを取得します。
func (uc *Interactor) FindByIdentifier(identifier string) (*domclient.Client, error) {
	return uc.repo.FindByIdentifier(identifier)
}

// Store はクライアントを新規登録します（RSA鍵ペア・アクセストークンを自動生成）。
func (uc *Interactor) Store(dto StoreDto) (*domclient.Client, error) {
	// RSA 4096bit 鍵ペアを生成
	privateKey, err := rsa.GenerateKey(rand.Reader, 4096)
	if err != nil {
		return nil, fmt.Errorf("rsa key generation: %w", err)
	}

	// 秘密鍵 PEM
	privPEM := pem.EncodeToMemory(&pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: x509.MarshalPKCS1PrivateKey(privateKey),
	})

	// 公開鍵 PEM
	pubDER, err := x509.MarshalPKIXPublicKey(&privateKey.PublicKey)
	if err != nil {
		return nil, err
	}
	pubPEM := pem.EncodeToMemory(&pem.Block{Type: "PUBLIC KEY", Bytes: pubDER})

	// フィンガープリント（SSH wire format SHA256）
	fingerprint := rsaFingerprint(&privateKey.PublicKey)

	// アクセストークン（64バイト hex）
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
	}

	return uc.repo.Save(c)
}

// Update はクライアントを更新します。
func (uc *Interactor) Update(dto UpdateDto) (*domclient.Client, error) {
	c, err := uc.repo.FindByID(dto.ID)
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

	// ステータス遷移
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

	return uc.repo.Save(c)
}

// Destroy はクライアントをステータス Closed(4) に更新してから論理削除します。
func (uc *Interactor) Destroy(id uint64, executorID uint) error {
	c, err := uc.repo.FindByID(id)
	if err != nil || c == nil {
		return apperror.NotFound("client_not_found")
	}

	now := time.Now()
	c.Status = domclient.StatusClosed
	c.UpdatedAt = now
	c.UpdatedBy = &executorID
	if _, err = uc.repo.Save(c); err != nil {
		return err
	}

	return uc.repo.SoftDelete(id, executorID)
}

// ---------- プライベートヘルパー ----------

// rsaFingerprint は PHP と同一方式で SSH wire format SHA256 フィンガープリントを生成します。
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

func generateIdentifier() string {
	b := make([]byte, 8)
	_, _ = rand.Read(b)
	return hex.EncodeToString(b)
}
