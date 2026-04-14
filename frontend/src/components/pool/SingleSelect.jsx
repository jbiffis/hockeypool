function SingleSelect({ options, value, onChange, name }) {
  return (
    <div>
      {options.map((opt) => (
        <div className="pool-option-row" key={opt.id}>
          <input
            type="radio"
            id={`${name}-${opt.id}`}
            name={name}
            checked={value === opt.id}
            onChange={() => onChange(opt.id)}
          />
          <label htmlFor={`${name}-${opt.id}`}>
            <span>{opt.optionText}</span>
            {opt.imageUrl && <img src={opt.imageUrl} alt="" className="pool-option-image" />}
          </label>
        </div>
      ))}
    </div>
  );
}

export default SingleSelect;
