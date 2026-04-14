function MultiSelect({ options, value = [], onChange, name, maxSelections }) {
  const atLimit = maxSelections != null && value.length >= maxSelections;

  const handleToggle = (optId) => {
    if (value.includes(optId)) {
      onChange(value.filter((id) => id !== optId));
    } else if (!atLimit) {
      onChange([...value, optId]);
    }
  };

  return (
    <div>
      {maxSelections != null && (
        <div className="pool-selection-hint">
          Select {value.length} of {maxSelections}
        </div>
      )}
      {options.map((opt) => {
        const checked = value.includes(opt.id);
        const disabled = !checked && atLimit;
        return (
          <div className="pool-option-row" key={opt.id}>
            <input
              type="checkbox"
              id={`${name}-${opt.id}`}
              checked={checked}
              disabled={disabled}
              onChange={() => handleToggle(opt.id)}
            />
            <label htmlFor={`${name}-${opt.id}`} className={disabled ? 'pool-option-disabled' : ''}>
              <span>{opt.optionText}</span>
              {opt.subtext && <span className="pool-option-subtext">{opt.subtext}</span>}
              {opt.imageUrl && <img src={opt.imageUrl} alt="" className="pool-option-image" />}
            </label>
            {opt.points != null && (
              <span className="pool-option-points">{opt.points} pts</span>
            )}
          </div>
        );
      })}
    </div>
  );
}

export default MultiSelect;
