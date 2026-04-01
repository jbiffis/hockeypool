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
          <label htmlFor={`${name}-${opt.id}`}>{opt.optionText}</label>
        </div>
      ))}
    </div>
  );
}

export default SingleSelect;
