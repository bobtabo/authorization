package support

import (
	"time"

	"github.com/jinzhu/copier"
	"gorm.io/gorm"
)

// Assign はソース構造体からデスト構造体へ同名フィールドをコピーします。
// gorm.DeletedAt ↔ *time.Time の型変換を自動処理します。
func Assign(dst, src any) {
	_ = copier.CopyWithOption(dst, src, copier.Option{
		Converters: []copier.TypeConverter{
			{
				SrcType: gorm.DeletedAt{},
				DstType: (*time.Time)(nil),
				Fn: func(src any) (any, error) {
					da := src.(gorm.DeletedAt)
					if da.Valid {
						t := da.Time
						return &t, nil
					}
					return (*time.Time)(nil), nil
				},
			},
			{
				SrcType: (*time.Time)(nil),
				DstType: gorm.DeletedAt{},
				Fn: func(src any) (any, error) {
					t, ok := src.(*time.Time)
					if ok && t != nil {
						return gorm.DeletedAt{Time: *t, Valid: true}, nil
					}
					return gorm.DeletedAt{}, nil
				},
			},
		},
	})
}
