import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getSeasonSignup, submitSignup } from '../api/seasons';
import { Container, Title, TextInput, Button, Card, Alert, Stack, Text, Divider, Box, Group, SimpleGrid } from '@mantine/core';

function SignupPage() {
  const { seasonId } = useParams();
  const [season, setSeason] = useState(null);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getSeasonSignup(seasonId)
      .then(res => { setSeason(res.data); setLoading(false); })
      .catch(() => setLoading(false));
  }, [seasonId]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setStatus(null);
    try {
      await submitSignup(seasonId, email);
      setSeason(prev => ({ ...prev, participantCount: (prev.participantCount || 0) + 1 }));
      setStatus('success');
    } catch (err) {
      if (err.response?.status === 409) setStatus('duplicate');
      else setStatus('error');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <Container size="sm" mt={80}><Text ta="center" c="dimmed">Loading...</Text></Container>;
  }

  if (!season) {
    return <Container size="sm" mt={80}><Text ta="center" c="dimmed">Season not found.</Text></Container>;
  }

  return (
    <Box>
      <Box bg="dark.9" py={60} px="md">
        <Container size="sm">
          <Text size="xs" tt="uppercase" fw={700} c="dimmed" mb="xs" lts={2}>Playoff Pool</Text>
          <Title order={1} c="white">{season.name}</Title>
        </Container>
      </Box>

      <Container size="sm" py="xl">
        {season.signupContent && (
          <>
            <Box mb="xl">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{season.signupContent}</ReactMarkdown>
            </Box>
            <Divider mb="xl" />
          </>
        )}

        {season.participantCount != null && (
          <SimpleGrid cols={2} mb="xl">
            <Card withBorder padding="lg" radius="md" ta="center">
              <Text size="2rem" fw={700}>{season.participantCount}</Text>
              <Text size="sm" c="dimmed">Participants</Text>
            </Card>
            <Card withBorder padding="lg" radius="md" ta="center">
              <Text size="2rem" fw={700}>${season.participantCount * 20}</Text>
              <Text size="sm" c="dimmed">Prize Pot</Text>
            </Card>
          </SimpleGrid>
        )}

        <Card withBorder padding="xl" radius="md">
          {status === 'success' ? (
            <Stack ta="center">
              <Title order={2}>You're in.</Title>
              <Text c="dimmed">We've got your spot locked in for the {season.name} pool. We'll be in touch with next steps.</Text>
            </Stack>
          ) : (
            <>
              <Title order={3} mb="md">Reserve your spot</Title>
              <form onSubmit={handleSubmit}>
                <Stack>
                  <TextInput type="email" placeholder="your@email.com" value={email} onChange={e => setEmail(e.target.value)} required disabled={submitting} />
                  <Button type="submit" loading={submitting}>Sign me up</Button>
                  {status === 'duplicate' && <Alert color="red">That email is already registered for this season.</Alert>}
                  {status === 'error' && <Alert color="red">Something went wrong. Please try again.</Alert>}
                </Stack>
              </form>
            </>
          )}
        </Card>
      </Container>
    </Box>
  );
}

export default SignupPage;
