import { useState, useEffect } from 'react';

const QUESTION_TYPES = ['multi_select', 'free_form', 'jeopardy'];

function QuestionForm({ question, allQuestions, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    imageUrl: '',
    questionType: 'multi_select',
    isMandatory: false,
    displayOrder: 0,
    maxWager: '',
    maxSelections: '',
    parentQuestionId: '',
  });

  useEffect(() => {
    if (question) {
      setForm({
        title: question.title || '',
        description: question.description || '',
        imageUrl: question.imageUrl || '',
        questionType: question.questionType || 'multi_select',
        isMandatory: question.isMandatory ?? false,
        displayOrder: question.displayOrder ?? 0,
        maxWager: question.maxWager ?? '',
        maxSelections: question.maxSelections ?? '',
        parentQuestionId: question.parentQuestionId ?? '',
      });
    }
  }, [question]);

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const data = {
      ...form,
      displayOrder: Number(form.displayOrder),
      maxWager: form.questionType === 'jeopardy' && form.maxWager !== '' ? Number(form.maxWager) : null,
      maxSelections: form.questionType === 'multi_select' && form.maxSelections !== '' ? Number(form.maxSelections) : null,
      parentQuestionId: form.parentQuestionId ? Number(form.parentQuestionId) : null,
    };
    onSubmit(data);
  }

  // Filter out current question from parent dropdown
  const parentOptions = (allQuestions || []).filter(q => !question || q.id !== question.id);

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="title">Title</label>
        <input id="title" name="title" type="text" value={form.title} onChange={handleChange} required />
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea id="description" name="description" value={form.description} onChange={handleChange} rows={3} />
      </div>

      <div className="form-group">
        <label htmlFor="imageUrl">Image URL</label>
        <input id="imageUrl" name="imageUrl" type="text" value={form.imageUrl} onChange={handleChange} />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="questionType">Question Type</label>
          <select id="questionType" name="questionType" value={form.questionType} onChange={handleChange}>
            {QUESTION_TYPES.map(t => (
              <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="displayOrder">Display Order</label>
          <input id="displayOrder" name="displayOrder" type="number" value={form.displayOrder} onChange={handleChange} />
        </div>
      </div>

      {form.questionType === 'jeopardy' && (
        <div className="form-group">
          <label htmlFor="maxWager">Max Wager</label>
          <input id="maxWager" name="maxWager" type="number" value={form.maxWager} onChange={handleChange} />
        </div>
      )}

      {form.questionType === 'multi_select' && (
        <div className="form-group">
          <label htmlFor="maxSelections">Max Selections</label>
          <input id="maxSelections" name="maxSelections" type="number" min="1" value={form.maxSelections} onChange={handleChange} placeholder="Unlimited" />
        </div>
      )}

      <div className="form-group">
        <label htmlFor="parentQuestionId">Parent Question</label>
        <select id="parentQuestionId" name="parentQuestionId" value={form.parentQuestionId} onChange={handleChange}>
          <option value="">-- None --</option>
          {parentOptions.map(q => (
            <option key={q.id} value={q.id}>{q.title}</option>
          ))}
        </select>
      </div>

      <div className="form-group form-checkbox">
        <label>
          <input type="checkbox" name="isMandatory" checked={form.isMandatory} onChange={handleChange} />
          Mandatory
        </label>
      </div>

      <div className="form-actions">
        <button type="submit" className="btn btn-primary">{question ? 'Update Question' : 'Create Question'}</button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

export default QuestionForm;
