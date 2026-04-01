import { Link } from 'react-router-dom';

function AdminDashboard() {
  return (
    <div>
      <h1>Admin Dashboard</h1>
      <div className="dashboard-cards">
        <Link to="/admin/seasons" className="dashboard-card">
          <h3>Seasons</h3>
          <p>Manage playoff pool seasons</p>
        </Link>
        <Link to="/admin/rounds" className="dashboard-card">
          <h3>Rounds</h3>
          <p>Manage rounds, questions, and options</p>
        </Link>
        <Link to="/admin/participants" className="dashboard-card">
          <h3>Participants</h3>
          <p>View participants and their responses</p>
        </Link>
      </div>
    </div>
  );
}

export default AdminDashboard;
