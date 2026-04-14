import MultiSelect from './MultiSelect';
import FreeFormInput from './FreeFormInput';
import JeopardyInput from './JeopardyInput';
import NumberOfGamesInput from './NumberOfGamesInput';

function QuestionCard({ question, answer = {}, error, onChange }) {
  const { id, title, description, imageUrl, questionType, isMandatory, maxWager, maxSelections, options = [] } = question;
  const name = `question-${id}`;

  const renderInput = () => {
    switch (questionType) {
      case 'multi_select':
        return (
          <MultiSelect
            options={options}
            value={answer.selectedOptionIds || []}
            onChange={(ids) => onChange({ selectedOptionIds: ids })}
            name={name}
            maxSelections={maxSelections}
          />
        );
      case 'free_form':
        return (
          <FreeFormInput
            value={answer.freeFormValue || ''}
            onChange={(text) => onChange({ freeFormValue: text })}
          />
        );
      case 'jeopardy':
        return (
          <JeopardyInput
            options={options}
            selectedOptionId={answer.selectedOptionId || null}
            wagerValue={answer.freeFormValue || ''}
            maxWager={maxWager}
            onOptionChange={(optionId) => onChange({ ...answer, selectedOptionId: optionId })}
            onWagerChange={(val) => onChange({ ...answer, freeFormValue: val })}
            name={name}
          />
        );
      case 'number_of_games':
        return (
          <NumberOfGamesInput
            value={answer.freeFormValue || ''}
            onChange={(val) => onChange({ freeFormValue: val })}
            name={name}
          />
        );
      default:
        return <p>Unknown question type: {questionType}</p>;
    }
  };

  return (
    <div className="pool-question-card" id={`question-${id}`}>
      <div className="pool-question-title">
        {title}
        {isMandatory && <span className="pool-required">*</span>}
      </div>
      {description && <div className="pool-question-desc">{description}</div>}
      {imageUrl && <img className="pool-question-image" src={imageUrl} alt={title} />}
      {renderInput()}
      {error && <div className="pool-field-error">{error}</div>}
    </div>
  );
}

export default QuestionCard;
