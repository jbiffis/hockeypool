function JeopardyInput({ options, selectedOptionId, wagerValue = '', maxWager, onOptionChange, onWagerChange, name }) {
  return (
    <div>
      {options.map((opt) => (
        <div className="pool-option-row" key={opt.id}>
          <input
            type="radio"
            id={`${name}-${opt.id}`}
            name={name}
            checked={selectedOptionId === opt.id}
            onChange={() => onOptionChange(opt.id)}
          />
          <label htmlFor={`${name}-${opt.id}`}>
            <span>{opt.optionText}</span>
            {opt.subtext && <span className="pool-option-subtext">{opt.subtext}</span>}
            {opt.imageUrl && <img src={opt.imageUrl} alt="" className="pool-option-image" />}
          </label>
        </div>
      ))}
      <div className="pool-wager-group">
        <label htmlFor={`${name}-wager`}>Your wager (max: {maxWager})</label>
        <input
          id={`${name}-wager`}
          type="number"
          min={1}
          max={maxWager}
          value={wagerValue}
          onChange={(e) => onWagerChange(e.target.value)}
        />
      </div>
    </div>
  );
}

export default JeopardyInput;
