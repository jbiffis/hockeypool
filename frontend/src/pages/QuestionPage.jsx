import { useState, useEffect } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { getQuestionDetail } from '../api/leaderboard';
import { Container, Title, Text, Card, Group, Badge, Image, Progress, Anchor, Alert, Center, Stack } from '@mantine/core';

function QuestionPage() {
  const { seasonId, questionId } = useParams();
  const [searchParams] = useSearchParams();
  const fromParticipantId = searchParams.get('from') ? parseInt(searchParams.get('from'), 10) : null;
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getQuestionDetail(questionId)
      .then(res => { setQuestion(res.data); setLoading(false); })
      .catch(() => { setError('Failed to load question'); setLoading(false); });
  }, [questionId]);

  const isDynamic = question && question.correctAnswerText == null;
  const totalPickers = question ? question.options.reduce((sum, o) => sum + o.pickers.length, 0) : 0;

  return (
    <Container size="sm" py="xl">
      <Group mb="md">
        {fromParticipantId ? (
          <Anchor component={Link} to={`/standings/${seasonId}/participant/${fromParticipantId}`} size="sm">← Back to Participant</Anchor>
        ) : (
          <Anchor component={Link} to={`/standings/${seasonId}`} size="sm">← Back to Standings</Anchor>
        )}
      </Group>

      {question && (
        <>
          <Title order={1} mb="xs">{question.title}</Title>
          <Text c="dimmed" mb="lg">{question.roundName}</Text>
        </>
      )}

      {error && <Alert color="red" mb="md">{error}</Alert>}

      {loading ? (
        <Center py="xl"><Text c="dimmed">Loading question...</Text></Center>
      ) : question && (
        <Stack gap="md">
          {question.description && <Text c="dimmed">{question.description}</Text>}
          {question.imageUrl && <Image src={question.imageUrl} alt="" radius="md" maw={500} />}

          {!isDynamic && question.correctAnswerText && (
            <Card withBorder bg="green.0" padding="sm">
              <Group>
                <Text size="sm" fw={500}>Correct Answer</Text>
                <Text size="sm" fw={700}>{question.correctAnswerText}</Text>
              </Group>
            </Card>
          )}

          {isDynamic && (
            <Card withBorder bg="gray.0" padding="sm">
              <Text size="sm" c="dimmed">Variable scoring — points depend on player performance</Text>
            </Card>
          )}

          {question.options.map(opt => {
            const pct = totalPickers > 0 ? Math.round((opt.pickers.length / totalPickers) * 100) : 0;
            const fromPicker = fromParticipantId != null ? opt.pickers.find(p => p.participantId === fromParticipantId) : null;

            return (
              <Card key={opt.optionId} withBorder padding="sm" radius="md"
                style={opt.correct ? { borderColor: 'var(--mantine-color-green-5)' } : fromPicker ? { borderColor: 'var(--mantine-color-blue-4)' } : undefined}
              >
                <Group justify="space-between" wrap="nowrap" mb={4}>
                  <Group gap="xs">
                    {opt.correct && <Text c="green" fw={700}>✓</Text>}
                    <div>
                      <Text size="sm" fw={500}>{opt.optionText}</Text>
                      {opt.subtext && <Text size="xs" c="dimmed">{opt.subtext}</Text>}
                    </div>
                    {opt.imageUrl && <Image src={opt.imageUrl} alt="" w={40} h={40} radius="sm" fit="contain" />}
                  </Group>
                  <Group gap="xs" wrap="nowrap">
                    {fromPicker && <Badge size="xs" variant="light">{fromPicker.teamName}'s pick</Badge>}
                    {opt.points != null && <Badge variant="light" color="blue" size="sm">{opt.points} pts</Badge>}
                    <Text size="xs" c="dimmed">{opt.pickers.length} {opt.pickers.length === 1 ? 'pick' : 'picks'}</Text>
                  </Group>
                </Group>
                <Progress value={pct} size="sm" color={opt.correct ? 'green' : 'blue'} />
              </Card>
            );
          })}
        </Stack>
      )}
    </Container>
  );
}

export default QuestionPage;
