import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRound, updateRound, updateRoundStatus, getRounds } from '../api/rounds';
import { getQuestions, createQuestion, updateQuestion, deleteQuestion } from '../api/questions';
import { getOptions } from '../api/options';
import RoundForm from '../components/RoundForm';
import QuestionForm from '../components/QuestionForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';
import PreviewModal from '../components/PreviewModal';

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
  const [showPreview, setShowPreview] = useState(false);
  const [previewQuestions, setPreviewQuestions] = useState([]);
  const [error, setError] = useState(null);

  const dragItem = useRef(null);
  const dragOverItem = useRef(null);

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

  function getNextDisplayOrder() {
    if (questions.length === 0) return 1;
    return Math.max(...questions.map(q => q.displayOrder ?? 0)) + 1;
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

  async function handleDragEnd() {
    const fromIdx = dragItem.current;
    const toIdx = dragOverItem.current;
    dragItem.current = null;
    dragOverItem.current = null;

    if (fromIdx === null || toIdx === null || fromIdx === toIdx) return;

    // Reorder locally first for instant feedback
    const reordered = [...questions];
    const [moved] = reordered.splice(fromIdx, 1);
    reordered.splice(toIdx, 0, moved);

    // Assign sequential display orders
    const updates = reordered.map((q, i) => ({ ...q, displayOrder: i + 1 }));
    setQuestions(updates);

    // Persist only the ones whose order actually changed
    try {
      await Promise.all(
        updates
          .filter((q, i) => q.displayOrder !== questions.find(oq => oq.id === q.id)?.displayOrder)
          .map(q => updateQuestion(roundId, q.id, { ...q }))
      );
      fetchData();
    } catch (err) {
      setError('Failed to reorder questions');
      fetchData();
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

  async function handlePreview() {
    try {
      const withOptions = await Promise.all(
        questions.map(async (q) => {
          const optRes = await getOptions(q.id);
          return { ...q, options: optRes.data };
        })
      );
      setPreviewQuestions(withOptions);
      setShowPreview(true);
    } catch (err) {
      setError('Failed to load preview');
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
          <div className="actions">
            {!showQuestionForm && (
              <button className="btn btn-primary" onClick={() => setShowQuestionForm(true)}>Add Question</button>
            )}
            {questions.length > 0 && (
              <button className="btn btn-secondary" onClick={handlePreview}>Preview</button>
            )}
          </div>
        </div>

        {showQuestionForm && (
          <div className="form-card">
            <h3>New Question</h3>
            <QuestionForm
              question={{ displayOrder: getNextDisplayOrder() }}
              allQuestions={questions}
              onSubmit={handleCreateQuestion}
              onCancel={() => setShowQuestionForm(false)}
            />
          </div>
        )}

        <table className="table">
          <thead>
            <tr>
              <th style={{ width: '30px' }}></th>
              <th>Title</th>
              <th>Type</th>
              <th>Mandatory</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {questions.map((q, idx) => (
              <tr
                key={q.id}
                draggable
                onDragStart={() => { dragItem.current = idx; }}
                onDragEnter={() => { dragOverItem.current = idx; }}
                onDragOver={(e) => e.preventDefault()}
                onDragEnd={handleDragEnd}
                className="draggable-row"
              >
                <td className="drag-handle" title="Drag to reorder">&#9776;</td>
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

      <PreviewModal
        isOpen={showPreview}
        onClose={() => setShowPreview(false)}
        questions={previewQuestions}
        roundName={round.name}
      />
    </div>
  );
}

export default RoundDetailPage;
