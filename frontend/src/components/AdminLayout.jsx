import { NavLink, Outlet } from 'react-router-dom';

function AdminLayout() {
  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <h2 className="sidebar-title">Playoff Pool</h2>
        <nav className="sidebar-nav">
          <NavLink to="/admin" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Dashboard
          </NavLink>
          <NavLink to="/admin/rounds" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Rounds
          </NavLink>
          <NavLink to="/admin/participants" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Participants
          </NavLink>
        </nav>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}

export default AdminLayout;
