// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using Authorization.Api.Domain.Client;
using Authorization.Api.Support;
using Authorization.Api.Support.Http.Responses;

namespace Authorization.Api.Http.Responses.Client;

/// <summary>クライアント詳細Responseクラスです（PHP版 ShowResponse 相当）。</summary>
public sealed class ShowResponse : AbstractResponse
{
    /// <summary>コンストラクタ。</summary>
    /// <param name="c">クライアント詳細</param>
    public ShowResponse(ClientDetailVo c)
    {
        Id = c.Id;
        Name = c.Name;
        Identifier = c.Identifier;
        PostCode = c.PostCode;
        Pref = c.Pref;
        City = c.City;
        Address = c.Address;
        Building = c.Building;
        Tel = c.Tel;
        Email = c.Email;
        Status = c.Status;
        StartAt = DateFormat.ToMinute(c.StartAt);
        StopAt = DateFormat.ToMinute(c.StopAt);
        CreatedAt = DateFormat.ToMinute(c.CreatedAt);
        UpdatedAt = DateFormat.ToMinute(c.UpdatedAt);
        Version = c.Version;
    }

    /// <summary>クライアントID</summary>
    public long Id { get; }

    /// <summary>名称</summary>
    public string Name { get; }

    /// <summary>識別子</summary>
    public string Identifier { get; }

    /// <summary>郵便番号</summary>
    public string PostCode { get; }

    /// <summary>都道府県</summary>
    public string Pref { get; }

    /// <summary>市区町村</summary>
    public string City { get; }

    /// <summary>番地</summary>
    public string Address { get; }

    /// <summary>建物名</summary>
    public string Building { get; }

    /// <summary>電話番号</summary>
    public string Tel { get; }

    /// <summary>メールアドレス</summary>
    public string Email { get; }

    /// <summary>ステータス</summary>
    public int Status { get; }

    /// <summary>利用開始日時（yyyy-MM-dd HH:mm形式）</summary>
    public string? StartAt { get; }

    /// <summary>利用停止日時（yyyy-MM-dd HH:mm形式）</summary>
    public string? StopAt { get; }

    /// <summary>作成日時（yyyy-MM-dd HH:mm形式）</summary>
    public string CreatedAt { get; }

    /// <summary>更新日時（yyyy-MM-dd HH:mm形式）</summary>
    public string UpdatedAt { get; }

    /// <summary>楽観排他ロック用バージョン</summary>
    public int Version { get; }
}
