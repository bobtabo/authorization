import { ClientQrPage } from "@/features/clients/components/ClientQrPage";

type Props = {
  params: Promise<{ identifier: string }>;
};

export default function Page({ params }: Props) {
  return <ClientQrPage params={params} />;
}
