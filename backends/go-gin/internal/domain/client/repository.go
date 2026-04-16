package client

// Repository はクライアントの永続化インターフェースです。
type Repository interface {
	FindByCondition(cond Condition) ([]*Client, error)
	FindByID(id uint64) (*Client, error)
	FindByAccessToken(token string) (*Client, error)
	FindByIdentifier(identifier string) (*Client, error)
	Save(c *Client) (*Client, error)
	SoftDelete(id uint64, deletedBy uint) error
}
