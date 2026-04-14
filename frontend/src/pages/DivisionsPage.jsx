import { useState, useEffect } from 'react';
import { getSeasons } from '../api/seasons';
import { getParticipants } from '../api/participants';
import { getAdminDivisions, createDivision, addParticipantToDivision, removeParticipantFromDivision } from '../api/divisions';
import { Title, Table, Select, Button, Group, Alert, Card, Text, TextInput, Stack, MultiSelect } from '@mantine/core';

function DivisionsPage() {
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [divisions, setDivisions] = useState([]);
  const [participants, setParticipants] = useState([]);
  const [newDivisionName, setNewDivisionName] = useState('');
  const [addingTo, setAddingTo] = useState(null);
  const [selectedParticipants, setSelectedParticipants] = useState([]);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      const active = res.data.find(s => s.status === 'active');
      if (active) setSelectedSeasonId(active.id);
      else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => {
    if (!selectedSeasonId) return;
    loadDivisions();
    getParticipants(selectedSeasonId).then(res => setParticipants(res.data)).catch(() => setError('Failed to load participants'));
  }, [selectedSeasonId]);

  function loadDivisions() {
    getAdminDivisions(selectedSeasonId).then(res => setDivisions(res.data)).catch(() => setError('Failed to load divisions'));
  }

  async function handleCreateDivision(e) {
    e.preventDefault();
    if (!newDivisionName.trim()) return;
    setSaving(true); setError(null);
    try { await createDivision(selectedSeasonId, newDivisionName.trim()); setNewDivisionName(''); loadDivisions(); }
    catch { setError('Failed to create division'); }
    finally { setSaving(false); }
  }

  async function handleAddParticipants(divisionId) {
    if (!selectedParticipants.length) return;
    setSaving(true); setError(null);
    try {
      await Promise.all(selectedParticipants.map(id => addParticipantToDivision(divisionId, Number(id))));
      setAddingTo(null); setSelectedParticipants([]); loadDivisions();
    } catch { setError('Failed to add participants'); }
    finally { setSaving(false); }
  }

  async function handleRemoveParticipant(divisionId, participantId) {
    setSaving(true); setError(null);
    try { await removeParticipantFromDivision(divisionId, participantId); loadDivisions(); }
    catch { setError('Failed to remove participant'); }
    finally { setSaving(false); }
  }

  function getUnassignedParticipants(division) {
    const assignedIds = new Set(division.participants.map(p => p.id));
    return participants.filter(p => !assignedIds.has(p.id));
  }

  return (
    <div>
      <Group justify="space-between" mb="md">
        <Title order={1}>Divisions</Title>
        <Select
          value={selectedSeasonId != null ? String(selectedSeasonId) : null}
          onChange={(val) => setSelectedSeasonId(val ? Number(val) : null)}
          data={seasons.map(s => ({ value: String(s.id), label: s.name }))}
          style={{ minWidth: 180 }}
        />
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      <Card withBorder mb="lg" padding="md">
        <Title order={3} mb="sm">Create New Division</Title>
        <form onSubmit={handleCreateDivision}>
          <Group>
            <TextInput placeholder="Division name" value={newDivisionName} onChange={(e) => setNewDivisionName(e.target.value)} style={{ flex: 1, maxWidth: 280 }} />
            <Button type="submit" disabled={saving || !newDivisionName.trim()}>Create</Button>
          </Group>
        </form>
      </Card>

      {divisions.length === 0 ? (
        <Text c="dimmed">No divisions yet for this season.</Text>
      ) : (
        <Stack gap="md">
          {divisions.map(division => (
            <Card key={division.id} withBorder padding="md">
              <Group justify="space-between" mb="sm">
                <Title order={3}>{division.name}</Title>
                <Text size="sm" c="dimmed">{division.participants.length} member{division.participants.length !== 1 ? 's' : ''}</Text>
              </Group>

              {division.participants.length > 0 ? (
                <Table striped mb="sm">
                  <Table.Thead>
                    <Table.Tr>
                      <Table.Th>Team</Table.Th>
                      <Table.Th>Name</Table.Th>
                      <Table.Th></Table.Th>
                    </Table.Tr>
                  </Table.Thead>
                  <Table.Tbody>
                    {division.participants.map(p => (
                      <Table.Tr key={p.id}>
                        <Table.Td>{p.teamName}</Table.Td>
                        <Table.Td>{p.name}</Table.Td>
                        <Table.Td ta="right">
                          <Button size="compact-sm" color="red" variant="light" onClick={() => handleRemoveParticipant(division.id, p.id)} disabled={saving}>
                            Remove
                          </Button>
                        </Table.Td>
                      </Table.Tr>
                    ))}
                  </Table.Tbody>
                </Table>
              ) : (
                <Text size="sm" c="dimmed" mb="sm">No members yet.</Text>
              )}

              {addingTo === division.id ? (
                <Stack gap="xs">
                  <MultiSelect
                    data={getUnassignedParticipants(division).map(p => ({ value: String(p.id), label: `${p.teamName} — ${p.name}` }))}
                    value={selectedParticipants}
                    onChange={setSelectedParticipants}
                    placeholder="Select participants"
                    searchable
                  />
                  <Group>
                    <Button size="sm" onClick={() => handleAddParticipants(division.id)} disabled={saving || !selectedParticipants.length}>
                      Add{selectedParticipants.length > 1 ? ` (${selectedParticipants.length})` : ''}
                    </Button>
                    <Button size="sm" variant="default" onClick={() => { setAddingTo(null); setSelectedParticipants([]); }}>Cancel</Button>
                  </Group>
                </Stack>
              ) : (
                <Button size="sm" variant="default" onClick={() => { setAddingTo(division.id); setSelectedParticipants([]); }}>
                  + Add Participant
                </Button>
              )}
            </Card>
          ))}
        </Stack>
      )}
    </div>
  );
}

export default DivisionsPage;
