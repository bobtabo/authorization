/** `public/flow/*.svg` をルートから配信。`BASE_URL` でサブパスデプロイにも対応（バンドルの `?url` より確実）。 */
function flowAsset(file: string): string {
  return `${import.meta.env.BASE_URL}flow/${file}`;
}

export const flowImages = {
  login: flowAsset("login.svg"),
  header: flowAsset("header.svg"),
  clientList: flowAsset("client-list.svg"),
  accountList: flowAsset("account-list.svg"),
  clientRegister: flowAsset("client-register.svg"),
  clientEdit: flowAsset("client-edit.svg"),
  clientDetail: flowAsset("client-detail.svg"),
} as const;
