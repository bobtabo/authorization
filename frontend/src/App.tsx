import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import HomePage from "@/app/page";
import LoginPage from "@/app/login/page";
import RegisterPage from "@/app/register/page";
import StaffPage from "@/app/staff/page";
import ClientsPage from "@/app/clients/page";
import ClientsCreatePage from "@/app/clients/create/page";
import ClientsEditPage from "@/app/clients/edit/page";
import ClientsShowPage from "@/app/clients/show/page";
import InvitationTokenPage from "@/app/invitation/[token]/page";
import NotionIntegrationPage from "@/app/settings/notion/page";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/staff" element={<StaffPage />} />
        <Route path="/clients/create" element={<ClientsCreatePage />} />
        <Route path="/clients/edit" element={<ClientsEditPage />} />
        <Route path="/clients/show" element={<ClientsShowPage />} />
        <Route path="/clients" element={<ClientsPage />} />
        <Route path="/settings/notion" element={<NotionIntegrationPage />} />
        <Route path="/invitation/:token" element={<InvitationTokenPage />} />
        <Route
          path="*"
          element={
            <main style={{ fontFamily: "system-ui, sans-serif", padding: 24 }}>
              <p>ページが見つかりません。</p>
              <p>
                <Link to="/">トップへ</Link>
              </p>
            </main>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
