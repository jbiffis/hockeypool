import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getSeasons, getLeaderboard } from '../api/leaderboard';
import { getDivisions } from '../api/divisions';
import { Container, Title, Table, Select, Group, Text, ScrollArea, UnstyledButton, Center, Paper, Stack } from '@mantine/core';
import cx from 'clsx';

function LeaderboardPage() {
  const { seasonId } = useParams();
  const navigate = useNavigate();
  const [seasons, setSeasons] = useState([]);
  const [divisions, setDivisions] = useState([]);
  const [divisionId, setDivisionId] = useState(null);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sortKey, setSortKey] = useState('overallTotal');
  const [sortDir, setSortDir] = useState('desc');
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      if (!seasonId && res.data.length > 0) navigate(`/standings/${res.data[0].id}`, { replace: true });
    });
  }, []);

  useEffect(() => {
    if (!seasonId) return;
    setDivisionId(null);
    getDivisions(seasonId).then(res => setDivisions(res.data)).catch(() => setDivisions([]));
  }, [seasonId]);

  useEffect(() => {
    if (!seasonId) return;
    setLoading(true);
    getLeaderboard(seasonId, divisionId).then(res => { setData(res.data); setLoading(false); });
  }, [seasonId, divisionId]);

  function handleSort(key) {
    if (sortKey === key) setSortDir(d => d === 'desc' ? 'asc' : 'desc');
    else { setSortKey(key); setSortDir(key === 'teamName' || key === 'name' ? 'asc' : 'desc'); }
  }

  const sortedEntries = useMemo(() => {
    if (!data) return [];
    const entries = [...data.entries];
    entries.sort((a, b) => {
      let va, vb;
      if (sortKey === 'teamName') { va = a.teamName.toLowerCase(); vb = b.teamName.toLowerCase(); }
      else if (sortKey === 'name') { va = a.name.toLowerCase(); vb = b.name.toLowerCase(); }
      else if (sortKey === 'overallTotal') { va = a.overallTotal; vb = b.overallTotal; }
      else if (sortKey.startsWith('round_')) { const rid = parseInt(sortKey.replace('round_', '')); va = a.roundScores[rid] ?? -1; vb = b.roundScores[rid] ?? -1; }
      if (va < vb) return sortDir === 'asc' ? -1 : 1;
      if (va > vb) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });
    return entries;
  }, [data, sortKey, sortDir]);

  const ranks = useMemo(() => {
    const byTotal = [...sortedEntries].sort((a, b) => b.overallTotal - a.overallTotal);
    const map = {};
    let rank = 1;
    for (let i = 0; i < byTotal.length; i++) {
      if (i > 0 && byTotal[i].overallTotal < byTotal[i - 1].overallTotal) rank = i + 1;
      map[byTotal[i].participantId] = rank;
    }
    return map;
  }, [sortedEntries]);

  function sortIcon(key) {
    if (sortKey !== key) return '\u25B4\u25BE';
    return sortDir === 'asc' ? '\u25B4' : '\u25BE';
  }

  function Th({ sortable, sortKeyName, children, ...props }) {
    if (!sortable) return <Table.Th {...props}>{children}</Table.Th>;
    return (
      <Table.Th {...props}>
        <UnstyledButton onClick={() => handleSort(sortKeyName)} style={{ fontWeight: sortKey === sortKeyName ? 700 : 500 }}>
          {children} <Text span size="xs" c="dimmed">{sortIcon(sortKeyName)}</Text>
        </UnstyledButton>
      </Table.Th>
    );
  }

  return (
    <Container size="xl" py="xl">
      <Stack gap={4} align="center" mb="xl">
        <Title order={1} className="hero-title" ta="center" fz={{ base: 32, sm: 44 }}>Playoff Pool Standings</Title>
        <Text className="hero-subtitle" ta="center" size="md">Live leaderboard — updated every round</Text>
        {data?.lastUpdatedAt && (
          <Text className="hero-subtitle" ta="center" size="sm" c="dimmed">
            Stats last updated {new Date(data.lastUpdatedAt).toLocaleString('en-US', {
              month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
            })}
          </Text>
        )}
      </Stack>

      <Paper shadow="lg" radius="lg" p="md" bg="white">
        <Group justify="flex-end" mb="md" wrap="wrap">
          <Group>
            {divisions.length > 0 && (
              <Select
                label="Division"
                placeholder="Overall"
                value={divisionId != null ? String(divisionId) : null}
                onChange={(val) => setDivisionId(val ? Number(val) : null)}
                data={divisions.map(d => ({ value: String(d.id), label: d.name }))}
                clearable
                style={{ minWidth: 160 }}
              />
            )}
            <Select
              label="Season"
              value={seasonId || null}
              onChange={(val) => navigate(`/standings/${val}`)}
              data={seasons.map(s => ({ value: String(s.id), label: s.name }))}
              style={{ minWidth: 160 }}
            />
          </Group>
        </Group>

        {loading ? (
          <Center py="xl"><Text c="dimmed">Loading standings...</Text></Center>
        ) : !data || data.entries.length === 0 ? (
          <Center py="xl"><Text c="dimmed">No participants found for this season.</Text></Center>
        ) : (
          <ScrollArea h="calc(100vh - 260px)" onScrollPositionChange={({ y }) => setScrolled(y !== 0)}>
            <Table striped highlightOnHover stickyHeader tabularNums miw={700}>
            <Table.Thead style={scrolled ? { boxShadow: 'var(--mantine-shadow-sm)' } : undefined}>
              <Table.Tr>
                <Table.Th style={{ width: 50 }}>#</Table.Th>
                <Th sortable sortKeyName="teamName">Team</Th>
                <Th sortable sortKeyName="overallTotal" style={{ fontWeight: 700 }}>Total</Th>
                {data.rounds.map(r => (
                  <Th key={r.id} sortable={r.scored} sortKeyName={`round_${r.id}`}>
                    {r.name}
                  </Th>
                ))}
                <Th sortable sortKeyName="overallTotal" style={{ fontWeight: 700 }}>Total</Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {sortedEntries.map(entry => (
                <Table.Tr key={entry.participantId}>
                  <Table.Td>{ranks[entry.participantId]}</Table.Td>
                  <Table.Td>
                    <Link to={`/standings/${seasonId}/participant/${entry.participantId}`} style={{ color: 'var(--mantine-color-blue-6)', textDecoration: 'none' }}>
                      {entry.teamName} <Text span size="xs" c="dimmed">- {entry.name}</Text>
                    </Link>
                  </Table.Td>
                  <Table.Td fw={700}>{entry.overallTotal}</Table.Td>
                  {data.rounds.map(r => {
                    const score = entry.roundScores[r.id];
                    return <Table.Td key={r.id} c={score == null ? 'dimmed' : undefined}>{score ?? '—'}</Table.Td>;
                  })}
                  <Table.Td fw={700}>{entry.overallTotal}</Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </ScrollArea>
        )}
      </Paper>
    </Container>
  );
}

export default LeaderboardPage;
