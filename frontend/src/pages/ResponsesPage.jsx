import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRound, getResponsesByRound } from '../api/rounds';

function ResponsesPage() {
  const { roundId } = useParams();
  const navigate = useNavigate();
  const [round, setRound] = useState(null);
  const [responses, setResponses] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [roundId]);

  async function fetchData() {
    try {
      const [roundRes, responsesRes] = await Promise.all([
        getRound(roundId),
        getResponsesByRound(roundId),
      ]);
      setRound(roundRes.data);
      setResponses(responsesRes.data);
    } catch (err) {
      setError('Failed to load responses');
    }
  }

  return (
    <div>
      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/admin/rounds')}>
        &larr; Back to Rounds
      </button>

      <h1>Responses: {round?.name || 'Loading...'}</h1>
      <p className="text-muted">{responses.length} submissions</p>

      {error && <div className="error-message">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Participant</th>
            <th>Team Name</th>
            <th>Submitted</th>
            <th>Answers</th>
          </tr>
        </thead>
        <tbody>
          {responses.map((r) => (
            <tr key={r.participantId} className="response-row" onClick={() => setExpandedId(expandedId === r.participantId ? null : r.participantId)}>
              <td>
                <a className="link" onClick={(e) => { e.stopPropagation(); navigate(`/admin/participants/${r.participantId}`); }}>
                  {r.participantName}
                </a>
              </td>
              <td>{r.teamName}</td>
              <td>{r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '--'}</td>
              <td>{r.answers?.length || 0} answers</td>
            </tr>
          ))}
          {responses.length === 0 && (
            <tr><td colSpan={4} className="empty-message">No responses yet.</td></tr>
          )}
        </tbody>
      </table>

      {expandedId && responses.filter(r => r.participantId === expandedId).map(r => (
        <div key={r.participantId} className="form-card" style={{ marginTop: '1rem' }}>
          <div className="page-header">
            <h3>{r.participantName} - {r.teamName}</h3>
            {r.roundPointsTotal != null && <strong>{r.roundPointsTotal} pts</strong>}
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Question</th>
                <th>Answer</th>
                <th>Point Value</th>
                <th>Points Scored</th>
              </tr>
            </thead>
            <tbody>
              {(r.answers || []).map((a, i) => (
                <tr key={i}>
                  <td>{a.questionTitle}</td>
                  <td>{a.selectedOptionText || '--'}</td>
                  <td>{a.optionPointValue ?? '--'}</td>
                  <td>{a.pointsEarned ?? '--'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
    </div>
  );
}

export default ResponsesPage;
