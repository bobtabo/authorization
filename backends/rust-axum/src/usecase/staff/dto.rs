pub struct UpdateRoleDto {
    pub id:          u32,
    pub role:        i32,
    pub executor_id: u32,
}

pub struct DestroyDto {
    pub id:          u32,
    pub executor_id: u32,
}
