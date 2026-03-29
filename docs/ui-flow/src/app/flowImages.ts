/** `public/flow/*.png` をルートから配信。`BASE_URL` でサブパスデプロイにも対応。 */
function flowAsset(file: string): string {
  return `${import.meta.env.BASE_URL}flow/${file}`;
}

export const flowImages = {
  login: flowAsset("login.png"),
  header: flowAsset("header.png"),
  clientList: flowAsset("client-list.png"),
  staffList: flowAsset("staff-list.png"),
  clientRegister: flowAsset("client-register.png"),
  clientEdit: flowAsset("client-edit.png"),
  clientDetail: flowAsset("client-detail.png"),
} as const;
