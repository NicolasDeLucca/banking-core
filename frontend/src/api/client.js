const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request(path, { method = "GET", token, body } = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    const error = new Error(data?.message || `Request failed with status ${response.status}`);
    error.code = data?.code;
    error.status = response.status;
    throw error;
  }

  return data;
}

export const api = {
  register: (email, password) => request("/api/auth/register", { method: "POST", body: { email, password } }),
  login: (email, password) => request("/api/auth/login", { method: "POST", body: { email, password } }),

  listAccounts: (token) => request("/api/accounts", { token }),
  getAccount: (token, id) => request(`/api/accounts/${id}`, { token }),
  createAccount: (token, type) => request("/api/accounts", { method: "POST", token, body: { type } }),
  deposit: (token, id, amount) => request(`/api/accounts/${id}/deposit`, { method: "POST", token, body: { amount } }),
  withdraw: (token, id, amount) => request(`/api/accounts/${id}/withdraw`, { method: "POST", token, body: { amount } }),
  transfer: (token, id, destinationAccountId, amount) =>
    request(`/api/accounts/${id}/transfer`, { method: "POST", token, body: { destinationAccountId, amount } }),
  closeAccount: (token, id) => request(`/api/accounts/${id}/close`, { method: "POST", token }),
  listTransactions: (token, id) => request(`/api/accounts/${id}/transactions`, { token }),

  adminListAccounts: (token) => request("/api/admin/accounts", { token }),
  adminGetAccount: (token, id) => request(`/api/admin/accounts/${id}`, { token }),
  adminBlockAccount: (token, id) => request(`/api/admin/accounts/${id}/block`, { method: "POST", token }),
  adminActivateAccount: (token, id) => request(`/api/admin/accounts/${id}/activate`, { method: "POST", token }),
  adminCloseAccount: (token, id) => request(`/api/admin/accounts/${id}/close`, { method: "POST", token }),
  adminAuditLogs: (token) => request("/api/admin/audit-logs", { token }),
};
