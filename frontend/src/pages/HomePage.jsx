import { useNavigate } from 'react-router-dom';
import { Container, Title, Text, Button, Stack } from '@mantine/core';

function HomePage() {
  const navigate = useNavigate();
  return (
    <Container size="xs" mt={120} ta="center">
      <Stack>
        <Title order={1}>Playoff Pool</Title>
        <Text c="dimmed">Hockey playoff pool app</Text>
        <Button onClick={() => navigate('/admin')}>Go to Admin</Button>
      </Stack>
    </Container>
  );
}

export default HomePage;
