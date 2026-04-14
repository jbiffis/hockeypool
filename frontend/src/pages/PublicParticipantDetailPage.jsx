import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getPublicParticipantResponses } from '../api/leaderboard';
import { Container, Title, Table, Card, Group, Text, Badge, Anchor, Alert, Center } from '@mantine/core';

function PublicParticipantDetailPage() {
  const { seasonId, participantId } = useParams();
  const [responses, setResponses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getPublicParticipantResponses(participantId)
      .then(res => { setResponses(res.data); setLoading(false); })
      .catch(() => { setError('Failed to load responses'); setLoading(false); });
  }, [participantId]);

  const participant = responses.length > 0 ? responses[0] : null;
  const overallTotal = responses.reduce((sum, r) => sum + (r.roundPointsTotal || 0), 0);
  const hasAnyScores = responses.some(r => r.roundPointsTotal != null);

  return (
    <Container size="md" py="xl">
      <Anchor component={Link} to={`/standings/${seasonId}`} size="sm" mb="md" display="block">← Back to Standings</Anchor>

      {participant ? (
        <>
          <Title order={1}>{participant.participantName}</Title>
          <Text c="dimmed" mb="md">{participant.teamName}</Text>
        </>
      ) : (
        <Title order={1}>{loading ? 'Loading...' : 'Participant'}</Title>
      )}

      {error && <Alert color="red" mb="md">{error}</Alert>}

      {loading ? (
        <Center py="xl"><Text c="dimmed">Loading responses...</Text></Center>
      ) : responses.length === 0 ? (
        <Text c="dimmed">No responses found.</Text>
      ) : (
        <>
          {hasAnyScores && (
            <Badge size="lg" variant="light" color="blue" mb="lg">Overall Total: {overallTotal} pts</Badge>
          )}

          {responses.map(r => (
            <Card key={r.roundId} withBorder mb="md" padding="md">
              <Group justify="space-between" mb="sm">
                <Title order={3}>{r.roundName}</Title>
                {r.roundPointsTotal != null && <Badge variant="light">{r.roundPointsTotal} pts</Badge>}
              </Group>
              <Table striped>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>Question</Table.Th>
                    <Table.Th>Your Pick</Table.Th>
                    <Table.Th>Correct Answer</Table.Th>
                    <Table.Th ta="center">Point Value</Table.Th>
                    <Table.Th ta="center">Points Scored</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {(r.answers || []).map((a, i) => {
                    const isCorrect = a.correctAnswerText != null && a.selectedOptionText === a.correctAnswerText;
                    const isWrong = a.correctAnswerText != null && a.selectedOptionText != null && a.selectedOptionText !== a.correctAnswerText;
                    return (
                      <Table.Tr key={i}>
                        <Table.Td>
                          <Anchor component={Link} to={`/standings/${seasonId}/question/${a.questionId}?from=${participantId}`} size="sm">
                            {a.questionTitle}
                          </Anchor>
                        </Table.Td>
                        <Table.Td c={isCorrect ? 'green' : isWrong ? 'red' : undefined} fw={isCorrect ? 500 : undefined}>
                          {a.selectedOptionText || a.freeFormValue || '--'}
                        </Table.Td>
                        <Table.Td c="dimmed">{a.correctAnswerText ?? 'N/A'}</Table.Td>
                        <Table.Td ta="center">{a.optionPointValue ?? '--'}</Table.Td>
                        <Table.Td ta="center">{a.pointsEarned ?? '--'}</Table.Td>
                      </Table.Tr>
                    );
                  })}
                </Table.Tbody>
                {r.roundPointsTotal != null && (
                  <Table.Tfoot>
                    <Table.Tr fw={700}>
                      <Table.Td colSpan={4} ta="right">Round Total</Table.Td>
                      <Table.Td ta="center">{r.roundPointsTotal}</Table.Td>
                    </Table.Tr>
                  </Table.Tfoot>
                )}
              </Table>
            </Card>
          ))}
        </>
      )}
    </Container>
  );
}

export default PublicParticipantDetailPage;
