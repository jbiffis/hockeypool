import { useNavigate } from 'react-router-dom';
import { Title, SimpleGrid, Card, Text } from '@mantine/core';

const cards = [
  { title: 'Seasons', description: 'Manage playoff pool seasons', path: '/admin/seasons' },
  { title: 'Rounds', description: 'Manage rounds, questions, and options', path: '/admin/rounds' },
  { title: 'Participants', description: 'View participants and their responses', path: '/admin/participants' },
];

function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <div>
      <Title order={1} mb="lg">Admin Dashboard</Title>
      <SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
        {cards.map((card) => (
          <Card
            key={card.path}
            withBorder
            padding="lg"
            radius="md"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(card.path)}
          >
            <Title order={3} mb="xs">{card.title}</Title>
            <Text size="sm" c="dimmed">{card.description}</Text>
          </Card>
        ))}
      </SimpleGrid>
    </div>
  );
}

export default AdminDashboard;
