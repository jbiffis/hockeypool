import { Card, Text, Image, Alert } from '@mantine/core';
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
      case 'text_box':
        return null;
      default:
        return <Text c="red" size="sm">Unknown question type: {questionType}</Text>;
    }
  };

  const isTextBox = questionType === 'text_box';

  return (
    <Card
      id={`question-${id}`}
      withBorder
      padding="md"
      radius="md"
      bg={isTextBox ? 'gray.0' : undefined}
    >
      <Text fw={600} size="md" mb={description || imageUrl ? 4 : 'sm'}>
        {title}
        {isMandatory && !isTextBox && <Text span c="red" ml={4}>*</Text>}
      </Text>
      {description && <Text size="sm" c="dimmed" mb="sm">{description}</Text>}
      {imageUrl && <Image src={imageUrl} alt={title} radius="sm" maw={400} mb="sm" />}
      {renderInput()}
      {error && <Alert color="red" mt="xs">{error}</Alert>}
    </Card>
  );
}

export default QuestionCard;
