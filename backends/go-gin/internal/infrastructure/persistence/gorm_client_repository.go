package persistence

import (
	domclient "authorization-go/internal/domain/client"
	"authorization-go/internal/infrastructure/model"
	"authorization-go/internal/support"
	"authorization-go/pkg/apperror"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormClientRepository は domain/client.Repository の GORM 実装です。
type GormClientRepository struct {
	db *gorm.DB
}

// NewGormClientRepository は GormClientRepository を生成します。
//
// db: GORM DB インスタンス
func NewGormClientRepository(db *gorm.DB) *GormClientRepository {
	return &GormClientRepository{db: db}
}

// applyFilters は共通のフィルタ条件をクエリに適用します。
func (r *GormClientRepository) applyFilters(q *gorm.DB, cond domclient.Condition) *gorm.DB {
	if cond.Keyword != nil && *cond.Keyword != "" {
		q = q.Where("name LIKE ?", "%"+*cond.Keyword+"%")
	}
	if cond.StartFrom != nil {
		q = q.Where("start_at >= ?", cond.StartFrom)
	}
	if cond.StartTo != nil {
		q = q.Where("start_at <= ?", cond.StartTo)
	}
	if len(cond.Statuses) > 0 {
		q = q.Where("status IN ?", cond.Statuses)
	}
	return q
}

// FindByCondition は検索条件に合致するクライアントエンティティを返します。
func (r *GormClientRepository) FindByCondition(cond domclient.Condition) ([]*domclient.Client, error) {
	q := r.db.Unscoped()
	q = r.applyFilters(q, cond)

	if cond.Sort != "" {
		dir := "ASC"
		if cond.SortType == "desc" {
			dir = "DESC"
		}
		q = q.Order(cond.Sort + " " + dir)
	} else {
		q = q.Order("id ASC")
	}

	if cond.Limit > 0 {
		q = q.Limit(cond.Limit).Offset(cond.Offset)
	}

	var ms []*model.Client
	if err := q.Find(&ms).Error; err != nil {
		return nil, err
	}
	out := make([]*domclient.Client, 0, len(ms))
	for _, m := range ms {
		out = append(out, clientToDomain(m))
	}
	return out, nil
}

// CountByCondition は検索条件に合致するクライアントの総件数を返します。
func (r *GormClientRepository) CountByCondition(cond domclient.Condition) (int, error) {
	q := r.db.Unscoped().Model(&model.Client{})
	q = r.applyFilters(q, cond)
	var count int64
	if err := q.Count(&count).Error; err != nil {
		return 0, err
	}
	return int(count), nil
}

// FindByID はIDでクライアントエンティティを返します。存在しない場合は nil を返します。
func (r *GormClientRepository) FindByID(id uint64) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Unscoped().First(&m, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// FindByAccessToken はアクセストークンでアクティブなクライアントエンティティを返します。
func (r *GormClientRepository) FindByAccessToken(token string) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("access_token = ? AND status = ?", token, domclient.StatusActive).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// FindByIdentifier は識別子でクライアントエンティティを返します。
func (r *GormClientRepository) FindByIdentifier(identifier string) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("identifier = ?", identifier).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// Save はクライアントエンティティを保存（新規作成または更新）して返します。
// 既存レコードの更新時はバージョン条件を付けた原子的 UPDATE を行い、
// 競合（RowsAffected == 0）の場合は楽観排他エラーを返します。
func (r *GormClientRepository) Save(c *domclient.Client) (*domclient.Client, error) {
	if c.ID == 0 {
		// 新規作成
		m := clientToModel(c)
		if err := r.db.Create(m).Error; err != nil {
			return nil, err
		}
		return clientToDomain(m), nil
	}

	// 更新：version 条件付き UPDATE
	originalVersion := c.Version
	now := time.Now()
	updates := map[string]interface{}{
		"name":         c.Name,
		"identifier":   c.Identifier,
		"post_code":    c.PostCode,
		"pref":         c.Pref,
		"city":         c.City,
		"address":      c.Address,
		"building":     c.Building,
		"tel":          c.Tel,
		"email":        c.Email,
		"access_token": c.AccessToken,
		"private_key":  c.PrivateKey,
		"public_key":   c.PublicKey,
		"fingerprint":  c.Fingerprint,
		"status":       c.Status,
		"start_at":     c.StartAt,
		"stop_at":      c.StopAt,
		"updated_at":   now,
		"updated_by":   c.UpdatedBy,
		"version":      gorm.Expr("version + 1"),
	}
	result := r.db.Unscoped().Model(&model.Client{}).
		Where("id = ? AND version = ?", c.ID, originalVersion).
		Updates(updates)
	if result.Error != nil {
		return nil, result.Error
	}
	if result.RowsAffected == 0 {
		return nil, apperror.Conflict("optimistic_lock")
	}
	c.Version = originalVersion + 1
	c.UpdatedAt = now
	return c, nil
}

// SoftDelete はクライアントを論理削除します。
// version が DB と一致しない場合は楽観排他エラーを返します。
func (r *GormClientRepository) SoftDelete(id uint64, deletedBy uint, version int) error {
	now := time.Now()
	result := r.db.Unscoped().Model(&model.Client{}).
		Where("id = ? AND version = ?", id, version).
		Updates(map[string]interface{}{
			"deleted_at": now,
			"deleted_by": deletedBy,
			"version":    gorm.Expr("version + 1"),
		})
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		return apperror.Conflict("optimistic_lock")
	}
	return nil
}

// ---------- マッピングヘルパー ----------

func clientToDomain(m *model.Client) *domclient.Client {
	c := &domclient.Client{}
	support.Assign(c, m)
	return c
}

func clientToModel(c *domclient.Client) *model.Client {
	m := &model.Client{}
	support.Assign(m, c)
	return m
}
