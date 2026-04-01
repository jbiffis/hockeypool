function FreeFormInput({ value = '', onChange }) {
  return (
    <div className="pool-input-group">
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Your answer"
      />
    </div>
  );
}

export default FreeFormInput;
