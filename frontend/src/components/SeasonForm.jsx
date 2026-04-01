import { useState, useEffect } from 'react';

function SeasonForm({ season, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    year: new Date().getFullYear(),
    status: 'archived',
  });

  useEffect(() => {
    if (season) {
      setForm({
        name: season.name || '',
        year: season.year || new Date().getFullYear(),
        status: season.status || 'archived',
      });
    }
  }, [season]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({ ...form, year: Number(form.year) });
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input id="name" name="name" type="text" value={form.name} onChange={handleChange} required placeholder="e.g. 2024/2025" />
        </div>
        <div className="form-group">
          <label htmlFor="year">Year</label>
          <input id="year" name="year" type="number" value={form.year} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select id="status" name="status" value={form.status} onChange={handleChange}>
            <option value="active">Active</option>
            <option value="archived">Archived</option>
          </select>
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary">{season ? 'Update' : 'Create'}</button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

export default SeasonForm;
