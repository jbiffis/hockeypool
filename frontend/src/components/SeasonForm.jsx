import { useState, useEffect } from 'react';
import { TextInput, NumberInput, Select, Textarea, Group, Button, Stack, Anchor } from '@mantine/core';

function SeasonForm({ season, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    year: new Date().getFullYear(),
    status: 'archived',
    signupContent: '',
  });

  useEffect(() => {
    if (season) {
      setForm({
        name: season.name || '',
        year: season.year || new Date().getFullYear(),
        status: season.status || 'archived',
        signupContent: season.signupContent || '',
      });
    }
  }, [season]);

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({ ...form, year: Number(form.year) });
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <Group grow>
          <TextInput label="Name" value={form.name} onChange={(e) => setForm(p => ({ ...p, name: e.target.value }))} required placeholder="e.g. 2024/2025" />
          <NumberInput label="Year" value={form.year} onChange={(val) => setForm(p => ({ ...p, year: val }))} required />
          <Select
            label="Status"
            value={form.status}
            onChange={(val) => setForm(p => ({ ...p, status: val }))}
            data={[
              { value: 'active', label: 'Active' },
              { value: 'archived', label: 'Archived' },
            ]}
          />
        </Group>
        <Group gap="xs" align="flex-end">
          <Textarea
            label="Signup Page Content (Markdown)"
            value={form.signupContent}
            onChange={(e) => setForm(p => ({ ...p, signupContent: e.target.value }))}
            minRows={8}
            placeholder="Write the signup page content in Markdown."
            style={{ flex: 1, fontFamily: 'monospace' }}
          />
        </Group>
        {season && (
          <Anchor href={`/season/${season.id}/signup`} target="_blank" size="sm">
            View Signup Page
          </Anchor>
        )}
        <Group>
          <Button type="submit">{season ? 'Update' : 'Create'}</Button>
          <Button variant="default" onClick={onCancel}>Cancel</Button>
        </Group>
      </Stack>
    </form>
  );
}

export default SeasonForm;
