import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getPublicParticipantResponses } from '../api/leaderboard';
import '../Leaderboard.css';

function PublicParticipantDetailPage() {
  const { seasonId, participantId } = useParams();
  const [responses, setResponses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getPublicParticipantResponses(participantId)
      .then(res => {
        setResponses(res.data);
        setLoading(false);
      })
      .catch(() => {
        setError('Failed to load responses');
        setLoading(false);
      });
  }, [participantId]);

  const participant = responses.length > 0 ? responses[0] : null;

  const overallTotal = responses.reduce((sum, r) => sum + (r.roundPointsTotal || 0), 0);
  const hasAnyScores = responses.some(r => r.roundPointsTotal != null);

  return (
    <div className="pd-page">
      <div className="pd-header">
        <div className="pd-header-top">
          <Link to={`/standings/${seasonId}`} className="pd-back">&larr; Back to Standings</Link>
        </div>
        {participant ? (
          <>
            <h1>{participant.participantName}</h1>
            <div className="pd-team">{participant.teamName}</div>
          </>
        ) : (
          <h1>{loading ? 'Loading...' : 'Participant'}</h1>
        )}
      </div>

      <div className="pd-container">
        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="lb-loading">Loading responses...</div>
        ) : responses.length === 0 ? (
          <div className="lb-empty">No responses found.</div>
        ) : (
          <>
            {hasAnyScores && (
              <div className="pd-overall-total">
                <span>Overall Total</span>
                <span>{overallTotal} pts</span>
              </div>
            )}

            {responses.map(r => (
              <div key={r.roundId} className="pd-round-card">
                <div className="pd-round-header">
                  <h2>{r.roundName}</h2>
                  {r.roundPointsTotal != null && (
                    <span className="pd-round-total">{r.roundPointsTotal} pts</span>
                  )}
                </div>
                <table className="pd-table">
                  <thead>
                    <tr>
                      <th>Question</th>
                      <th>Your Pick</th>
                      <th>Correct Answer</th>
                      <th style={{ textAlign: 'center' }}>Point Value</th>
                      <th style={{ textAlign: 'center' }}>Points Scored</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(r.answers || []).map((a, i) => {
                      const isCorrect = a.correctAnswerText != null &&
                        a.selectedOptionText === a.correctAnswerText;
                      const isWrong = a.correctAnswerText != null &&
                        a.selectedOptionText != null &&
                        a.selectedOptionText !== a.correctAnswerText;
                      return (
                        <tr key={i}>
                          <td>
                            <Link to={`/standings/${seasonId}/question/${a.questionId}?from=${participantId}`}
                                  style={{ color: '#2b6cb0', textDecoration: 'none' }}>
                              {a.questionTitle}
                            </Link>
                          </td>
                          <td className={isCorrect ? 'pd-correct' : isWrong ? 'pd-incorrect' : ''}>
                            {a.selectedOptionText || a.freeFormValue || '--'}
                          </td>
                          <td className="pd-na">
                            {a.correctAnswerText ?? 'N/A'}
                          </td>
                          <td className="pd-points" style={{ textAlign: 'center' }}>
                            {a.optionPointValue ?? '--'}
                          </td>
                          <td className="pd-points-earned" style={{ textAlign: 'center' }}>
                            {a.pointsEarned ?? '--'}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                  {r.roundPointsTotal != null && (
                    <tfoot>
                      <tr>
                        <td colSpan={4} style={{ textAlign: 'right' }}>Round Total</td>
                        <td style={{ textAlign: 'center' }}>{r.roundPointsTotal}</td>
                      </tr>
                    </tfoot>
                  )}
                </table>
              </div>
            ))}
          </>
        )}
      </div>
    </div>
  );
}

export default PublicParticipantDetailPage;
