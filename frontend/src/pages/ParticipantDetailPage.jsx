import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getParticipantResponses, deleteParticipantResponse } from '../api/participants';
import { Title, Table, Button, Group, Alert, Card, Text, Badge } from '@mantine/core';

function ParticipantDetailPage() {
  const { participantId } = useParams();
  const navigate = useNavigate();
  const [responses, setResponses] = useState([]);
  const [error, setError] = useState(null);
  const [deleting, setDeleting] = useState(null);

  useEffect(() => { fetchData(); }, [participantId]);

  async function fetchData() {
    try { setResponses((await getParticipantResponses(participantId)).data); }
    catch { setError('Failed to load responses'); }
  }

  async function handleDeleteResponse(roundId, roundName) {
    if (!window.confirm(`Delete ${roundName} response for this participant? This cannot be undone.`)) return;
    setDeleting(roundId);
    try {
      await deleteParticipantResponse(participantId, roundId);
      await fetchData();
    } catch {
      setError('Failed to delete response');
    } finally {
      setDeleting(null);
    }
  }

  const participant = responses.length > 0 ? responses[0] : null;
  const overallTotal = responses.reduce((sum, r) => sum + (r.roundPointsTotal || 0), 0);
  const hasAnyScores = responses.some(r => r.roundPointsTotal != null);

  return (
    <div>
      <Button variant="subtle" size="compact-sm" onClick={() => navigate('/admin/participants')} mb="sm">
        ← Back to Participants
      </Button>

      <Title order={1} mb="xs">
        {participant ? `${participant.participantName} - ${participant.teamName}` : 'Loading...'}
      </Title>
      {participant && <Text c="dimmed" mb="sm">{participant.email}</Text>}
      {hasAnyScores && (
        <Badge size="lg" variant="light" color="blue" mb="md">Overall Total: {overallTotal} pts</Badge>
      )}

      {error && <Alert color="red" mb="md">{error}</Alert>}
      {responses.length === 0 && !error && <Text c="dimmed">No responses found for this participant.</Text>}

      {responses.map((r) => (
        <Card key={r.roundId} withBorder mb="md" padding="md">
          <Group justify="space-between" mb="sm">
            <Title order={3}>{r.roundName}</Title>
            <Group>
              {r.roundPointsTotal != null && <Badge variant="light">{r.roundPointsTotal} pts</Badge>}
              <Text size="xs" c="dimmed">Submitted {r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '--'}</Text>
              {r.submittedAt && (
                <Button
                  size="compact-sm"
                  color="red"
                  variant="subtle"
                  loading={deleting === r.roundId}
                  onClick={() => handleDeleteResponse(r.roundId, r.roundName)}
                >
                  Delete
                </Button>
              )}
            </Group>
          </Group>
          <Table striped>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>Question</Table.Th>
                <Table.Th>Correct Answer</Table.Th>
                <Table.Th>Response</Table.Th>
                <Table.Th>Point Value</Table.Th>
                <Table.Th>Points Scored</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {(r.answers || []).map((a, i) => {
                const isCorrect = a.correctAnswerText != null && a.selectedOptionText === a.correctAnswerText;
                const isWrong = a.correctAnswerText != null && a.selectedOptionText && a.selectedOptionText !== a.correctAnswerText;
                return (
                  <Table.Tr key={i}>
                    <Table.Td>{a.questionTitle}</Table.Td>
                    <Table.Td c="dimmed">{a.correctAnswerText ?? 'N/A'}</Table.Td>
                    <Table.Td c={isCorrect ? 'green' : isWrong ? 'red' : undefined} fw={isCorrect ? 500 : undefined}>
                      {a.selectedOptionText || a.freeFormValue || '--'}
                    </Table.Td>
                    <Table.Td>{a.optionPointValue ?? '--'}</Table.Td>
                    <Table.Td>{a.pointsEarned ?? '--'}</Table.Td>
                  </Table.Tr>
                );
              })}
            </Table.Tbody>
            <Table.Tfoot>
              <Table.Tr fw={700}>
                <Table.Td colSpan={4} ta="right">Round Total</Table.Td>
                <Table.Td>{r.roundPointsTotal ?? '--'}</Table.Td>
              </Table.Tr>
            </Table.Tfoot>
          </Table>
        </Card>
      ))}
    </div>
  );
}

export default ParticipantDetailPage;
