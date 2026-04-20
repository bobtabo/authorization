pub struct IssueDto {
    pub access_token: String,
    pub member_id:    String,
}

pub struct VerifyDto {
    pub identifier: String,
    pub token:      String,
}
