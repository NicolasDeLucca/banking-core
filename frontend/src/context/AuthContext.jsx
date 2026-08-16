import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import { decodeJwt } from "../api/jwt";
import { AuthContext } from "./useAuth";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));

  useEffect(() => {
    if (token) {
      localStorage.setItem("token", token);
    } else {
      localStorage.removeItem("token");
    }
  }, [token]);

  const user = useMemo(() => (token ? decodeJwt(token) : null), [token]);

  const login = async (email, password) => {
    const result = await api.login(email, password);
    setToken(result.token);
  };

  const register = async (email, password) => {
    const result = await api.register(email, password);
    setToken(result.token);
  };

  const logout = () => setToken(null);

  const value = {
    token,
    user,
    isAuthenticated: Boolean(token),
    isAdmin: user?.role === "ADMIN",
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
