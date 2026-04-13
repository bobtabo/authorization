package service

import (
	"authorization-go/internal/model"
	"authorization-go/internal/repository"
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

type ClientService struct {
	repo *repository.ClientRepository
}

func NewClientService(repo *repository.ClientRepository) *ClientService {
	return &ClientService{repo: repo}
}

// AuthenticateByToken はBearerトークンでクライアントを認証します。
func (s *ClientService) AuthenticateByToken(token string) (bool, error) {
	client, err := s.repo.FindByAccessToken(token)
	if err != nil {
		return false, err
	}
	return client != nil, nil
}

func (s *ClientService) FindByCondition(f repository.ClientFilter) ([]*model.Client, error) {
	return s.repo.FindByCondition(f)
}

func (s *ClientService) FindByID(id uint64) (*model.Client, error) {
	client, err := s.repo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if client == nil {
		return nil, apperror.NotFound("client_not_found")
	}
	return client, nil
}

func (s *ClientService) FindByAccessToken(token string) (*model.Client, error) {
	return s.repo.FindByAccessToken(token)
}

func (s *ClientService) FindByIdentifier(identifier string) (*model.Client, error) {
	return s.repo.FindByIdentifier(identifier)
}

type StoreClientInput struct {
	Name       string
	PostCode   string
	Pref       string
	City       string
	Address    string
	Building   string
	Tel        string
	Email      string
	ExecutorID uint
}

// Store はクライアントを新規登録します（RSA鍵ペア・アクセストークンを自動生成）。
func (s *ClientService) Store(in StoreClientInput) (*model.Client, error) {
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
	client := &model.Client{
		Name:        in.Name,
		Identifier:  generateIdentifier(),
		PostCode:    in.PostCode,
		Pref:        in.Pref,
		City:        in.City,
		Address:     in.Address,
		Building:    in.Building,
		Tel:         in.Tel,
		Email:       in.Email,
		AccessToken: accessToken,
		PrivateKey:  string(privPEM),
		PublicKey:   string(pubPEM),
		Fingerprint: fingerprint,
		Status:      1, // Inactive
		CreatedAt:   now,
		UpdatedAt:   now,
		CreatedBy:   &in.ExecutorID,
	}

	return s.repo.Save(client)
}

type UpdateClientInput struct {
	ID         uint64
	Name       *string
	PostCode   *string
	Pref       *string
	City       *string
	Address    *string
	Building   *string
	Tel        *string
	Email      *string
	Status     *int
	ExecutorID uint
}

// Update はクライアントを更新します。
func (s *ClientService) Update(in UpdateClientInput) (*model.Client, error) {
	client, err := s.repo.FindByID(in.ID)
	if err != nil || client == nil {
		return nil, apperror.NotFound("client_not_found")
	}

	if in.Name != nil {
		client.Name = *in.Name
	}
	if in.PostCode != nil {
		client.PostCode = *in.PostCode
	}
	if in.Pref != nil {
		client.Pref = *in.Pref
	}
	if in.City != nil {
		client.City = *in.City
	}
	if in.Address != nil {
		client.Address = *in.Address
	}
	if in.Building != nil {
		client.Building = *in.Building
	}
	if in.Tel != nil {
		client.Tel = *in.Tel
	}
	if in.Email != nil {
		client.Email = *in.Email
	}

	// ステータス遷移
	if in.Status != nil {
		client.Status = *in.Status
		now := time.Now()
		if *in.Status == 2 && client.StartAt == nil { // Active
			client.StartAt = &now
			client.StopAt = nil
		}
		if *in.Status == 3 { // Suspended
			client.StopAt = &now
		}
	}

	now := time.Now()
	client.UpdatedAt = now
	client.UpdatedBy = &in.ExecutorID

	return s.repo.Save(client)
}

// Destroy はクライアントをステータス Closed(4) に更新してから論理削除します。
func (s *ClientService) Destroy(id uint64, executorID uint) error {
	client, err := s.repo.FindByID(id)
	if err != nil || client == nil {
		return apperror.NotFound("client_not_found")
	}

	now := time.Now()
	client.Status = 4 // Closed
	client.UpdatedAt = now
	client.UpdatedBy = &executorID
	if _, err = s.repo.Save(client); err != nil {
		return err
	}

	return s.repo.SoftDelete(id, executorID)
}

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
