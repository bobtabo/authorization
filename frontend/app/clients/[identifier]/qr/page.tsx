import React from "react";

import { ClientQrPage } from "@/features/clients/components/ClientQrPage";

type Props = {
  params: Promise<{ identifier: string }>;
};

export default function Page({ params }: Props): React.JSX.Element {
  return <ClientQrPage params={params} />;
}
