// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
namespace Authorization.Api.Infrastructure.Persistence;

/// <summary>LIKE検索用のエスケープ処理クラスです。</summary>
public static class LikeEscaper
{
    /// <summary>
    /// LIKE検索用にキーワードをエスケープします。
    /// キーワードに含まれる <c>\</c>/<c>%</c>/<c>_</c> はLIKEパターン上でエスケープ文字・ワイルドカードとして
    /// 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
    /// <c>\</c> は他の文字のエスケープに使うため最初に変換する。
    /// </summary>
    /// <param name="keyword">エスケープ対象のキーワード</param>
    /// <returns>エスケープ済みのキーワード</returns>
    public static string Escape(string keyword) =>
        keyword.Replace("\\", "\\\\").Replace("%", "\\%").Replace("_", "\\_");
}
