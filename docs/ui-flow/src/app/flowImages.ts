/** Vite が解決する URL（dev / build / Vercel 共通）。差し替えは同パスのファイルを上書き。 */
import login from "../assets/flow/login.svg?url";
import header from "../assets/flow/header.svg?url";
import clientList from "../assets/flow/client-list.svg?url";
import accountList from "../assets/flow/account-list.svg?url";
import clientRegister from "../assets/flow/client-register.svg?url";
import clientEdit from "../assets/flow/client-edit.svg?url";
import clientDetail from "../assets/flow/client-detail.svg?url";

export const flowImages = {
  login,
  header,
  clientList,
  accountList,
  clientRegister,
  clientEdit,
  clientDetail,
} as const;
