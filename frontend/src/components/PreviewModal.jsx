import QuestionCard from './pool/QuestionCard';

function PreviewModal({ isOpen, onClose, questions, roundName }) {
  if (!isOpen || !questions || questions.length === 0) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="preview-modal" onClick={(e) => e.stopPropagation()}>
        <div className="preview-modal-header">
          <h2>Preview: {roundName || 'Round'}</h2>
          <button className="btn btn-secondary btn-sm" onClick={onClose}>Close</button>
        </div>
        <div className="preview-modal-body">
          {questions.map((q) => (
            <QuestionCard
              key={q.id}
              question={q}
              answer={{}}
              error={null}
              onChange={() => {}}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

export default PreviewModal;
