import { useState, useEffect } from 'react';

function RoundForm({ round, allRounds, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    description: '',
    deadline: '',
    displayOrder: 0,
    displayWithRoundId: '',
  });

  useEffect(() => {
    if (round) {
      setForm({
        name: round.name || '',
        description: round.description || '',
        deadline: round.deadline ? round.deadline.slice(0, 16) : '',
        displayOrder: round.displayOrder ?? 0,
        displayWithRoundId: round.displayWithRoundId ?? '',
      });
    }
  }, [round]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const data = {
      ...form,
      displayOrder: Number(form.displayOrder),
      displayWithRoundId: form.displayWithRoundId ? Number(form.displayWithRoundId) : null,
    };
    onSubmit(data);
  }

  // Filter out the current round from the dropdown options
  const dropdownRounds = (allRounds || []).filter(r => !round || r.id !== round.id);

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="name">Name</label>
        <input id="name" name="name" type="text" value={form.name} onChange={handleChange} required />
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea id="description" name="description" value={form.description} onChange={handleChange} rows={3} />
      </div>

      <div className="form-group">
        <label htmlFor="deadline">Deadline</label>
        <input id="deadline" name="deadline" type="datetime-local" value={form.deadline} onChange={handleChange} />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="displayOrder">Display Order</label>
          <input id="displayOrder" name="displayOrder" type="number" value={form.displayOrder} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label htmlFor="displayWithRoundId">Display With Round</label>
          <select id="displayWithRoundId" name="displayWithRoundId" value={form.displayWithRoundId} onChange={handleChange}>
            <option value="">-- None --</option>
            {dropdownRounds.map(r => (
              <option key={r.id} value={r.id}>{r.name}</option>
            ))}
          </select>
        </div>
      </div>

      {round && (
        <div className="form-group">
          <label>Status</label>
          <span className="form-static">{round.status}</span>
        </div>
      )}

      <div className="form-actions">
        <button type="submit" className="btn btn-primary">{round ? 'Update Round' : 'Create Round'}</button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

export default RoundForm;
