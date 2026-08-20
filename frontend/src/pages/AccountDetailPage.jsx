import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { api } from "../api/client";
import { ErrorMessage } from "../components/ErrorMessage";
import { usePaginatedList } from "../hooks/usePaginatedList";

export function AccountDetailPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();

  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const [depositAmount, setDepositAmount] = useState("");
  const [withdrawAmount, setWithdrawAmount] = useState("");
  const [transferDestination, setTransferDestination] = useState("");
  const [transferAmount, setTransferAmount] = useState("");

  const fetchTransactionsPage = useCallback(
    (page, size) => api.listTransactions(token, id, page, size),
    [token, id]
  );
  const transactions = usePaginatedList(fetchTransactionsPage);

  const loadAccount = async () => {
    setLoading(true);
    try {
      setAccount(await api.getAccount(token, id));
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccount();
    transactions.reload();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const runAction = async (action) => {
    setError(null);
    setBusy(true);
    try {
      await action();
      await Promise.all([loadAccount(), transactions.reload()]);
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  const handleDeposit = (event) => {
    event.preventDefault();
    runAction(async () => {
      await api.deposit(token, id, Number(depositAmount));
      setDepositAmount("");
    });
  };

  const handleWithdraw = (event) => {
    event.preventDefault();
    runAction(async () => {
      await api.withdraw(token, id, Number(withdrawAmount));
      setWithdrawAmount("");
    });
  };

  const handleTransfer = (event) => {
    event.preventDefault();
    runAction(async () => {
      await api.transfer(token, id, Number(transferDestination), Number(transferAmount));
      setTransferDestination("");
      setTransferAmount("");
    });
  };

  const handleClose = () => {
    if (!window.confirm("Close this account? This cannot be undone.")) return;
    runAction(() => api.closeAccount(token, id));
  };

  if (loading && !account) return <div className="page">Loading…</div>;
  if (!account) return <div className="page"><ErrorMessage error={error} /></div>;

  const isActive = account.status === "ACTIVE";

  return (
    <div className="page">
      <button type="button" className="link-button" onClick={() => navigate("/")}>
        ← Back to accounts
      </button>

      <div className="card">
        <div className="account-card-header">
          <h1>
            {account.type} #{account.id}
          </h1>
          <span className={`status-badge status-${account.status.toLowerCase()}`}>{account.status}</span>
        </div>
        <p className="account-balance">${account.balance}</p>
      </div>

      <ErrorMessage error={error} />

      {isActive && (
        <div className="operations-grid">
          <form className="card inline-form" onSubmit={handleDeposit}>
            <h2>Deposit</h2>
            <input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Amount"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              required
            />
            <button type="submit" disabled={busy}>
              Deposit
            </button>
          </form>

          <form className="card inline-form" onSubmit={handleWithdraw}>
            <h2>Withdraw</h2>
            <input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Amount"
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              required
            />
            <button type="submit" disabled={busy}>
              Withdraw
            </button>
          </form>

          <form className="card inline-form" onSubmit={handleTransfer}>
            <h2>Transfer</h2>
            <input
              type="number"
              placeholder="Destination account ID"
              value={transferDestination}
              onChange={(e) => setTransferDestination(e.target.value)}
              required
            />
            <input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Amount"
              value={transferAmount}
              onChange={(e) => setTransferAmount(e.target.value)}
              required
            />
            <button type="submit" disabled={busy}>
              Transfer
            </button>
          </form>
        </div>
      )}

      {account.status !== "CLOSED" && (
        <button type="button" className="danger" disabled={busy} onClick={handleClose}>
          Close account
        </button>
      )}

      <div className="card">
        <h2>Transaction history</h2>
        {transactions.items.length === 0 && !transactions.loading ? (
          <p>No transactions yet.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Amount</th>
                <th>Related account</th>
                <th>When</th>
              </tr>
            </thead>
            <tbody>
              {transactions.items.map((tx) => (
                <tr key={tx.id}>
                  <td>{tx.type}</td>
                  <td>${tx.amount}</td>
                  <td>{tx.relatedAccountId ?? "—"}</td>
                  <td>{new Date(tx.occurredAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {transactions.hasMore && (
          <div className="load-more">
            <button type="button" disabled={transactions.loadingMore} onClick={transactions.loadMore}>
              {transactions.loadingMore ? "Loading…" : "Load more"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
