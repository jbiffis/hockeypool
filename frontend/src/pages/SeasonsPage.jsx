import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSeasons, createSeason, updateSeason, deleteSeason } from '../api/seasons';
import SeasonForm from '../components/SeasonForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';

function SeasonsPage() {
  const navigate = useNavigate();
  const [seasons, setSeasons] = useState([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchSeasons();
  }, []);

  async function fetchSeasons() {
    try {
      const res = await getSeasons();
      setSeasons(res.data);
    } catch (err) {
      setError('Failed to load seasons');
    }
  }

  async function handleCreate(data) {
    try {
      await createSeason(data);
      setShowCreateForm(false);
      fetchSeasons();
    } catch (err) {
      setError('Failed to create season');
    }
  }

  async function handleUpdate(id, data) {
    try {
      await updateSeason(id, data);
      setEditingId(null);
      fetchSeasons();
    } catch (err) {
      setError('Failed to update season');
    }
  }

  async function handleDelete(id) {
    try {
      await deleteSeason(id);
      setConfirmDelete(null);
      fetchSeasons();
    } catch (err) {
      setError('Failed to delete season');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Seasons</h1>
        {!showCreateForm && (
          <button className="btn btn-primary" onClick={() => setShowCreateForm(true)}>Create Season</button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {showCreateForm && (
        <div className="form-card">
          <h2>New Season</h2>
          <SeasonForm onSubmit={handleCreate} onCancel={() => setShowCreateForm(false)} />
        </div>
      )}

      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Year</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {seasons.map(season => (
            editingId === season.id ? (
              <tr key={season.id}>
                <td colSpan={4}>
                  <SeasonForm
                    season={season}
                    onSubmit={(data) => handleUpdate(season.id, data)}
                    onCancel={() => setEditingId(null)}
                  />
                </td>
              </tr>
            ) : (
              <tr key={season.id}>
                <td>
                  <a className="link" onClick={() => navigate(`/admin/rounds?seasonId=${season.id}`)}>
                    {season.name}
                  </a>
                </td>
                <td>{season.year}</td>
                <td><StatusBadge status={season.status} /></td>
                <td className="actions">
                  <button className="btn btn-secondary btn-sm" onClick={() => setEditingId(season.id)}>Edit</button>
                  <button className="btn btn-danger btn-sm" onClick={() => setConfirmDelete(season.id)}>Delete</button>
                </td>
              </tr>
            )
          ))}
          {seasons.length === 0 && (
            <tr><td colSpan={4} className="empty-message">No seasons yet.</td></tr>
          )}
        </tbody>
      </table>

      <ConfirmDialog
        isOpen={confirmDelete !== null}
        message="Are you sure you want to delete this season?"
        onConfirm={() => handleDelete(confirmDelete)}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}

export default SeasonsPage;
