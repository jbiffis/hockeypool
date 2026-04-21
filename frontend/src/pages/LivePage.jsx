import { useState, useEffect, useCallback, useMemo } from 'react';
import { getLiveData } from '../api/live';
import { useLocalStorage } from '@mantine/hooks';
import {
  Badge, Button, Checkbox, Drawer, Group, Stack, Text, Title,
  ActionIcon, Tooltip, ScrollArea,
} from '@mantine/core';

// ── team colour accents ───────────────────────────────────────────────────────
const TEAM_COLORS = {
  BUF: '#003087', BOS: '#FFB81C', TBL: '#002868', MTL: '#AF1E2D',
  CAR: '#CC0000', OTT: '#C52032', PHI: '#F74902', PIT: '#FCB514',
  COL: '#6F263D', LAK: '#A2AAAD', DAL: '#006847', MIN: '#154734',
  VGK: '#B4975A', UTA: '#6CAEDF', EDM: '#FF4C00', ANA: '#F47A38',
};

const PICKER_PALETTE = [
  '#3b82f6','#10b981','#f59e0b','#ec4899','#8b5cf6',
  '#06b6d4','#84cc16','#f97316','#6366f1','#ef4444',
  '#14b8a6','#d946ef','#22c55e','#fb923c','#a855f7',
];

function pickerColor(id) {
  return PICKER_PALETTE[id % PICKER_PALETTE.length];
}

// ── pill component ────────────────────────────────────────────────────────────
function PickerPill({ picker }) {
  const color = pickerColor(picker.participantId);
  return (
    <span style={{
      display: 'inline-block',
      padding: '1px 8px',
      borderRadius: 99,
      fontSize: 11,
      fontWeight: 600,
      lineHeight: '18px',
      background: color + '33',
      color,
      border: `1px solid ${color}55`,
      whiteSpace: 'nowrap',
    }}>
      {picker.teamName}
    </span>
  );
}

// ── live pulsing dot ──────────────────────────────────────────────────────────
function LiveDot() {
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
      <span className="live-dot" />
      <span style={{ fontSize: 11, fontWeight: 700, color: '#ef4444', letterSpacing: 1 }}>LIVE</span>
    </span>
  );
}

// ── game card ─────────────────────────────────────────────────────────────────
function GameCard({ game, filteredPicks }) {
  const isLive = game.gameState === 'LIVE' || game.gameState === 'CRIT';
  const startET = game.startTimeUTC
    ? new Date(game.startTimeUTC).toLocaleTimeString('en-US', {
        hour: 'numeric', minute: '2-digit', timeZone: 'America/New_York',
      })
    : '';

  const awayColor = TEAM_COLORS[game.awayTeam.abbrev] || '#888';
  const homeColor = TEAM_COLORS[game.homeTeam.abbrev] || '#888';

  return (
    <div style={{
      background: 'rgba(255,255,255,0.07)',
      border: '1px solid rgba(255,255,255,0.12)',
      borderRadius: 14,
      padding: '12px 14px',
      backdropFilter: 'blur(8px)',
      flex: 1,
      minWidth: 0,
    }}>
      {/* header row */}
      <Group justify="space-between" mb={8}>
        {isLive ? <LiveDot /> : <Badge size="xs" variant="outline" color="gray">{startET}</Badge>}
        {isLive && (
          <Text size="xs" fw={600} c="gray.4">
            {game.inIntermission ? `INT after ${game.period}` : `${game.period} · ${game.clock}`}
          </Text>
        )}
      </Group>

      {/* teams */}
      {[
        { team: game.awayTeam, picks: filteredPicks.away, color: awayColor },
        { team: game.homeTeam, picks: filteredPicks.home, color: homeColor },
      ].map(({ team, picks, color }) => (
        <div key={team.abbrev} style={{ marginBottom: 8 }}>
          <Group gap={8} mb={4} align="center">
            <img
              src={team.logo}
              alt={team.abbrev}
              style={{ width: 28, height: 28, objectFit: 'contain', filter: 'drop-shadow(0 1px 3px rgba(0,0,0,0.5))' }}
            />
            <Text fw={700} size="sm" style={{ color: '#f1f5f9', flex: 1 }}>{team.name}</Text>
            {team.score != null && (
              <Text fw={800} size="xl" style={{
                color: '#fff',
                lineHeight: 1,
                minWidth: 20,
                textAlign: 'right',
                textShadow: `0 0 12px ${color}88`,
              }}>
                {team.score}
              </Text>
            )}
          </Group>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, paddingLeft: 36, minHeight: 20 }}>
            {picks.length === 0
              ? <Text size="xs" c="dimmed" fs="italic">—</Text>
              : picks.map(p => <PickerPill key={p.participantId} picker={p} />)
            }
          </div>
        </div>
      ))}
    </div>
  );
}

