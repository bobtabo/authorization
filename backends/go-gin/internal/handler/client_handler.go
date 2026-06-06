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
	db          *gorm.DB
	newClientUC func(*gorm.DB) *uclient.Interactor
	newNotifUC  func(*gorm.DB) *unotification.Interactor
	mailer      *mail.Mailer
	historyRepo domclient.JwtHistoryRepository
	frontendURL string
}

// NewClientHandler は ClientHandler を生成します。
//
// db: GORM DB インスタンス
// newClientUC: クライアントユースケースファクトリ
// newNotifUC: 通知ユースケースファクトリ
// mailer: メール送信サービス
// historyRepo: JWT 履歴リポジトリ
// frontendURL: フロントエンド URL
func NewClientHandler(
	db *gorm.DB,
	newClientUC func(*gorm.DB) *uclient.Interactor,
	newNotifUC func(*gorm.DB) *unotification.Interactor,
	mailer *mail.Mailer,
	historyRepo domclient.JwtHistoryRepository,
	frontendURL string,
) *ClientHandler {
	return &ClientHandler{
		db:          db,
		newClientUC: newClientUC,
		newNotifUC:  newNotifUC,
		mailer:      mailer,
		historyRepo: historyRepo,
		frontendURL: frontendURL,
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

	limit := 20
	if v := c.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	page := 1
	if v := c.Query("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond.Offset = offset
	cond.Limit = limit
	cond.Sort = c.Query("sort")
	cond.SortType = c.Query("sort_type")

	uc := h.newClientUC(h.db)
	count, err := uc.CountByCondition(cond)
	if err != nil {
		_ = c.Error(err)
		return
	}
	clients, err := uc.FindByCondition(cond)
	if err != nil {
		_ = c.Error(err)
		return
	}

	pager := BuildPager(count, limit, offset, len(clients))
	c.JSON(http.StatusOK, gin.H{
		"data":  mapClientList(clients),
		"pager": pager,
	})
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
	var body StoreClientBody
	if err := c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.UnprocessableEntity("validation_error"))
		return
	}
	if err := validateStruct(&body); err != nil {
		_ = c.Error(err)
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

	// ご利用開始のご案内メール送信（非同期）
	activateURL := h.frontendURL + "/clients/" + storeVo.Identifier + "/qr"
	go h.mailer.SendActivation(storeVo.Email, storeVo.Name, activateURL)

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

	var body UpdateClientBody
	if err = c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.UnprocessableEntity("validation_error"))
		return
	}
	if err = validateStruct(&body); err != nil {
		_ = c.Error(err)
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

// JwtHistories はクライアントの JWT 履歴一覧を返します。
// GET /api/clients/:id/jwt-histories
func (h *ClientHandler) JwtHistories(c *gin.Context) {
	idStr := c.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid_id"})
		return
	}
	histories, err := h.historyRepo.FindByClientID(id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "internal_error"})
		return
	}
	out := make([]gin.H, 0, len(histories))
	for _, hist := range histories {
		out = append(out, gin.H{
			"id":        hist.ID,
			"member_id": hist.MemberID,
			"issue_at":  hist.IssueAt.Format("2006-01-02 15:04:05"),
			"jwt":       hist.Jwt,
		})
	}
	c.JSON(http.StatusOK, out)
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
