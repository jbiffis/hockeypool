import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSeasons, createSeason, updateSeason, deleteSeason } from '../api/seasons';
import SeasonForm from '../components/SeasonForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';
import { Title, Table, Button, Group, Alert, Card, Anchor } from '@mantine/core';

function SeasonsPage() {
  const navigate = useNavigate();
  const [seasons, setSeasons] = useState([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => { fetchSeasons(); }, []);

  async function fetchSeasons() {
    try { setSeasons((await getSeasons()).data); }
    catch { setError('Failed to load seasons'); }
  }

  async function handleCreate(data) {
    try { await createSeason(data); setShowCreateForm(false); fetchSeasons(); }
    catch { setError('Failed to create season'); }
  }

  async function handleUpdate(id, data) {
    try { await updateSeason(id, data); setEditingId(null); fetchSeasons(); }
    catch { setError('Failed to update season'); }
  }

  async function handleDelete(id) {
    try { await deleteSeason(id); setConfirmDelete(null); fetchSeasons(); }
    catch { setError('Failed to delete season'); }
  }

  return (
    <div>
      <Group justify="space-between" mb="md">
        <Title order={1}>Seasons</Title>
        {!showCreateForm && <Button onClick={() => setShowCreateForm(true)}>Create Season</Button>}
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      {showCreateForm && (
        <Card withBorder mb="md" padding="md">
          <Title order={3} mb="sm">New Season</Title>
          <SeasonForm onSubmit={handleCreate} onCancel={() => setShowCreateForm(false)} />
        </Card>
      )}

      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Name</Table.Th>
            <Table.Th>Year</Table.Th>
            <Table.Th>Status</Table.Th>
            <Table.Th>Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {seasons.map(season =>
            editingId === season.id ? (
              <Table.Tr key={season.id}>
                <Table.Td colSpan={4}>
                  <SeasonForm season={season} onSubmit={(data) => handleUpdate(season.id, data)} onCancel={() => setEditingId(null)} />
                </Table.Td>
              </Table.Tr>
            ) : (
              <Table.Tr key={season.id}>
                <Table.Td>
                  <Anchor component="button" onClick={() => navigate(`/admin/rounds?seasonId=${season.id}`)}>
                    {season.name}
                  </Anchor>
                </Table.Td>
                <Table.Td>{season.year}</Table.Td>
                <Table.Td><StatusBadge status={season.status} /></Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Button size="compact-sm" variant="default" onClick={() => setEditingId(season.id)}>Edit</Button>
                    <Button size="compact-sm" color="red" variant="light" onClick={() => setConfirmDelete(season.id)}>Delete</Button>
                  </Group>
                </Table.Td>
              </Table.Tr>
            )
          )}
          {seasons.length === 0 && (
            <Table.Tr><Table.Td colSpan={4} ta="center" c="dimmed">No seasons yet.</Table.Td></Table.Tr>
          )}
        </Table.Tbody>
      </Table>

      <ConfirmDialog isOpen={confirmDelete !== null} message="Are you sure you want to delete this season?" onConfirm={() => handleDelete(confirmDelete)} onCancel={() => setConfirmDelete(null)} />
    </div>
  );
}

export default SeasonsPage;
