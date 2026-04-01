import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getParticipantResponses } from '../api/participants';

function ParticipantDetailPage() {
  const { participantId } = useParams();
  const navigate = useNavigate();
  const [responses, setResponses] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [participantId]);

  async function fetchData() {
    try {
      const res = await getParticipantResponses(participantId);
      setResponses(res.data);
    } catch (err) {
      setError('Failed to load responses');
    }
  }

  const participant = responses.length > 0 ? responses[0] : null;
  const overallTotal = responses.reduce((sum, r) => sum + (r.roundPointsTotal || 0), 0);
  const hasAnyScores = responses.some(r => r.roundPointsTotal != null);

  return (
    <div>
      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/admin/participants')}>
        &larr; Back to Participants
      </button>

      <h1>{participant ? `${participant.participantName} - ${participant.teamName}` : 'Loading...'}</h1>
      {participant && <p className="text-muted">{participant.email}</p>}
      {hasAnyScores && (
        <div style={{ background: '#d2e3fc', color: '#1967d2', padding: '0.75rem 1rem', borderRadius: '8px', marginBottom: '1rem', fontWeight: 700, fontSize: '1.1rem' }}>
          Overall Total: {overallTotal} pts
        </div>
      )}

      {error && <div className="error-message">{error}</div>}

      {responses.length === 0 && !error && (
        <p className="empty-message">No responses found for this participant.</p>
      )}

      {responses.map((r) => (
        <div key={r.roundId} className="form-card" style={{ marginBottom: '1.5rem' }}>
          <div className="page-header">
            <h2>{r.roundName}</h2>
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              {r.roundPointsTotal != null && (
                <strong>{r.roundPointsTotal} pts</strong>
              )}
              <span className="text-muted" style={{ fontSize: '0.85rem' }}>
                Submitted {r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '--'}
              </span>
            </div>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Question</th>
                <th>Correct Answer</th>
                <th>Response</th>
                <th>Point Value</th>
                <th>Points Scored</th>
              </tr>
            </thead>
            <tbody>
              {(r.answers || []).map((a, i) => (
                <tr key={i}>
                  <td>{a.questionTitle}</td>
                  <td style={{ color: '#999' }}>{a.correctAnswerText ?? 'N/A'}</td>
                  <td style={
                    a.correctAnswerText != null && a.selectedOptionText === a.correctAnswerText
                      ? { color: '#137333', fontWeight: 500 }
                      : a.correctAnswerText != null && a.selectedOptionText && a.selectedOptionText !== a.correctAnswerText
                      ? { color: '#d93025' }
                      : {}
                  }>
                    {a.selectedOptionText || a.freeFormValue || '--'}
                  </td>
                  <td>{a.optionPointValue ?? '--'}</td>
                  <td>{a.pointsEarned ?? '--'}</td>
                </tr>
              ))}
              {(!r.answers || r.answers.length === 0) && (
                <tr><td colSpan={5} className="empty-message">No answers.</td></tr>
              )}
            </tbody>
            <tfoot>
              <tr style={{ fontWeight: 'bold' }}>
                <td colSpan={4} style={{ textAlign: 'right' }}>Round Total</td>
                <td>{r.roundPointsTotal ?? '--'}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      ))}
    </div>
  );
}

export default ParticipantDetailPage;
