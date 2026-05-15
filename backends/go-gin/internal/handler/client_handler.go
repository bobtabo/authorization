package handler

import (
	domclient "authorization-go/internal/domain/client"
	"authorization-go/internal/infrastructure/mail"
	uclient "authorization-go/internal/usecase/client"
	unotification "authorization-go/internal/usecase/notification"
	"authorization-go/pkg/apperror"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// ClientHandler はクライアント関連のHTTPハンドラーを提供します。
type ClientHandler struct {
	db           *gorm.DB
	newClientUC  func(*gorm.DB) *uclient.Interactor
	newNotifUC   func(*gorm.DB) *unotification.Interactor
	mailer       *mail.Mailer
}

// NewClientHandler は ClientHandler を生成します。
//
// db: GORM DB インスタンス
// newClientUC: クライアントユースケースファクトリ
// newNotifUC: 通知ユースケースファクトリ
// mailer: メール送信サービス
func NewClientHandler(
	db *gorm.DB,
	newClientUC func(*gorm.DB) *uclient.Interactor,
	newNotifUC func(*gorm.DB) *unotification.Interactor,
	mailer *mail.Mailer,
) *ClientHandler {
	return &ClientHandler{
		db:          db,
		newClientUC: newClientUC,
		newNotifUC:  newNotifUC,
		mailer:      mailer,
	}
}

// Index は検索条件に合致するクライアント一覧を返します。
// GET /api/clients
func (h *ClientHandler) Index(c *gin.Context) {
	cond := domclient.Condition{}

	if kw := c.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	if v := c.Query("start_from"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartFrom = &t
		}
	}
	if v := c.Query("start_to"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartTo = &t
		}
	}

	clients, err := h.newClientUC(h.db).FindByCondition(cond)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapClientList(clients))
}

// Show はIDでクライアント詳細を返します。
// GET /api/clients/:id
func (h *ClientHandler) Show(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	client, err := h.newClientUC(h.db).FindByID(id)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapClientDetail(client))
}

// Store はクライアントを新規登録します。
// POST /api/clients/store
func (h *ClientHandler) Store(c *gin.Context) {
	var body struct {
		Name     string `json:"name"     binding:"required"`
		PostCode string `json:"post_code"`
		Pref     string `json:"pref"`
		City     string `json:"city"`
		Address  string `json:"address"`
		Building string `json:"building"`
		Tel      string `json:"tel"`
		Email    string `json:"email"`
	}
	if err := c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(c)

	var storeVo *domclient.StoreVo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		storeVo, e = h.newClientUC(tx).Store(uclient.StoreDto{
			Name:       body.Name,
			PostCode:   body.PostCode,
			Pref:       body.Pref,
			City:       body.City,
			Address:    body.Address,
			Building:   body.Building,
			Tel:        body.Tel,
			Email:      body.Email,
			ExecutorID: executorID,
		})
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}

	// 全スタッフへ通知配信（トランザクション外・ベストエフォート）
	notifURL := fmt.Sprintf("/clients/show?id=%d", storeVo.ID)
	_ = h.newNotifUC(h.db).FanOut(unotification.FanOutDto{
		Title:       "新しいクライアントが登録されました",
		Message:     storeVo.Name,
		MessageType: 1,
		ExecutorID:  executorID,
		URL:         notifURL,
	})

	// アクセストークンメール送信（非同期）
	go h.mailer.SendAccessToken(storeVo.Email, storeVo.Name, storeVo.AccessToken)

	c.JSON(http.StatusCreated, gin.H{"id": storeVo.ID})
}

// Update はクライアントを更新して詳細を返します。
// PUT /api/clients/:id/update
func (h *ClientHandler) Update(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}

	var body struct {
		Name     *string `json:"name"`
		PostCode *string `json:"post_code"`
		Pref     *string `json:"pref"`
		City     *string `json:"city"`
		Address  *string `json:"address"`
		Building *string `json:"building"`
		Tel      *string `json:"tel"`
		Email    *string `json:"email"`
		Status   *int    `json:"status"`
	}
	if err = c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(c)

	var detailVo *domclient.DetailVo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		detailVo, e = h.newClientUC(tx).Update(uclient.UpdateDto{
			ID:         id,
			Name:       body.Name,
			PostCode:   body.PostCode,
			Pref:       body.Pref,
			City:       body.City,
			Address:    body.Address,
			Building:   body.Building,
			Tel:        body.Tel,
			Email:      body.Email,
			Status:     body.Status,
			ExecutorID: executorID,
		})
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, mapClientDetail(detailVo))
}

// Qr はidentifierでQRコードデータを返します。
// GET /api/clients/:id/qr
func (h *ClientHandler) Qr(c *gin.Context) {
	identifier := c.Param("id")
	vo, err := h.newClientUC(h.db).GetQr(identifier)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"identifier":   vo.Identifier,
		"deeplink_url": vo.DeeplinkURL,
	})
}

// Info はidentifierでスマホアプリ向けクライアント情報を返します。
// GET /api/clients/:id/info
func (h *ClientHandler) Info(c *gin.Context) {
	identifier := c.Param("id")
	vo, err := h.newClientUC(h.db).GetInfo(identifier)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"identifier": vo.Identifier,
		"name":       vo.Name,
		"status":     vo.Status,
	})
}

// Start は利用開始処理を行い、アクセストークンを返します。
// PATCH /api/clients/:id/start
func (h *ClientHandler) Start(c *gin.Context) {
	identifier := c.Param("id")
	var startVo *domclient.StartVo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		startVo, e = h.newClientUC(tx).Start(identifier)
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"access_token": startVo.AccessToken,
	})
}

// Stop は利用停止処理を行います。
// PATCH /api/clients/:id/stop
func (h *ClientHandler) Stop(c *gin.Context) {
	identifier := c.Param("id")
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newClientUC(tx).Stop(identifier)
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{})
}

// Destroy はクライアントを論理削除します。
// DELETE /api/clients/:id/delete
func (h *ClientHandler) Destroy(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newClientUC(tx).Destroy(id, executorID)
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{})
}

// ---------- 変換ヘルパー ----------

// mapClientList はクライアント一覧 Vo をレスポンス用マップのスライスに変換します。
func mapClientList(clients []*domclient.ListItem) []gin.H {
	out := make([]gin.H, 0, len(clients))
	for _, c := range clients {
		out = append(out, gin.H{
			"id":         c.ID,
			"name":       c.Name,
			"status":     c.Status,
			"start_at":   formatTimePtr(c.StartAt),
			"stop_at":    formatTimePtr(c.StopAt),
			"created_at": formatTime(c.CreatedAt),
			"updated_at": formatTime(c.UpdatedAt),
		})
	}
	return out
}

// mapClientDetail はクライアント詳細 Vo をレスポンス用マップに変換します。
func mapClientDetail(c *domclient.DetailVo) gin.H {
	return gin.H{
		"id":         c.ID,
		"name":       c.Name,
		"identifier": c.Identifier,
		"post_code":  c.PostCode,
		"pref":       c.Pref,
		"city":       c.City,
		"address":    c.Address,
		"building":   c.Building,
		"tel":        c.Tel,
		"email":      c.Email,
		"status":     c.Status,
		"start_at":   formatTimePtr(c.StartAt),
		"stop_at":    formatTimePtr(c.StopAt),
		"created_at": formatTime(c.CreatedAt),
		"updated_at": formatTime(c.UpdatedAt),
	}
}

// parseUint64Param はパスパラメータを uint64 に変換します。
func parseUint64Param(c *gin.Context, key string) (uint64, error) {
	return strconv.ParseUint(c.Param(key), 10, 64)
}
