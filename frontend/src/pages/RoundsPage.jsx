import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getRounds, createRound, updateRound, deleteRound, updateRoundStatus } from '../api/rounds';
import { getSeasons } from '../api/seasons';
import RoundForm from '../components/RoundForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';
import { formatDeadlineEt } from '../utils/deadline';
import { Title, Table, Button, Group, Alert, Card, Select, Anchor } from '@mantine/core';

const STATUS_FORWARD = { draft: 'open', open: 'closed', closed: 'scored' };
const STATUS_BACKWARD = { scored: 'closed', closed: 'open', open: 'draft' };

function RoundsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [rounds, setRounds] = useState([]);
  const [seasons, setSeasons] = useState([]);
  const [selectedSeasonId, setSelectedSeasonId] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const querySeasonId = searchParams.get('seasonId');
    getSeasons().then(res => {
      setSeasons(res.data);
      if (querySeasonId) setSelectedSeasonId(Number(querySeasonId));
      else {
        const active = res.data.find(s => s.status === 'active');
        if (active) setSelectedSeasonId(active.id);
        else if (res.data.length > 0) setSelectedSeasonId(res.data[0].id);
      }
    }).catch(() => setError('Failed to load seasons'));
  }, []);

  useEffect(() => { if (selectedSeasonId) fetchRounds(); }, [selectedSeasonId]);

  async function fetchRounds() {
    try { setRounds((await getRounds(selectedSeasonId)).data); }
    catch { setError('Failed to load rounds'); }
  }

  async function handleCreate(data) {
    try { await createRound({ ...data, seasonId: selectedSeasonId }); setShowCreateForm(false); fetchRounds(); }
    catch { setError('Failed to create round'); }
  }

  async function handleUpdate(id, data) {
    try { await updateRound(id, data); setEditingId(null); fetchRounds(); }
    catch { setError('Failed to update round'); }
  }

  async function handleDelete(id) {
    try { await deleteRound(id); setConfirmDelete(null); fetchRounds(); }
    catch { setError('Failed to delete round'); }
  }

  async function handleStatusChange(id, newStatus) {
    try { await updateRoundStatus(id, newStatus); fetchRounds(); }
    catch { setError('Failed to update status'); }
  }

  return (
    <div>
      <Group justify="space-between" mb="md">
        <Title order={1}>Rounds</Title>
        <Group>
          <Select
            value={selectedSeasonId != null ? String(selectedSeasonId) : null}
            onChange={(val) => setSelectedSeasonId(val ? Number(val) : null)}
            data={seasons.map(s => ({ value: String(s.id), label: s.name }))}
            style={{ minWidth: 180 }}
          />
          {!showCreateForm && <Button onClick={() => setShowCreateForm(true)}>Create Round</Button>}
        </Group>
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      {showCreateForm && (
        <Card withBorder mb="md" padding="md">
          <Title order={3} mb="sm">New Round</Title>
          <RoundForm round={null} allRounds={rounds} onSubmit={handleCreate} onCancel={() => setShowCreateForm(false)} />
        </Card>
      )}

      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Name</Table.Th>
            <Table.Th>Status</Table.Th>
            <Table.Th>Deadline</Table.Th>
            <Table.Th>Order</Table.Th>
            <Table.Th>Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {rounds.map(round =>
            editingId === round.id ? (
              <Table.Tr key={round.id}>
                <Table.Td colSpan={5}>
                  <RoundForm round={round} allRounds={rounds} onSubmit={(data) => handleUpdate(round.id, data)} onCancel={() => setEditingId(null)} />
                </Table.Td>
              </Table.Tr>
            ) : (
              <Table.Tr key={round.id}>
                <Table.Td>
                  <Anchor component="button" onClick={() => navigate(`/admin/rounds/${round.id}`)}>{round.name}</Anchor>
                </Table.Td>
                <Table.Td><StatusBadge status={round.status} /></Table.Td>
                <Table.Td>{round.deadline ? formatDeadlineEt(round.deadline) : '--'}</Table.Td>
                <Table.Td>{round.displayOrder}</Table.Td>
                <Table.Td>
                  <Group gap="xs" wrap="wrap">
                    <Button size="compact-sm" variant="default" onClick={() => setEditingId(round.id)}>Edit</Button>
                    <Button size="compact-sm" color="red" variant="light" onClick={() => setConfirmDelete(round.id)}>Delete</Button>
                    {STATUS_BACKWARD[round.status] && (
                      <Button size="compact-sm" variant="default" onClick={() => handleStatusChange(round.id, STATUS_BACKWARD[round.status])}>
                        ← {STATUS_BACKWARD[round.status]}
                      </Button>
                    )}
                    {STATUS_FORWARD[round.status] && (
                      <Button size="compact-sm" onClick={() => handleStatusChange(round.id, STATUS_FORWARD[round.status])}>
                        {STATUS_FORWARD[round.status]} →
                      </Button>
                    )}
                    <Button size="compact-sm" variant="default" onClick={() => navigate(`/admin/rounds/${round.id}/responses`)}>Responses</Button>
                    <Button size="compact-sm" variant="default" onClick={() => {
                      const url = `${window.location.origin}/pool/season/${selectedSeasonId}/round/${round.id}`;
                      navigator.clipboard.writeText(url);
                      alert('URL copied: ' + url);
                    }}>Share URL</Button>
                  </Group>
                </Table.Td>
              </Table.Tr>
            )
          )}
          {rounds.length === 0 && (
            <Table.Tr><Table.Td colSpan={5} ta="center" c="dimmed">No rounds yet.</Table.Td></Table.Tr>
          )}
        </Table.Tbody>
      </Table>

      <ConfirmDialog isOpen={confirmDelete !== null} message="Are you sure you want to delete this round?" onConfirm={() => handleDelete(confirmDelete)} onCancel={() => setConfirmDelete(null)} />
    </div>
  );
}

export default RoundsPage;
