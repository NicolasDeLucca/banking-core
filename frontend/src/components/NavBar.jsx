import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

export function NavBar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        Banking Core
      </Link>
      {isAuthenticated && (
        <nav className="navbar-links">
          <Link to="/">Accounts</Link>
          {isAdmin && <Link to="/admin">Admin</Link>}
          <span className="navbar-user">
            {user?.email} {isAdmin && <span className="badge">ADMIN</span>}
          </span>
          <button type="button" onClick={handleLogout}>
            Log out
          </button>
        </nav>
      )}
    </header>
  );
}
