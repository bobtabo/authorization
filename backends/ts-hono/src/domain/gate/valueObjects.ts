export interface GateIssueVo {
  token: string;
}

export interface GateVerifyVo {
  identifier: string;
  member: string | undefined;
  fingerprint: string | null;
  payload: Record<string, unknown>;
}
