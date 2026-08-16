import { useEffect, useState } from "react";
import { useAuth } from "../context/useAuth";
import { api } from "../api/client";
import { ErrorMessage } from "../components/ErrorMessage";

export function AdminPage() {
  const { token } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      const [accountData, auditData] = await Promise.all([api.adminListAccounts(token), api.adminAuditLogs(token)]);
      setAccounts(accountData);
      setAuditLogs(auditData);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const runAction = async (accountId, action) => {
    setError(null);
    setBusyId(accountId);
    try {
      await action();
      await load();
    } catch (err) {
      setError(err);
    } finally {
      setBusyId(null);
    }
  };

  if (loading) return <div className="page">Loading…</div>;

  return (
    <div className="page">
      <h1>Admin</h1>
      <ErrorMessage error={error} />

      <div className="card">
        <h2>All accounts</h2>
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Owner</th>
              <th>Type</th>
              <th>Balance</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.id}>
                <td>{account.id}</td>
                <td>{account.ownerId}</td>
                <td>{account.type}</td>
                <td>${account.balance}</td>
                <td>
                  <span className={`status-badge status-${account.status.toLowerCase()}`}>{account.status}</span>
                </td>
                <td className="admin-actions">
                  {account.status === "ACTIVE" && (
                    <button
                      type="button"
                      disabled={busyId === account.id}
                      onClick={() => runAction(account.id, () => api.adminBlockAccount(token, account.id))}
                    >
                      Block
                    </button>
                  )}
                  {account.status === "BLOCKED" && (
                    <button
                      type="button"
                      disabled={busyId === account.id}
                      onClick={() => runAction(account.id, () => api.adminActivateAccount(token, account.id))}
                    >
                      Activate
                    </button>
                  )}
                  {account.status !== "CLOSED" && (
                    <button
                      type="button"
                      className="danger"
                      disabled={busyId === account.id}
                      onClick={() => runAction(account.id, () => api.adminCloseAccount(token, account.id))}
                    >
                      Close
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h2>Audit log</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Action</th>
              <th>Actor</th>
              <th>Target</th>
              <th>When</th>
            </tr>
          </thead>
          <tbody>
            {auditLogs.map((entry) => (
              <tr key={entry.id}>
                <td>{entry.action}</td>
                <td>{entry.actorUserId ?? "—"}</td>
                <td>
                  {entry.targetType} {entry.targetId ? `#${entry.targetId}` : ""}
                </td>
                <td>{new Date(entry.occurredAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
