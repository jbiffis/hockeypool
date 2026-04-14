import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRound, getResponsesByRound } from '../api/rounds';
import { Title, Table, Button, Alert, Card, Group, Text, Anchor } from '@mantine/core';

function ResponsesPage() {
  const { roundId } = useParams();
  const navigate = useNavigate();
  const [round, setRound] = useState(null);
  const [responses, setResponses] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => { fetchData(); }, [roundId]);

  async function fetchData() {
    try {
      const [roundRes, responsesRes] = await Promise.all([getRound(roundId), getResponsesByRound(roundId)]);
      setRound(roundRes.data);
      setResponses(responsesRes.data);
    } catch { setError('Failed to load responses'); }
  }

  return (
    <div>
      <Button variant="subtle" size="compact-sm" onClick={() => navigate('/admin/rounds')} mb="sm">
        ← Back to Rounds
      </Button>

      <Title order={1} mb="xs">Responses: {round?.name || 'Loading...'}</Title>
      <Text c="dimmed" mb="md">{responses.length} submissions</Text>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Participant</Table.Th>
            <Table.Th>Team Name</Table.Th>
            <Table.Th>Submitted</Table.Th>
            <Table.Th>Answers</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {responses.map((r) => (
            <Table.Tr key={r.participantId} style={{ cursor: 'pointer' }} onClick={() => setExpandedId(expandedId === r.participantId ? null : r.participantId)}>
              <Table.Td>
                <Anchor component="button" onClick={(e) => { e.stopPropagation(); navigate(`/admin/participants/${r.participantId}`); }}>
                  {r.participantName}
                </Anchor>
              </Table.Td>
              <Table.Td>{r.teamName}</Table.Td>
              <Table.Td>{r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '--'}</Table.Td>
              <Table.Td>{r.answers?.length || 0} answers</Table.Td>
            </Table.Tr>
          ))}
          {responses.length === 0 && (
            <Table.Tr><Table.Td colSpan={4} ta="center" c="dimmed">No responses yet.</Table.Td></Table.Tr>
          )}
        </Table.Tbody>
      </Table>

      {expandedId && responses.filter(r => r.participantId === expandedId).map(r => (
        <Card key={r.participantId} withBorder mt="md" padding="md">
          <Group justify="space-between" mb="sm">
            <Title order={3}>{r.participantName} - {r.teamName}</Title>
            {r.roundPointsTotal != null && <Text fw={700}>{r.roundPointsTotal} pts</Text>}
          </Group>
          <Table striped>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>Question</Table.Th>
                <Table.Th>Answer</Table.Th>
                <Table.Th>Point Value</Table.Th>
                <Table.Th>Points Scored</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {(r.answers || []).map((a, i) => (
                <Table.Tr key={i}>
                  <Table.Td>{a.questionTitle}</Table.Td>
                  <Table.Td>{a.selectedOptionText || '--'}</Table.Td>
                  <Table.Td>{a.optionPointValue ?? '--'}</Table.Td>
                  <Table.Td>{a.pointsEarned ?? '--'}</Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </Card>
      ))}
    </div>
  );
}

export default ResponsesPage;
