"use client";

import React from "react";

import { useInvitationRedirect } from "../hooks/useInvitationRedirect";

export function InvitationRedirect(): React.JSX.Element {
  useInvitationRedirect();

  return <></>;
}
