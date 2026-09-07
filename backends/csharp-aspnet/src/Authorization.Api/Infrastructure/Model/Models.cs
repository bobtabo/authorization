/*
 * EF Core エンティティモデル（テーブル定義）モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Authorization.Api.Infrastructure.Model;

/// <summary>clients テーブル</summary>
[Table("clients")]
public sealed class ClientModel
{
    [Key, Column("id")]        public long      Id          { get; set; }
    [Column("name")]           public string    Name        { get; set; } = "";
    [Column("identifier")]     public string    Identifier  { get; set; } = "";
    [Column("post_code")]      public string    PostCode    { get; set; } = "";
    [Column("pref")]           public string    Pref        { get; set; } = "";
    [Column("city")]           public string    City        { get; set; } = "";
    [Column("address")]        public string    Address     { get; set; } = "";
    [Column("building")]       public string?   Building    { get; set; }
    [Column("tel")]            public string    Tel         { get; set; } = "";
    [Column("email")]          public string    Email       { get; set; } = "";
    [Column("access_token")]   public string    AccessToken { get; set; } = "";
    [Column("private_key")]    public string    PrivateKey  { get; set; } = "";
    [Column("public_key")]     public string    PublicKey   { get; set; } = "";
    [Column("fingerprint")]    public string    Fingerprint { get; set; } = "";
    [Column("status")]         public int       Status      { get; set; }
    [Column("start_at")]       public DateTime? StartAt     { get; set; }
    [Column("stop_at")]        public DateTime? StopAt      { get; set; }
    [Column("created_at")]     public DateTime  CreatedAt   { get; set; }
    [Column("created_by")]     public int?      CreatedBy   { get; set; }
    [Column("updated_at")]     public DateTime  UpdatedAt   { get; set; }
    [Column("updated_by")]     public int?      UpdatedBy   { get; set; }
    [Column("deleted_at")]     public DateTime? DeletedAt   { get; set; }
    [Column("deleted_by")]     public int?      DeletedBy   { get; set; }
    [Column("version")]        public int       Version     { get; set; }
}

/// <summary>staffs テーブル</summary>
[Table("staffs")]
public sealed class StaffModel
{
    [Key, Column("id")]        public long      Id          { get; set; }
    [Column("name")]           public string    Name        { get; set; } = "";
    [Column("email")]          public string    Email       { get; set; } = "";
    [Column("provider")]       public int       Provider    { get; set; }
    [Column("provider_id")]    public string    ProviderId  { get; set; } = "";
    [Column("avatar")]         public string?   Avatar      { get; set; }
    [Column("role")]           public int       Role        { get; set; }
    [Column("last_login_at")]  public DateTime? LastLoginAt { get; set; }
    [Column("created_at")]     public DateTime  CreatedAt   { get; set; }
    [Column("created_by")]     public int?      CreatedBy   { get; set; }
    [Column("updated_at")]     public DateTime  UpdatedAt   { get; set; }
    [Column("updated_by")]     public int?      UpdatedBy   { get; set; }
    [Column("deleted_at")]     public DateTime? DeletedAt   { get; set; }
    [Column("deleted_by")]     public int?      DeletedBy   { get; set; }
    [Column("version")]        public int       Version     { get; set; }
}

/// <summary>invitations テーブル</summary>
[Table("invitations")]
public sealed class InvitationModel
{
    [Key, Column("id")]        public int       Id        { get; set; }
    [Column("token")]          public string    Token     { get; set; } = "";
    [Column("role")]           public int       Role      { get; set; }
    [Column("created_at")]     public DateTime  CreatedAt { get; set; }
    [Column("created_by")]     public int?      CreatedBy { get; set; }
    [Column("updated_at")]     public DateTime  UpdatedAt { get; set; }
    [Column("updated_by")]     public int?      UpdatedBy { get; set; }
    [Column("deleted_at")]     public DateTime? DeletedAt { get; set; }
    [Column("deleted_by")]     public int?      DeletedBy { get; set; }
    [Column("version")]        public int       Version   { get; set; }
}

/// <summary>jwt_histories テーブル</summary>
[Table("jwt_histories")]
public sealed class JwtHistoryModel
{
    [Key, Column("id")]        public long      Id        { get; set; }
    [Column("client_id")]      public long      ClientId  { get; set; }
    [Column("member_id")]      public string    MemberId  { get; set; } = "";
    [Column("issue_at")]       public DateTime  IssueAt   { get; set; }
    [Column("jwt")]            public string    Jwt       { get; set; } = "";
    [Column("created_at")]     public DateTime  CreatedAt { get; set; }
    [Column("created_by")]     public int       CreatedBy { get; set; }
    [Column("updated_at")]     public DateTime  UpdatedAt { get; set; }
    [Column("updated_by")]     public int       UpdatedBy { get; set; }
    [Column("deleted_at")]     public DateTime? DeletedAt { get; set; }
    [Column("deleted_by")]     public int?      DeletedBy { get; set; }
    [Column("version")]        public int       Version   { get; set; }
}

/// <summary>notifications テーブル</summary>
[Table("notifications")]
public sealed class NotificationModel
{
    [Key, Column("id")]        public long      Id          { get; set; }
    [Column("staff_id")]       public long      StaffId     { get; set; }
    [Column("message_type")]   public int       MessageType { get; set; }
    [Column("title")]          public string    Title       { get; set; } = "";
    [Column("message")]        public string    Message     { get; set; } = "";
    [Column("url")]            public string?   Url         { get; set; }
    [Column("read")]           public bool      Read        { get; set; }
    [Column("created_at")]     public DateTime  CreatedAt   { get; set; }
    [Column("created_by")]     public int       CreatedBy   { get; set; }
    [Column("updated_at")]     public DateTime  UpdatedAt   { get; set; }
    [Column("updated_by")]     public int       UpdatedBy   { get; set; }
    [Column("deleted_at")]     public DateTime? DeletedAt   { get; set; }
    [Column("deleted_by")]     public int?      DeletedBy   { get; set; }
    [Column("version")]        public int       Version     { get; set; }
}
