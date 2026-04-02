import { useState, useEffect } from 'react';
import { getSeasons } from '../api/seasons';
import { getParticipants } from '../api/participants';
import {
  getAdminDivisions,
  createDivision,
  addParticipantToDivision,
  removeParticipantFromDivision,
} from '../api/divisions';

function DivisionsPage() {
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [divisions, setDivisions] = useState([]);
  const [participants, setParticipants] = useState([]);
  const [newDivisionName, setNewDivisionName] = useState('');
  const [addingTo, setAddingTo] = useState(null); // divisionId being assigned to
  const [selectedParticipant, setSelectedParticipant] = useState('');
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      const active = res.data.find(s => s.status === 'active');
      if (active) setSelectedSeasonId(active.id);
      else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => {
    if (!selectedSeasonId) return;
    loadDivisions();
    getParticipants(selectedSeasonId)
      .then(res => setParticipants(res.data))
      .catch(() => setError('Failed to load participants'));
  }, [selectedSeasonId]);

  function loadDivisions() {
    getAdminDivisions(selectedSeasonId)
      .then(res => setDivisions(res.data))
      .catch(() => setError('Failed to load divisions'));
  }

  async function handleCreateDivision(e) {
    e.preventDefault();
    if (!newDivisionName.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await createDivision(selectedSeasonId, newDivisionName.trim());
      setNewDivisionName('');
      loadDivisions();
    } catch {
      setError('Failed to create division');
    } finally {
      setSaving(false);
    }
  }

  async function handleAddParticipant(divisionId) {
    if (!selectedParticipant) return;
    setSaving(true);
    setError(null);
    try {
      await addParticipantToDivision(divisionId, Number(selectedParticipant));
      setAddingTo(null);
      setSelectedParticipant('');
      loadDivisions();
    } catch {
      setError('Failed to add participant');
    } finally {
      setSaving(false);
    }
  }

  async function handleRemoveParticipant(divisionId, participantId) {
    setSaving(true);
    setError(null);
    try {
      await removeParticipantFromDivision(divisionId, participantId);
      loadDivisions();
    } catch {
      setError('Failed to remove participant');
    } finally {
      setSaving(false);
    }
  }

  function getUnassignedParticipants(division) {
    const assignedIds = new Set(division.participants.map(p => p.id));
    return participants.filter(p => !assignedIds.has(p.id));
  }

  return (
    <div>
      <div className="page-header">
        <h1>Divisions</h1>
        <select
          value={selectedSeasonId || ''}
          onChange={e => setSelectedSeasonId(Number(e.target.value))}
          className="form-select"
        >
          {seasons.map(s => (
            <option key={s.id} value={s.id}>{s.name}</option>
          ))}
        </select>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Create new division */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ marginTop: 0, marginBottom: '0.75rem', fontSize: '1rem' }}>Create New Division</h2>
        <form onSubmit={handleCreateDivision} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <input
            className="form-input"
            type="text"
            placeholder="Division name"
            value={newDivisionName}
            onChange={e => setNewDivisionName(e.target.value)}
            style={{ maxWidth: '280px' }}
          />
          <button className="btn btn-primary" type="submit" disabled={saving || !newDivisionName.trim()}>
            Create
          </button>
        </form>
      </div>

      {/* Division cards */}
      {divisions.length === 0 ? (
        <div className="empty-message">No divisions yet for this season.</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {divisions.map(division => (
            <div key={division.id} className="card">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                <h2 style={{ margin: 0, fontSize: '1.1rem' }}>{division.name}</h2>
                <span style={{ fontSize: '0.8rem', color: '#718096' }}>
                  {division.participants.length} member{division.participants.length !== 1 ? 's' : ''}
                </span>
              </div>

              {/* Member list */}
              {division.participants.length > 0 ? (
                <table className="table" style={{ marginBottom: '0.75rem' }}>
                  <thead>
                    <tr>
                      <th>Team</th>
                      <th>Name</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {division.participants.map(p => (
                      <tr key={p.id}>
                        <td>{p.teamName}</td>
                        <td>{p.name}</td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => handleRemoveParticipant(division.id, p.id)}
                            disabled={saving}
                          >
                            Remove
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p style={{ color: '#a0aec0', fontSize: '0.875rem', marginBottom: '0.75rem' }}>
                  No members yet.
                </p>
              )}

              {/* Add participant row */}
              {addingTo === division.id ? (
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  <select
                    className="form-select"
                    value={selectedParticipant}
                    onChange={e => setSelectedParticipant(e.target.value)}
                    style={{ flex: 1, maxWidth: '300px' }}
                  >
                    <option value="">Select participant…</option>
                    {getUnassignedParticipants(division).map(p => (
                      <option key={p.id} value={p.id}>{p.teamName} — {p.name}</option>
                    ))}
                  </select>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => handleAddParticipant(division.id)}
                    disabled={saving || !selectedParticipant}
                  >
                    Add
                  </button>
                  <button
                    className="btn btn-sm"
                    onClick={() => { setAddingTo(null); setSelectedParticipant(''); }}
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  className="btn btn-sm"
                  onClick={() => { setAddingTo(division.id); setSelectedParticipant(''); }}
                >
                  + Add Participant
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default DivisionsPage;
