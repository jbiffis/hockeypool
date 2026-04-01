import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRound, updateRound, updateRoundStatus, getRounds } from '../api/rounds';
import { getQuestions, createQuestion, deleteQuestion } from '../api/questions';
import RoundForm from '../components/RoundForm';
import QuestionForm from '../components/QuestionForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';

const STATUS_FORWARD = { draft: 'open', open: 'closed', closed: 'scored' };
const STATUS_BACKWARD = { scored: 'closed', closed: 'open', open: 'draft' };

function RoundDetailPage() {
  const { roundId } = useParams();
  const navigate = useNavigate();
  const [round, setRound] = useState(null);
  const [allRounds, setAllRounds] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [showQuestionForm, setShowQuestionForm] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [roundId]);

  async function fetchData() {
    try {
      const [roundRes, roundsRes, questionsRes] = await Promise.all([
        getRound(roundId),
        getRounds(),
        getQuestions(roundId),
      ]);
      setRound(roundRes.data);
      setAllRounds(roundsRes.data);
      setQuestions(questionsRes.data);
    } catch (err) {
      setError('Failed to load round data');
    }
  }

  async function handleUpdateRound(data) {
    try {
      await updateRound(roundId, data);
      fetchData();
    } catch (err) {
      setError('Failed to update round');
    }
  }

  async function handleCreateQuestion(data) {
    try {
      await createQuestion(roundId, data);
      setShowQuestionForm(false);
      fetchData();
    } catch (err) {
      setError('Failed to create question');
    }
  }

  async function handleDeleteQuestion(questionId) {
    try {
      await deleteQuestion(roundId, questionId);
      setConfirmDelete(null);
      fetchData();
    } catch (err) {
      setError('Failed to delete question');
    }
  }

  if (!round) return <p>Loading...</p>;

  return (
    <div>
      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/admin/rounds')}>
        &larr; Back to Rounds
      </button>

      <div className="page-header">
        <h1>Round: {round.name}</h1>
        <div className="actions">
          <StatusBadge status={round.status} />
          {STATUS_BACKWARD[round.status] && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={async () => { await updateRoundStatus(roundId, STATUS_BACKWARD[round.status]); fetchData(); }}
            >
              ← {STATUS_BACKWARD[round.status]}
            </button>
          )}
          {STATUS_FORWARD[round.status] && (
            <button
              className="btn btn-primary btn-sm"
              onClick={async () => { await updateRoundStatus(roundId, STATUS_FORWARD[round.status]); fetchData(); }}
            >
              {STATUS_FORWARD[round.status]} →
            </button>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="form-card">
        <h2>Edit Round</h2>
        <RoundForm
          round={round}
          allRounds={allRounds}
          onSubmit={handleUpdateRound}
          onCancel={() => navigate('/admin/rounds')}
        />
      </div>

      <div className="section">
        <div className="page-header">
          <h2>Questions</h2>
          {!showQuestionForm && (
            <button className="btn btn-primary" onClick={() => setShowQuestionForm(true)}>Add Question</button>
          )}
        </div>

        {showQuestionForm && (
          <div className="form-card">
            <h3>New Question</h3>
            <QuestionForm
              question={null}
              allQuestions={questions}
              onSubmit={handleCreateQuestion}
              onCancel={() => setShowQuestionForm(false)}
            />
          </div>
        )}

        <table className="table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Title</th>
              <th>Type</th>
              <th>Mandatory</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {questions.map(q => (
              <tr key={q.id}>
                <td>{q.displayOrder}</td>
                <td>
                  <a className="link" onClick={() => navigate(`/admin/rounds/${roundId}/questions/${q.id}`)}>
                    {q.title}
                  </a>
                </td>
                <td>{q.questionType?.replace(/_/g, ' ')}</td>
                <td>{q.isMandatory ? 'Yes' : 'No'}</td>
                <td className="actions">
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => navigate(`/admin/rounds/${roundId}/questions/${q.id}`)}
                  >
                    Edit
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => setConfirmDelete(q.id)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {questions.length === 0 && (
              <tr><td colSpan={5} className="empty-message">No questions yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <ConfirmDialog
        isOpen={confirmDelete !== null}
        message="Are you sure you want to delete this question?"
        onConfirm={() => handleDeleteQuestion(confirmDelete)}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}

export default RoundDetailPage;
