import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { api } from "../api/client";
import { ErrorMessage } from "../components/ErrorMessage";

export function AccountsPage() {
  const { token } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [type, setType] = useState("CHECKING");
  const [creating, setCreating] = useState(false);

  const loadAccounts = async () => {
    setLoading(true);
    try {
      setAccounts(await api.listAccounts(token));
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreate = async (event) => {
    event.preventDefault();
    setError(null);
    setCreating(true);
    try {
      await api.createAccount(token, type);
      await loadAccounts();
    } catch (err) {
      setError(err);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="page">
      <h1>Your accounts</h1>

      <form className="card inline-form" onSubmit={handleCreate}>
        <label>
          New account type
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="CHECKING">Checking</option>
            <option value="SAVINGS">Savings</option>
          </select>
        </label>
        <button type="submit" disabled={creating}>
          {creating ? "Creating…" : "Open account"}
        </button>
      </form>

      <ErrorMessage error={error} />

      {loading ? (
        <p>Loading…</p>
      ) : accounts.length === 0 ? (
        <p>No accounts yet — open one above.</p>
      ) : (
        <div className="account-grid">
          {accounts.map((account) => (
            <Link key={account.id} to={`/accounts/${account.id}`} className={`card account-card status-${account.status.toLowerCase()}`}>
              <div className="account-card-header">
                <span>{account.type}</span>
                <span className={`status-badge status-${account.status.toLowerCase()}`}>{account.status}</span>
              </div>
              <p className="account-balance">${account.balance}</p>
              <p className="account-id">#{account.id}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
