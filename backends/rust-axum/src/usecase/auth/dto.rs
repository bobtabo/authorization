pub struct LoginDto {
    pub provider:    i32,
    pub provider_id: String,
    pub name:        String,
    pub email:       String,
    pub avatar:      Option<String>,
}
