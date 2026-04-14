import { AppShell, Burger, Group, NavLink, Text, Button, Stack } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_ITEMS = [
  { label: 'Dashboard', path: '/admin' },
  { label: 'Rounds', path: '/admin/rounds' },
  { label: 'Participants', path: '/admin/participants' },
  { label: 'Divisions', path: '/admin/divisions' },
];

function AdminLayout() {
  const [opened, { toggle, close }] = useDisclosure();
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();

  async function handleLogout() {
    await logout();
    navigate('/admin/login');
  }

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={{ width: 240, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md">
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
          <Text fw={700} size="lg">Playoff Pool</Text>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        <Stack justify="space-between" h="100%">
          <Stack gap={4}>
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.path}
                label={item.label}
                active={item.path === '/admin'
                  ? location.pathname === '/admin'
                  : location.pathname.startsWith(item.path)}
                onClick={() => { navigate(item.path); close(); }}
              />
            ))}
          </Stack>
          <Button variant="subtle" color="gray" onClick={handleLogout} fullWidth>
            Log Out
          </Button>
        </Stack>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}

export default AdminLayout;
