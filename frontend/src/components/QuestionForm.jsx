import { useState, useEffect } from 'react';
import { TextInput, Textarea, Select, NumberInput, Checkbox, Group, Button, Stack } from '@mantine/core';

const QUESTION_TYPES = [
  { value: 'multi_select', label: 'Multi Select' },
  { value: 'free_form', label: 'Free Form' },
  { value: 'jeopardy', label: 'Jeopardy' },
  { value: 'number_of_games', label: 'Number of Games' },
  { value: 'text_box', label: 'Text Box' },
  { value: 'box', label: 'Box' },
];

function QuestionForm({ question, allQuestions, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    imageUrl: '',
    questionType: 'multi_select',
    isMandatory: false,
    displayOrder: 0,
    maxWager: '',
    maxSelections: '',
    points: '',
    parentQuestionId: '',
  });

  useEffect(() => {
    if (question) {
      setForm({
        title: question.title || '',
        description: question.description || '',
        imageUrl: question.imageUrl || '',
        questionType: question.questionType || 'multi_select',
        isMandatory: question.isMandatory ?? false,
        displayOrder: question.displayOrder ?? 0,
        maxWager: question.maxWager ?? '',
        maxSelections: question.maxSelections ?? '',
        points: question.points ?? '',
        parentQuestionId: question.parentQuestionId != null ? String(question.parentQuestionId) : '',
      });
    }
  }, [question]);

  function handleSubmit(e) {
    e.preventDefault();
    const isTextBox = form.questionType === 'text_box';
    const showPoints = form.questionType === 'free_form' || form.questionType === 'number_of_games';
    const data = {
      ...form,
      displayOrder: Number(form.displayOrder),
      imageUrl: isTextBox ? null : (form.imageUrl || null),
      isMandatory: isTextBox ? false : form.isMandatory,
      maxWager: form.questionType === 'jeopardy' && form.maxWager !== '' ? Number(form.maxWager) : null,
      maxSelections: (form.questionType === 'multi_select' || form.questionType === 'box') && form.maxSelections !== '' ? Number(form.maxSelections) : null,
      points: showPoints && form.points !== '' ? Number(form.points) : null,
      parentQuestionId: isTextBox ? null : (form.parentQuestionId ? Number(form.parentQuestionId) : null),
    };
    onSubmit(data);
  }

  const parentOptions = (allQuestions || []).filter(q => !question || q.id !== question.id);
  const isTextBox = form.questionType === 'text_box';

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput label="Title" value={form.title} onChange={(e) => setForm(p => ({ ...p, title: e.target.value }))} required />
        <Textarea label="Description" value={form.description} onChange={(e) => setForm(p => ({ ...p, description: e.target.value }))} minRows={2} />

        {!isTextBox && (
          <TextInput label="Image URL" value={form.imageUrl} onChange={(e) => setForm(p => ({ ...p, imageUrl: e.target.value }))} />
        )}

        <Group grow>
          <Select
            label="Question Type"
            value={form.questionType}
            onChange={(val) => setForm(p => ({ ...p, questionType: val }))}
            data={QUESTION_TYPES}
          />
          <NumberInput label="Display Order" value={form.displayOrder} onChange={(val) => setForm(p => ({ ...p, displayOrder: val || 0 }))} />
        </Group>

        {(form.questionType === 'free_form' || form.questionType === 'number_of_games') && (
          <NumberInput label="Points" value={form.points} onChange={(val) => setForm(p => ({ ...p, points: val ?? '' }))} placeholder="Points value" />
        )}

        {form.questionType === 'jeopardy' && (
          <NumberInput label="Max Wager" value={form.maxWager} onChange={(val) => setForm(p => ({ ...p, maxWager: val ?? '' }))} />
        )}

        {(form.questionType === 'multi_select' || form.questionType === 'box') && (
          <NumberInput
            label={form.questionType === 'box' ? 'Picks per box' : 'Max Selections'}
            value={form.maxSelections}
            onChange={(val) => setForm(p => ({ ...p, maxSelections: val ?? '' }))}
            placeholder={form.questionType === 'box' ? 'Required' : 'Unlimited'}
            min={1}
          />
        )}

        {!isTextBox && (
          <>
            <Select
              label="Parent Question"
              placeholder="-- None --"
              value={form.parentQuestionId || null}
              onChange={(val) => setForm(p => ({ ...p, parentQuestionId: val || '' }))}
              data={parentOptions.map(q => ({ value: String(q.id), label: q.title }))}
              clearable
            />
            <Checkbox label="Mandatory" checked={form.isMandatory} onChange={(e) => setForm(p => ({ ...p, isMandatory: e.currentTarget.checked }))} />
          </>
        )}

        <Group>
          <Button type="submit">{question ? 'Update Question' : 'Create Question'}</Button>
          <Button variant="default" onClick={onCancel}>Cancel</Button>
        </Group>
      </Stack>
    </form>
  );
}

export default QuestionForm;
