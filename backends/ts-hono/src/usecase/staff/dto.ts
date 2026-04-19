export interface StaffUpdateRoleInput {
  staffId: number;
  role: number;
  executorId: number;
}

export interface StaffDestroyInput {
  staffId: number;
  executorId: number;
}