// ── player row (hot hand / blockade) ─────────────────────────────────────────
function PlayerRow({ player, filteredPickers }) {
  const teamColor = TEAM_COLORS[player.teamAbbrev] || '#888';
  return (
    <div style={{ marginBottom: 6 }}>
      <Group gap={6} mb={3}>
        <img
          src={`https://assets.nhle.com/logos/nhl/svg/${player.teamAbbrev}_light.svg`}
          alt={player.teamAbbrev}
          style={{ width: 20, height: 20, objectFit: 'contain', opacity: 0.9 }}
        />
        <Text size="xs" fw={700} style={{ color: '#e2e8f0', flex: 1 }}>{player.playerName}</Text>
        {player.points != null && (
          <Badge size="xs" variant="filled" style={{
            background: teamColor + '44', color: teamColor === '#888' ? '#ccc' : '#fff',
            border: `1px solid ${teamColor}66`, minWidth: 32,
          }}>
            {player.points}pt
          </Badge>
        )}
      </Group>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 3, paddingLeft: 26, minHeight: 16 }}>
        {filteredPickers.length === 0
          ? <Text size="xs" c="dimmed" fs="italic">—</Text>
          : filteredPickers.map(p => <PickerPill key={p.participantId} picker={p} />)
        }
      </div>
    </div>
  );
}

// ── section heading ───────────────────────────────────────────────────────────
function SectionLabel({ children }) {
  return (
    <Text size="xs" fw={800} tt="uppercase" c="dimmed" mb={6} style={{ letterSpacing: 1 }}>
      {children}
    </Text>
  );
}

