import { Card, Text, Image, Alert } from '@mantine/core';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import MultiSelect from './MultiSelect';
import FreeFormInput from './FreeFormInput';
import JeopardyInput from './JeopardyInput';
import NumberOfGamesInput from './NumberOfGamesInput';
import BoxInput from './BoxInput';

function QuestionCard({ question, answer = {}, error, onChange, childQuestions = [], answers = {}, errors = {}, onChildChange }) {
  const { id, title, description, imageUrl, questionType, isMandatory, maxWager, maxSelections, options = [] } = question;
  const name = `question-${id}`;

  const renderInput = (q, a, n, mw, ms, opts) => {
    switch (q.questionType) {
      case 'multi_select':
        return (
          <MultiSelect
            options={opts}
            value={a.selectedOptionIds || []}
            onChange={(ids) => (q === question ? onChange({ selectedOptionIds: ids }) : onChildChange(q.id, { selectedOptionIds: ids }))}
            name={n}
            maxSelections={ms}
          />
        );
      case 'free_form':
        return (
          <FreeFormInput
            value={a.freeFormValue || ''}
            onChange={(text) => (q === question ? onChange({ freeFormValue: text }) : onChildChange(q.id, { freeFormValue: text }))}
          />
        );
      case 'jeopardy':
        return (
          <JeopardyInput
            options={opts}
            selectedOptionId={a.selectedOptionId || null}
            wagerValue={a.freeFormValue || ''}
            maxWager={mw}
            onOptionChange={(optionId) => {
              const update = { ...a, selectedOptionId: optionId };
              q === question ? onChange(update) : onChildChange(q.id, update);
            }}
            onWagerChange={(val) => {
              const update = { ...a, freeFormValue: val };
              q === question ? onChange(update) : onChildChange(q.id, update);
            }}
            name={n}
          />
        );
      case 'number_of_games':
        return (
          <NumberOfGamesInput
            value={a.freeFormValue || ''}
            onChange={(val) => (q === question ? onChange({ freeFormValue: val }) : onChildChange(q.id, { freeFormValue: val }))}
            name={n}
          />
        );
      case 'box':
        return (
          <BoxInput
            options={opts}
            value={a.selectedOptionIds || []}
            onChange={(ids) => (q === question ? onChange({ selectedOptionIds: ids }) : onChildChange(q.id, { selectedOptionIds: ids }))}
            maxSelections={ms}
          />
        );
      case 'text_box':
        return null;
      default:
        return <Text c="red" size="sm">Unknown question type: {q.questionType}</Text>;
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
      {description && <div style={{ fontSize: 14, color: 'var(--mantine-color-dark-3)', marginBottom: 8 }}><ReactMarkdown remarkPlugins={[remarkGfm]}>{description}</ReactMarkdown></div>}
      {imageUrl && <Image src={imageUrl} alt={title} radius="sm" maw={400} mb="sm" />}
      {renderInput(question, answer, name, maxWager, maxSelections, options)}
      {error && <Alert color="red" mt="xs">{error}</Alert>}

      {childQuestions.map((child) => {
        const childAnswer = answers[child.id] || {};
        const childError = errors[child.id] || null;
        return (
          <div key={child.id} id={`question-${child.id}`}>
            <Text fw={600} size="sm" mb={child.description ? 4 : 'xs'} mt="sm">
              {child.title}
              {child.isMandatory && <Text span c="red" ml={4}>*</Text>}
            </Text>
            {child.description && <div style={{ fontSize: 12, color: 'var(--mantine-color-dark-3)', marginBottom: 4 }}><ReactMarkdown remarkPlugins={[remarkGfm]}>{child.description}</ReactMarkdown></div>}
            {renderInput(child, childAnswer, `question-${child.id}`, child.maxWager, child.maxSelections, child.options || [])}
            {childError && <Alert color="red" mt="xs">{childError}</Alert>}
          </div>
        );
      })}
    </Card>
  );
}

export default QuestionCard;
