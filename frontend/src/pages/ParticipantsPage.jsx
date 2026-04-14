import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getParticipants, updateParticipant, updateParticipantPaid } from '../api/participants';
import { getSeasons } from '../api/seasons';
import { Title, Table, Select, Group, Alert, Anchor, Badge, Text } from '@mantine/core';
import ParticipantEditModal from '../components/ParticipantEditModal';

function ParticipantsPage() {
  const navigate = useNavigate();
  const [participants, setParticipants] = useState([]);
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [error, setError] = useState(null);
  const [editingParticipant, setEditingParticipant] = useState(null);

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      const active = res.data.find(s => s.status === 'active');
      if (active) setSelectedSeasonId(active.id);
      else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => { if (selectedSeasonId) fetchParticipants(); }, [selectedSeasonId]);

  async function fetchParticipants() {
    try { setParticipants((await getParticipants(selectedSeasonId)).data); }
    catch { setError('Failed to load participants'); }
  }

  return (
    <div>
      <Group justify="space-between" mb="md">
        <Group gap="sm" align="baseline">
          <Title order={1}>Participants</Title>
          {participants.length > 0 && <Text c="dimmed" size="lg">({participants.length})</Text>}
        </Group>
        <Select
          value={selectedSeasonId != null ? String(selectedSeasonId) : null}
          onChange={(val) => setSelectedSeasonId(val ? Number(val) : null)}
          data={seasons.map(s => ({ value: String(s.id), label: s.name }))}
          style={{ minWidth: 180 }}
        />
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Name</Table.Th>
            <Table.Th>Team Name</Table.Th>
            <Table.Th>Email</Table.Th>
            <Table.Th>Division</Table.Th>
            <Table.Th>Paid</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {participants.map(p => (
            <Table.Tr key={p.id}>
              <Table.Td>
                <Anchor component="button" onClick={() => navigate(`/admin/participants/${p.id}`)}>
                  {p.name}
                </Anchor>
              </Table.Td>
              <Table.Td>{p.teamName}</Table.Td>
              <Table.Td><Anchor component="button" onClick={() => setEditingParticipant(p)}>{p.email}</Anchor></Table.Td>
              <Table.Td>{p.division}</Table.Td>
              <Table.Td>
                <Badge
                  color={p.paid ? 'green' : 'red'}
                  variant="filled"
                  style={{ cursor: 'pointer' }}
                  onClick={async () => {
                    try {
                      const res = await updateParticipantPaid(p.id, !p.paid);
                      setParticipants(prev => prev.map(x => x.id === p.id ? { ...x, paid: res.data.paid } : x));
                    } catch {
                      setError('Failed to update paid status');
                    }
                  }}
                >
                  {p.paid ? 'Paid' : 'Unpaid'}
                </Badge>
              </Table.Td>
            </Table.Tr>
          ))}
          {participants.length === 0 && (
            <Table.Tr><Table.Td colSpan={5} ta="center" c="dimmed">No participants yet.</Table.Td></Table.Tr>
          )}
        </Table.Tbody>
      </Table>

      <ParticipantEditModal
        participant={editingParticipant}
        opened={editingParticipant != null}
        onClose={() => setEditingParticipant(null)}
        onSave={async (id, data) => {
          try {
            const res = await updateParticipant(id, data);
            setParticipants(prev => prev.map(x => x.id === id ? { ...x, ...res.data } : x));
            setEditingParticipant(null);
          } catch {
            setError('Failed to update participant');
          }
        }}
      />
    </div>
  );
}

export default ParticipantsPage;
