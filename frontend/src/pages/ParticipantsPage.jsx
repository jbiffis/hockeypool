import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getParticipants } from '../api/participants';
import { getSeasons } from '../api/seasons';

function ParticipantsPage() {
  const navigate = useNavigate();
  const [participants, setParticipants] = useState([]);
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      const active = res.data.find(s => s.status === 'active');
      if (active) setSelectedSeasonId(active.id);
      else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => {
    if (selectedSeasonId) fetchParticipants();
  }, [selectedSeasonId]);

  async function fetchParticipants() {
    try {
      const res = await getParticipants(selectedSeasonId);
      setParticipants(res.data);
    } catch (err) {
      setError('Failed to load participants');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Participants</h1>
        <select
          value={selectedSeasonId || ''}
          onChange={(e) => setSelectedSeasonId(Number(e.target.value))}
          className="form-select"
        >
          {seasons.map(s => (
            <option key={s.id} value={s.id}>{s.name}</option>
          ))}
        </select>
      </div>

      {error && <div className="error-message">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Team Name</th>
            <th>Email</th>
            <th>Division</th>
            <th>Paid</th>
          </tr>
        </thead>
        <tbody>
          {participants.map(p => (
            <tr key={p.id}>
              <td>
                <a className="link" onClick={() => navigate(`/admin/participants/${p.id}`)}>
                  {p.name}
                </a>
              </td>
              <td>{p.teamName}</td>
              <td>{p.email}</td>
              <td>{p.division}</td>
              <td>{p.paid ? 'Yes' : 'No'}</td>
            </tr>
          ))}
          {participants.length === 0 && (
            <tr><td colSpan={5} className="empty-message">No participants yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default ParticipantsPage;
