import { useState, useEffect } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { getQuestionDetail } from '../api/leaderboard';
import '../Leaderboard.css';

function QuestionPage() {
  const { seasonId, questionId } = useParams();
  const [searchParams] = useSearchParams();
  const fromParticipantId = searchParams.get('from') ? parseInt(searchParams.get('from'), 10) : null;
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getQuestionDetail(questionId)
      .then(res => { setQuestion(res.data); setLoading(false); })
      .catch(() => { setError('Failed to load question'); setLoading(false); });
  }, [questionId]);

  const isDynamic = question && question.correctAnswerText == null;
  const totalPickers = question
    ? question.options.reduce((sum, o) => sum + o.pickers.length, 0)
    : 0;

  return (
    <div className="pd-page">
      <div className="pd-header">
        <div className="pd-header-top">
          {fromParticipantId ? (
            <Link to={`/standings/${seasonId}/participant/${fromParticipantId}`} className="pd-back">&larr; Back to Participant</Link>
          ) : (
            <Link to={`/standings/${seasonId}`} className="pd-back">&larr; Back to Standings</Link>
          )}
        </div>
        {question ? (
          <>
            <h1>{question.title}</h1>
            <div className="pd-team">{question.roundName}</div>
          </>
        ) : (
          <h1>{loading ? 'Loading...' : 'Question'}</h1>
        )}
      </div>

      <div className="pd-container">
        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="lb-loading">Loading question...</div>
        ) : question && (
          <>
            {question.description && (
              <div className="qp-description">{question.description}</div>
            )}

            {question.imageUrl && (
              <img src={question.imageUrl} alt="" className="qp-image" />
            )}

            {!isDynamic && question.correctAnswerText && (
              <div className="qp-correct-banner">
                <span className="qp-correct-label">Correct Answer</span>
                <span className="qp-correct-value">{question.correctAnswerText}</span>
              </div>
            )}

            {isDynamic && (
              <div className="qp-dynamic-banner">
                Variable scoring — points depend on player performance
              </div>
            )}

            <div className="qp-options">
              {question.options.map(opt => {
                const pct = totalPickers > 0
                  ? Math.round((opt.pickers.length / totalPickers) * 100)
                  : 0;
                const fromPicker = fromParticipantId != null
                  ? opt.pickers.find(p => p.participantId === fromParticipantId)
                  : null;
                const isFromPick = fromPicker != null;

                return (
                  <div
                    key={opt.optionId}
                    className={`qp-option ${opt.correct ? 'qp-option-correct' : ''} ${isFromPick ? 'qp-option-from-pick' : ''}`}
                  >
                    <div className="qp-option-main">
                      <div className="qp-option-left">
                        {opt.correct && <span className="qp-check">&#10003;</span>}
                        <span className="qp-option-text">{opt.optionText}</span>
                        {opt.subtext && (
                          <span className="qp-option-subtext">{opt.subtext}</span>
                        )}
                        {opt.imageUrl && (
                          <img src={opt.imageUrl} alt="" className="qp-option-image" />
                        )}
                      </div>
                      <div className="qp-option-right">
                        {isFromPick && (
                          <span className="qp-their-pick">{fromPicker.teamName}'s pick</span>
                        )}
                        {opt.points != null && (
                          <span className="qp-option-points">{opt.points} pts</span>
                        )}
                        <span className="qp-option-count" data-tooltip={
                          opt.pickers.length > 0
                            ? opt.pickers.map(p => `${p.teamName} (${p.name})`).join('\n')
                            : 'No one picked this'
                        }>
                          {opt.pickers.length} {opt.pickers.length === 1 ? 'pick' : 'picks'}
                        </span>
                      </div>
                    </div>
                    <div className="qp-option-bar-track">
                      <div
                        className={`qp-option-bar-fill ${opt.correct ? 'qp-bar-correct' : ''}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default QuestionPage;
