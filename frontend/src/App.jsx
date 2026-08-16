import { Navigate, Route, Routes } from "react-router-dom";
import { NavBar } from "./components/NavBar";
import { ProtectedRoute, AdminRoute } from "./components/ProtectedRoute";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AccountsPage } from "./pages/AccountsPage";
import { AccountDetailPage } from "./pages/AccountDetailPage";
import { AdminPage } from "./pages/AdminPage";

export default function App() {
  return (
    <>
      <NavBar />
      <main>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<AccountsPage />} />
            <Route path="/accounts/:id" element={<AccountDetailPage />} />

            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
