import { useState, useEffect } from 'react';
import { TextInput, Textarea, NumberInput, Select, Group, Button, Stack, Text, Paper } from '@mantine/core';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function RoundForm({ round, allRounds, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    description: '',
    deadline: '',
    displayOrder: 0,
    displayWithRoundId: '',
  });

  useEffect(() => {
    if (round) {
      setForm({
        name: round.name || '',
        description: round.description || '',
        deadline: round.deadline ? round.deadline.slice(0, 16) : '',
        displayOrder: round.displayOrder ?? 0,
        displayWithRoundId: round.displayWithRoundId != null ? String(round.displayWithRoundId) : '',
      });
    }
  }, [round]);

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      ...form,
      displayOrder: Number(form.displayOrder),
      displayWithRoundId: form.displayWithRoundId ? Number(form.displayWithRoundId) : null,
    });
  }

  const dropdownRounds = (allRounds || []).filter(r => !round || r.id !== round.id);

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput label="Name" value={form.name} onChange={(e) => setForm(p => ({ ...p, name: e.target.value }))} required />
        <Textarea label="Description (markdown supported)" value={form.description} onChange={(e) => setForm(p => ({ ...p, description: e.target.value }))} minRows={3} />
        {form.description && (
          <Paper withBorder p="sm" style={{ fontSize: 14 }}>
            <Text size="xs" c="dimmed" mb={4}>Preview</Text>
            <ReactMarkdown remarkPlugins={[remarkGfm]}>{form.description}</ReactMarkdown>
          </Paper>
        )}
        <TextInput label="Deadline (Eastern Time)" type="datetime-local" value={form.deadline} onChange={(e) => setForm(p => ({ ...p, deadline: e.target.value }))} />
        <Group grow>
          <NumberInput label="Display Order" value={form.displayOrder} onChange={(val) => setForm(p => ({ ...p, displayOrder: val || 0 }))} />
          <Select
            label="Display With Round"
            placeholder="-- None --"
            value={form.displayWithRoundId || null}
            onChange={(val) => setForm(p => ({ ...p, displayWithRoundId: val || '' }))}
            data={dropdownRounds.map(r => ({ value: String(r.id), label: r.name }))}
            clearable
          />
        </Group>
        {round && <Group gap="xs"><Text size="sm" fw={500}>Status:</Text><Text size="sm">{round.status}</Text></Group>}
        <Group>
          <Button type="submit">{round ? 'Update Round' : 'Create Round'}</Button>
          <Button variant="default" onClick={onCancel}>Cancel</Button>
        </Group>
      </Stack>
    </form>
  );
}

export default RoundForm;