// ── main page ─────────────────────────────────────────────────────────────────
function LivePage() {
  const [data, setData] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [lastRefresh, setLastRefresh] = useState(null);
  const [storedIds, setStoredIds] = useLocalStorage({ key: 'live-selected-v1', defaultValue: null });

  const fetchData = useCallback(() => {
    getLiveData().then(res => {
      setData(res.data);
      setLastRefresh(new Date());
    }).catch(() => {});
  }, []);

  useEffect(() => {
    fetchData();
    const iv = setInterval(fetchData, 30000);
    return () => clearInterval(iv);
  }, [fetchData]);

  // default to all selected on first load
  const selectedSet = useMemo(() => {
    if (!data) return new Set();
    if (storedIds === null) return new Set(data.participants.map(p => p.participantId));
    return new Set(storedIds);
  }, [data, storedIds]);

  const initIfNull = () => {
    if (storedIds === null && data) {
      setStoredIds(data.participants.map(p => p.participantId));
    }
  };

  const toggleParticipant = (id) => {
    initIfNull();
    const current = storedIds !== null
      ? new Set(storedIds)
      : new Set(data.participants.map(p => p.participantId));
    if (current.has(id)) current.delete(id); else current.add(id);
    setStoredIds([...current]);
  };

  const selectAll = () => {
    if (data) setStoredIds(data.participants.map(p => p.participantId));
  };
  const selectNone = () => setStoredIds([]);

  const filteredGame = (game) => ({
    away: game.awayPicks.filter(p => selectedSet.has(p.participantId)),
    home: game.homePicks.filter(p => selectedSet.has(p.participantId)),
  });

  const filterPickers = (pickers) =>
    (pickers || []).filter(p => selectedSet.has(p.participantId));

  const selectedCount = selectedSet.size;

  return (
    <>
      <style>{`
        @keyframes pulse-dot {
          0%, 100% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.4; transform: scale(0.7); }
        }
        .live-dot {
          width: 8px; height: 8px; border-radius: 50%;
          background: #ef4444;
          animation: pulse-dot 1.2s ease-in-out infinite;
          display: inline-block;
        }
        .live-page { display: flex; flex-direction: column; height: 100vh; padding: 12px 16px; box-sizing: border-box; }
        .live-main { display: flex; gap: 14px; flex: 1; min-height: 0; margin-top: 10px; }
        .live-games { flex: 1.1; display: flex; flex-direction: column; gap: 10px; min-width: 0; }
        .live-sidebar { width: 340px; min-width: 280px; display: flex; flex-direction: column; gap: 10px; }
        .side-card {
          background: rgba(255,255,255,0.07);
          border: 1px solid rgba(255,255,255,0.12);
          border-radius: 14px;
          padding: 12px 14px;
          backdropFilter: blur(8px);
          flex: 1;
          overflow: hidden;
          display: flex;
          flex-direction: column;
        }
        .side-scroll { flex: 1; overflow-y: auto; }
        .side-scroll::-webkit-scrollbar { width: 4px; }
        .side-scroll::-webkit-scrollbar-track { background: transparent; }
        .side-scroll::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 4px; }
      `}</style>

      <div className="live-page">
        {/* header */}
        <Group justify="space-between" align="center">
          <Group gap={10} align="center">
            <Title order={3} style={{ color: '#f8fafc', fontWeight: 800, letterSpacing: 0.5 }}>
              🏒 Live Now
            </Title>
            {lastRefresh && (
              <Text size="xs" c="dimmed">
                updated {lastRefresh.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', second: '2-digit' })}
              </Text>
            )}
          </Group>
          <Group gap={8}>
            <Tooltip label="Refresh">
              <ActionIcon variant="subtle" color="gray" onClick={fetchData} size="sm">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                </svg>
              </ActionIcon>
            </Tooltip>
            <Button
              size="xs"
              variant="light"
              color="blue"
              onClick={() => setDrawerOpen(true)}
              style={{ fontWeight: 700 }}
            >
              Who's Here? {selectedCount > 0 && `(${selectedCount})`}
            </Button>
          </Group>
        </Group>

        {/* main content */}
        <div className="live-main">
          {/* games column */}
          <div className="live-games">
            {!data ? (
              <div style={{ color: '#94a3b8', fontSize: 14, marginTop: 20 }}>Loading games…</div>
            ) : data.games.length === 0 ? (
              <div style={{
                flex: 1,
                display: 'flex', flexDirection: 'column',
                alignItems: 'center', justifyContent: 'center',
                color: '#64748b', fontSize: 14, gap: 8,
              }}>
                <span style={{ fontSize: 40 }}>🏒</span>
                <span>No games scheduled today.</span>
              </div>
            ) : (
              data.games.map(game => (
                <GameCard
                  key={game.gameId}
                  game={game}
                  filteredPicks={filteredGame(game)}
                />
              ))
            )}
          </div>

          {/* sidebar */}
          <div className="live-sidebar">
            {/* hot hand */}
            <div className="side-card">
              <SectionLabel>🔥 Hot Hand</SectionLabel>
              <div className="side-scroll">
                {(data?.hotHand || []).map(player => (
                  <PlayerRow
                    key={player.optionId}
                    player={player}
                    filteredPickers={filterPickers(player.pickers)}
                  />
                ))}
              </div>
            </div>

            {/* blockade */}
            <div className="side-card" style={{ flex: 'none' }}>
              <SectionLabel>🧱 Blockade</SectionLabel>
              <div>
                {(data?.blockade || []).map(player => (
                  <PlayerRow
                    key={player.optionId}
                    player={player}
                    filteredPickers={filterPickers(player.pickers)}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Who's Here? drawer */}
      <Drawer
        opened={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        title={
          <Group justify="space-between" style={{ width: '100%' }}>
            <Text fw={700}>Who's Here?</Text>
            <Group gap={6}>
              <Button size="xs" variant="subtle" onClick={selectAll}>All</Button>
              <Button size="xs" variant="subtle" color="red" onClick={selectNone}>None</Button>
            </Group>
          </Group>
        }
        position="right"
        size={280}
        overlayProps={{ opacity: 0.3 }}
        styles={{
          header: { background: '#0f172a', borderBottom: '1px solid rgba(255,255,255,0.08)' },
          body: { background: '#0f172a', padding: '8px 16px' },
          title: { width: '100%' },
          close: { color: '#94a3b8' },
        }}
      >
        <Stack gap={4}>
          {(data?.participants || []).map(p => (
            <Checkbox
              key={p.participantId}
              checked={selectedSet.has(p.participantId)}
              onChange={() => toggleParticipant(p.participantId)}
              label={
                <div>
                  <Text size="sm" fw={600} style={{ color: '#f1f5f9', lineHeight: 1.2 }}>{p.teamName}</Text>
                  <Text size="xs" c="dimmed">{p.name}</Text>
                </div>
              }
              styles={{
                root: { padding: '4px 0', cursor: 'pointer' },
                input: { cursor: 'pointer' },
              }}
            />
          ))}
        </Stack>
      </Drawer>
    </>
  );
}

export default LivePage;
