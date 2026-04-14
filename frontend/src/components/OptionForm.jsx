import { useState, useEffect } from 'react';
import { TextInput, NumberInput, Group, Button, Stack } from '@mantine/core';

function OptionForm({ option, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    optionText: '',
    subtext: '',
    imageUrl: '',
    displayOrder: 0,
    points: '',
  });

  useEffect(() => {
    if (option) {
      setForm({
        optionText: option.optionText || '',
        subtext: option.subtext || '',
        imageUrl: option.imageUrl || '',
        displayOrder: option.displayOrder ?? 0,
        points: option.points ?? '',
      });
    }
  }, [option]);

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      ...form,
      displayOrder: Number(form.displayOrder),
      points: form.points !== '' ? Number(form.points) : null,
      subtext: form.subtext || null,
      imageUrl: form.imageUrl || null,
    });
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput label="Option text" value={form.optionText} onChange={(e) => setForm(p => ({ ...p, optionText: e.target.value }))} required />
        <TextInput label="Subtext" value={form.subtext} onChange={(e) => setForm(p => ({ ...p, subtext: e.target.value }))} placeholder="Optional" />
        <TextInput label="Image URL" value={form.imageUrl} onChange={(e) => setForm(p => ({ ...p, imageUrl: e.target.value }))} placeholder="https://example.com/image.png (optional)" />
        <Group grow>
          <NumberInput label="Order" value={form.displayOrder} onChange={(val) => setForm(p => ({ ...p, displayOrder: val || 0 }))} />
          <NumberInput label="Points" value={form.points} onChange={(val) => setForm(p => ({ ...p, points: val ?? '' }))} />
        </Group>
        <Group>
          <Button type="submit" size="sm">{option ? 'Update' : 'Add'}</Button>
          <Button variant="default" size="sm" onClick={onCancel}>Cancel</Button>
        </Group>
      </Stack>
    </form>
  );
}

export default OptionForm;
