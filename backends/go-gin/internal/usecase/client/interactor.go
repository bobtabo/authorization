// Package client はクライアントユースケースを提供します。
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

// NewInteractor は Interactor を生成します。
//
// repo: クライアントリポジトリ
func NewInteractor(repo domclient.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// AuthenticateByToken はBearerトークンでクライアントを認証します。
//
// token: アクセストークン
// 戻り値: 認証成功の場合 true、またはエラー
func (uc *Interactor) AuthenticateByToken(token string) (bool, error) {
	c, err := uc.repo.FindByAccessToken(token)
	if err != nil {
		return false, err
	}
	return c != nil, nil
}

// FindByCondition は検索条件に合致するクライアント一覧の値オブジェクトを返します。
//
// cond: 検索条件
// 戻り値: クライアント一覧 Vo のスライス、またはエラー
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

// FindByID はIDでクライアント詳細の値オブジェクトを返します。
//
// id: クライアントID
// 戻り値: クライアント詳細 Vo、またはエラー
func (uc *Interactor) FindByID(id uint64) (*domclient.DetailVo, error) {
	c, err := uc.repo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if c == nil {
		return nil, apperror.NotFound("client_not_found")
	}
	return clientToDetailVo(c), nil
}

// FindByAccessToken はアクセストークンでクライアントエンティティを返します。
//
// token: アクセストークン
// 戻り値: クライアントエンティティ、またはエラー
func (uc *Interactor) FindByAccessToken(token string) (*domclient.Client, error) {
	return uc.repo.FindByAccessToken(token)
}

// FindByIdentifier はidentifierでクライアントエンティティを返します。
//
// identifier: クライアント識別子
// 戻り値: クライアントエンティティ、またはエラー
func (uc *Interactor) FindByIdentifier(identifier string) (*domclient.Client, error) {
	return uc.repo.FindByIdentifier(identifier)
}

// Store はクライアントを新規登録し、登録結果の値オブジェクトを返します。
// RSA鍵ペア・アクセストークンを自動生成します。
//
// dto: クライアント登録 Dto
// 戻り値: 登録結果 Vo（メール送信・通知配信に使用）、またはエラー
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

// Update はクライアントを更新し、更新後の詳細値オブジェクトを返します。
//
// dto: クライアント更新 Dto
// 戻り値: クライアント詳細 Vo、またはエラー
func (uc *Interactor) Update(dto UpdateDto) (*domclient.DetailVo, error) {
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

// Destroy はクライアントをステータス Closed(4) に更新してから論理削除します。
//
// id: クライアントID
// executorID: 操作者スタッフID
// 戻り値: エラー
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

// ---------- 変換ヘルパー ----------

// clientToListItem はクライアントエンティティを一覧用 Vo に変換します。
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

// clientToDetailVo はクライアントエンティティを詳細用 Vo に変換します。
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

// generateIdentifier はランダムな16進数文字列の識別子を生成します。
func generateIdentifier() string {
	b := make([]byte, 8)
	_, _ = rand.Read(b)
	return hex.EncodeToString(b)
}
