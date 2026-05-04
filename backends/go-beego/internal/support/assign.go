package support

import "github.com/jinzhu/copier"

// Assign はソース構造体からデスト構造体へ同名フィールドをコピーします。
func Assign(dst, src any) {
	_ = copier.Copy(dst, src)
}
