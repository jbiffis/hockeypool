import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getRounds, createRound, updateRound, deleteRound, updateRoundStatus } from '../api/rounds';
import { getSeasons } from '../api/seasons';
import RoundForm from '../components/RoundForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';

const STATUS_FORWARD = { draft: 'open', open: 'closed', closed: 'scored' };
const STATUS_BACKWARD = { scored: 'closed', closed: 'open', open: 'draft' };

function RoundsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [rounds, setRounds] = useState([]);
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const querySeasonId = searchParams.get('seasonId');
    getSeasons().then(res => {
      setSeasons(res.data);
      if (querySeasonId) {
        setSelectedSeasonId(Number(querySeasonId));
      } else {
        const active = res.data.find(s => s.status === 'active');
        if (active) setSelectedSeasonId(active.id);
        else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
      }
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => {
    if (selectedSeasonId) fetchRounds();
  }, [selectedSeasonId]);

  async function fetchRounds() {
    try {
      const res = await getRounds(selectedSeasonId);
      setRounds(res.data);
    } catch (err) {
      setError('Failed to load rounds');
    }
  }

  async function handleCreate(data) {
    try {
      await createRound({ ...data, seasonId: selectedSeasonId });
      setShowCreateForm(false);
      fetchRounds();
    } catch (err) {
      setError('Failed to create round');
    }
  }

  async function handleUpdate(id, data) {
    try {
      await updateRound(id, data);
      setEditingId(null);
      fetchRounds();
    } catch (err) {
      setError('Failed to update round');
    }
  }

  async function handleDelete(id) {
    try {
      await deleteRound(id);
      setConfirmDelete(null);
      fetchRounds();
    } catch (err) {
      setError('Failed to delete round');
    }
  }

  async function handleStatusChange(id, newStatus) {
    try {
      await updateRoundStatus(id, newStatus);
      fetchRounds();
    } catch (err) {
      setError('Failed to update status');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Rounds</h1>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <select
            value={selectedSeasonId || ''}
            onChange={(e) => setSelectedSeasonId(Number(e.target.value))}
            className="form-select"
          >
            {seasons.map(s => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
          {!showCreateForm && (
            <button className="btn btn-primary" onClick={() => setShowCreateForm(true)}>Create Round</button>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showCreateForm && (
        <div className="form-card">
          <h2>New Round</h2>
          <RoundForm
            round={null}
            allRounds={rounds}
            onSubmit={handleCreate}
            onCancel={() => setShowCreateForm(false)}
          />
        </div>
      )}

      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th>Deadline</th>
            <th>Order</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {rounds.map(round => (
            editingId === round.id ? (
              <tr key={round.id}>
                <td colSpan={5}>
                  <RoundForm
                    round={round}
                    allRounds={rounds}
                    onSubmit={(data) => handleUpdate(round.id, data)}
                    onCancel={() => setEditingId(null)}
                  />
                </td>
              </tr>
            ) : (
              <tr key={round.id}>
                <td>
                  <a className="link" onClick={() => navigate(`/admin/rounds/${round.id}`)}>
                    {round.name}
                  </a>
                </td>
                <td><StatusBadge status={round.status} /></td>
                <td>{round.deadline ? new Date(round.deadline).toLocaleString() : '--'}</td>
                <td>{round.displayOrder}</td>
                <td className="actions">
                  <button className="btn btn-secondary btn-sm" onClick={() => setEditingId(round.id)}>Edit</button>
                  <button className="btn btn-danger btn-sm" onClick={() => setConfirmDelete(round.id)}>Delete</button>
                  {STATUS_BACKWARD[round.status] && (
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleStatusChange(round.id, STATUS_BACKWARD[round.status])}
                    >
                      ← {STATUS_BACKWARD[round.status]}
                    </button>
                  )}
                  {STATUS_FORWARD[round.status] && (
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => handleStatusChange(round.id, STATUS_FORWARD[round.status])}
                    >
                      {STATUS_FORWARD[round.status]} →
                    </button>
                  )}
                  <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/admin/rounds/${round.id}/responses`)}>
                    Responses
                  </button>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => {
                      const url = `${window.location.origin}/pool/season/${selectedSeasonId}/round/${round.id}`;
                      navigator.clipboard.writeText(url);
                      alert('URL copied: ' + url);
                    }}
                  >
                    Share URL
                  </button>
                </td>
              </tr>
            )
          ))}
          {rounds.length === 0 && (
            <tr><td colSpan={5} className="empty-message">No rounds yet.</td></tr>
          )}
        </tbody>
      </table>

      <ConfirmDialog
        isOpen={confirmDelete !== null}
        message="Are you sure you want to delete this round?"
        onConfirm={() => handleDelete(confirmDelete)}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}

export default RoundsPage;
