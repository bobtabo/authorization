export interface GateIssueInput {
  accessToken: string;
  member: string;
}

export interface GateVerifyInput {
  identifier: string;
  token: string;
}
