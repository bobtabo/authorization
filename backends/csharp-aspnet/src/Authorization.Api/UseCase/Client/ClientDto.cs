/*
 * クライアントユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.UseCase.Client;

/// <summary>一覧検索条件 DTO です。</summary>
public sealed record ClientListConditionDto(
    string? Keyword = null,
    string? StartFrom = null,
    string? StartTo = null,
    IReadOnlyList<int>? Statuses = null,
    int Offset = 0,
    int Limit = 10,
    string? Sort = null,
    string? SortType = null
);

/// <summary>登録 DTO です。</summary>
public sealed record ClientStoreDto(
    string Name,
    string PostCode,
    string Pref,
    string City,
    string Address,
    string Building,
    string Tel,
    string Email,
    long ExecutorId
);

/// <summary>更新 DTO です。null の項目は更新しません。</summary>
public sealed record ClientUpdateDto(
    long Id,
    string? Name,
    string? PostCode,
    string? Pref,
    string? City,
    string? Address,
    string? Building,
    string? Tel,
    string? Email,
    int? Status,
    long ExecutorId,
    int Version
);

/// <summary>QR 取得 DTO です。</summary>
public sealed record ClientQrDto(string Identifier);

/// <summary>スマホアプリ向け情報取得 DTO です。</summary>
public sealed record ClientInfoDto(string Identifier);

/// <summary>利用開始 DTO です。</summary>
public sealed record ClientStartDto(string Identifier);

/// <summary>利用停止 DTO です。</summary>
public sealed record ClientStopDto(string Identifier);
