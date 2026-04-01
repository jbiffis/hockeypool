import { useState, useEffect } from 'react';

function OptionForm({ option, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    optionText: '',
    subtext: '',
    displayOrder: 0,
  });

  useEffect(() => {
    if (option) {
      setForm({
        optionText: option.optionText || '',
        subtext: option.subtext || '',
        displayOrder: option.displayOrder ?? 0,
      });
    }
  }, [option]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      ...form,
      displayOrder: Number(form.displayOrder),
      subtext: form.subtext || null,
    });
  }

  return (
    <form className="inline-form" onSubmit={handleSubmit}>
      <input
        name="optionText"
        type="text"
        placeholder="Option text"
        value={form.optionText}
        onChange={handleChange}
        required
      />
      <input
        name="subtext"
        type="text"
        placeholder="Subtext (optional)"
        value={form.subtext}
        onChange={handleChange}
      />
      <input
        name="displayOrder"
        type="number"
        placeholder="Order"
        value={form.displayOrder}
        onChange={handleChange}
        style={{ width: '80px' }}
      />
      <button type="submit" className="btn btn-primary btn-sm">
        {option ? 'Update' : 'Add'}
      </button>
      <button type="button" className="btn btn-secondary btn-sm" onClick={onCancel}>
        Cancel
      </button>
    </form>
  );
}

export default OptionForm;
