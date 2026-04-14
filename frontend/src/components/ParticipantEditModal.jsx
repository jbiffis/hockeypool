import { useState, useEffect } from 'react';
import { Modal, TextInput, Stack, Group, Button } from '@mantine/core';

function ParticipantEditModal({ participant, opened, onClose, onSave }) {
  const [form, setForm] = useState({ name: '', email: '', teamName: '', division: '' });

  useEffect(() => {
    if (participant) {
      setForm({
        name: participant.name || '',
        email: participant.email || '',
        teamName: participant.teamName || '',
        division: participant.division || '',
      });
    }
  }, [participant]);

  function handleSubmit(e) {
    e.preventDefault();
    onSave(participant.id, form);
  }

  return (
    <Modal opened={opened} onClose={onClose} title="Edit Participant" centered>
      <form onSubmit={handleSubmit}>
        <Stack gap="sm">
          <TextInput label="Name" value={form.name} onChange={(e) => setForm(p => ({ ...p, name: e.target.value }))} />
          <TextInput label="Email" value={form.email} onChange={(e) => setForm(p => ({ ...p, email: e.target.value }))} required />
          <TextInput label="Team Name" value={form.teamName} onChange={(e) => setForm(p => ({ ...p, teamName: e.target.value }))} />
          <TextInput label="Division" value={form.division} onChange={(e) => setForm(p => ({ ...p, division: e.target.value }))} />
          <Group justify="flex-end">
            <Button variant="default" onClick={onClose}>Cancel</Button>
            <Button type="submit">Save</Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}

export default ParticipantEditModal;
