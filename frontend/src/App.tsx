import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { ErrorPage } from "@/components/error-page";
import LoginPage from "@/app/login/page";
import ErrorRoutePage from "@/app/error/page";
import RegisterPage from "@/app/register/page";
import StaffPage from "@/app/staffs/page";
import ClientsPage from "@/app/clients/page";
import ClientsCreatePage from "@/app/clients/create/page";
import ClientsEditPage from "@/app/clients/edit/page";
import ClientsShowPage from "@/app/clients/show/page";
import InvitationTokenPage from "@/app/invitation/[token]/page";
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/staffs" element={<StaffPage />} />
        <Route path="/clients/create" element={<ClientsCreatePage />} />
        <Route path="/clients/edit" element={<ClientsEditPage />} />
        <Route path="/clients/show" element={<ClientsShowPage />} />
        <Route path="/clients" element={<ClientsPage />} />
        <Route path="/invitation/:token" element={<InvitationTokenPage />} />
        <Route path="/error" element={<ErrorRoutePage />} />
        <Route path="*" element={<ErrorPage statusCode={404} />} />
      </Routes>
    </BrowserRouter>
  );
}
