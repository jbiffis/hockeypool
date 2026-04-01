import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getQuestion, updateQuestion, getQuestions } from '../api/questions';
import { getOptions, createOption, updateOption, deleteOption, updateOptionPoints } from '../api/options';
import QuestionForm from '../components/QuestionForm';
import OptionForm from '../components/OptionForm';
import ConfirmDialog from '../components/ConfirmDialog';

function QuestionDetailPage() {
  const { roundId, questionId } = useParams();
  const navigate = useNavigate();
  const [question, setQuestion] = useState(null);
  const [allQuestions, setAllQuestions] = useState([]);
  const [options, setOptions] = useState([]);
  const [showOptionForm, setShowOptionForm] = useState(false);
  const [editingOptionId, setEditingOptionId] = useState(null);
  const [pointsValues, setPointsValues] = useState({});
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [roundId, questionId]);

  async function fetchData() {
    try {
      const [questionRes, questionsRes, optionsRes] = await Promise.all([
        getQuestion(roundId, questionId),
        getQuestions(roundId),
        getOptions(questionId),
      ]);
      setQuestion(questionRes.data);
      setAllQuestions(questionsRes.data);
      setOptions(optionsRes.data);
      // Initialize points values
      const pts = {};
      optionsRes.data.forEach(o => { pts[o.id] = o.points ?? ''; });
      setPointsValues(pts);
    } catch (err) {
      setError('Failed to load question data');
    }
  }

  async function handleUpdateQuestion(data) {
    try {
      await updateQuestion(roundId, questionId, data);
      fetchData();
    } catch (err) {
      setError('Failed to update question');
    }
  }

  async function handleCreateOption(data) {
    try {
      await createOption(questionId, data);
      setShowOptionForm(false);
      fetchData();
    } catch (err) {
      setError('Failed to create option');
    }
  }

  async function handleUpdateOption(optionId, data) {
    try {
      await updateOption(questionId, optionId, data);
      setEditingOptionId(null);
      fetchData();
    } catch (err) {
      setError('Failed to update option');
    }
  }

  async function handleDeleteOption(optionId) {
    try {
      await deleteOption(questionId, optionId);
      setConfirmDelete(null);
      fetchData();
    } catch (err) {
      setError('Failed to delete option');
    }
  }

  async function handleSavePoints(optionId) {
    try {
      const points = pointsValues[optionId] !== '' ? Number(pointsValues[optionId]) : null;
      await updateOptionPoints(questionId, optionId, points);
      fetchData();
    } catch (err) {
      setError('Failed to update points');
    }
  }

  function handlePointsChange(optionId, value) {
    setPointsValues(prev => ({ ...prev, [optionId]: value }));
  }

  if (!question) return <p>Loading...</p>;

  return (
    <div>
      <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/admin/rounds/${roundId}`)}>
        &larr; Back to Round
      </button>

      <h1>Question: {question.title}</h1>

      {error && <div className="error-message">{error}</div>}

      <div className="form-card">
        <h2>Edit Question</h2>
        <QuestionForm
          question={question}
          allQuestions={allQuestions}
          onSubmit={handleUpdateQuestion}
          onCancel={() => navigate(`/admin/rounds/${roundId}`)}
        />
      </div>

      <div className="section">
        <div className="page-header">
          <h2>Options</h2>
          {!showOptionForm && (
            <button className="btn btn-primary" onClick={() => setShowOptionForm(true)}>Add Option</button>
          )}
        </div>

        <table className="table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Option Text</th>
              <th>Points</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {options.map(opt => (
              editingOptionId === opt.id ? (
                <tr key={opt.id}>
                  <td colSpan={4}>
                    <OptionForm
                      option={opt}
                      onSubmit={(data) => handleUpdateOption(opt.id, data)}
                      onCancel={() => setEditingOptionId(null)}
                    />
                  </td>
                </tr>
              ) : (
                <tr key={opt.id}>
                  <td>{opt.displayOrder}</td>
                  <td>{opt.optionText}</td>
                  <td>
                    <div className="points-cell">
                      <input
                        type="number"
                        value={pointsValues[opt.id] ?? ''}
                        onChange={(e) => handlePointsChange(opt.id, e.target.value)}
                        style={{ width: '70px' }}
                      />
                      <button className="btn btn-primary btn-sm" onClick={() => handleSavePoints(opt.id)}>
                        Save
                      </button>
                    </div>
                  </td>
                  <td className="actions">
                    <button className="btn btn-secondary btn-sm" onClick={() => setEditingOptionId(opt.id)}>Edit</button>
                    <button className="btn btn-danger btn-sm" onClick={() => setConfirmDelete(opt.id)}>Delete</button>
                  </td>
                </tr>
              )
            ))}
            {options.length === 0 && (
              <tr><td colSpan={4} className="empty-message">No options yet.</td></tr>
            )}
          </tbody>
        </table>

        {showOptionForm && (
          <div className="form-card">
            <h3>New Option</h3>
            <OptionForm
              option={null}
              onSubmit={handleCreateOption}
              onCancel={() => setShowOptionForm(false)}
            />
          </div>
        )}
      </div>

      <ConfirmDialog
        isOpen={confirmDelete !== null}
        message="Are you sure you want to delete this option?"
        onConfirm={() => handleDeleteOption(confirmDelete)}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}

export default QuestionDetailPage;
