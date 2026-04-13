import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();
  const { logout } = useAuth();

  function handleNavClick() {
    setSidebarOpen(false);
  }

  async function handleLogout() {
    await logout();
    navigate('/admin/login');
  }

  return (
    <div className="admin-layout">
      {/* Mobile header bar */}
      <div className="admin-mobile-header">
        <button
          className="admin-hamburger"
          onClick={() => setSidebarOpen(o => !o)}
          aria-label="Toggle menu"
        >
          <span /><span /><span />
        </button>
        <span className="admin-mobile-title">Playoff Pool</span>
      </div>

      {/* Sidebar backdrop */}
      {sidebarOpen && (
        <div className="admin-sidebar-backdrop" onClick={() => setSidebarOpen(false)} />
      )}

      <aside className={`admin-sidebar${sidebarOpen ? ' sidebar-open' : ''}`}>
        <h2 className="sidebar-title">Playoff Pool</h2>
        <nav className="sidebar-nav">
          <NavLink to="/admin" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} onClick={handleNavClick}>
            Dashboard
          </NavLink>
          <NavLink to="/admin/rounds" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} onClick={handleNavClick}>
            Rounds
          </NavLink>
          <NavLink to="/admin/participants" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} onClick={handleNavClick}>
            Participants
          </NavLink>
          <NavLink to="/admin/divisions" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} onClick={handleNavClick}>
            Divisions
          </NavLink>
        </nav>
        <div style={{ marginTop: 'auto', padding: '1.25rem' }}>
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleLogout}
            style={{ width: '100%' }}
          >
            Log Out
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}

export default AdminLayout;
