const GAME_OPTIONS = [4, 5, 6, 7];

function NumberOfGamesInput({ value, onChange, name }) {
  return (
    <div>
      {GAME_OPTIONS.map((num) => (
        <div className="pool-option-row" key={num}>
          <input
            type="radio"
            id={`${name}-${num}`}
            name={name}
            checked={value === String(num)}
            onChange={() => onChange(String(num))}
          />
          <label htmlFor={`${name}-${num}`}>{num}</label>
        </div>
      ))}
    </div>
  );
}

export default NumberOfGamesInput;
