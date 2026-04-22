import { useState, useEffect, useCallback, useMemo } from 'react';
import { getLiveData } from '../api/live';
import { useLocalStorage } from '@mantine/hooks';
import {
  Button, Checkbox, Drawer, Group, Stack, Text, ActionIcon, Tooltip,
} from '@mantine/core';

// ── team colour accents ───────────────────────────────────────────────────────
const TEAM_COLORS = {
  BUF: '#003087', BOS: '#FFB81C', TBL: '#002868', MTL: '#AF1E2D',
  CAR: '#CC0000', OTT: '#C52032', PHI: '#F74902', PIT: '#FCB514',
  COL: '#6F263D', LAK: '#A2AAAD', DAL: '#006847', MIN: '#154734',
  VGK: '#B4975A', UTA: '#6CAEDF', EDM: '#FF4C00', ANA: '#F47A38',
};

const PICKER_PALETTE = [
  '#60a5fa','#34d399','#fbbf24','#f472b6','#a78bfa',
  '#22d3ee','#a3e635','#fb923c','#818cf8','#f87171',
  '#2dd4bf','#e879f9','#4ade80','#fdba74','#c084fc',
];

function pickerColor(id) {
  return PICKER_PALETTE[id % PICKER_PALETTE.length];
}

// ── big picker chip ──────────────────────────────────────────────────────────
function PickerPill({ picker, size = 'md' }) {
  const color = pickerColor(picker.participantId);
  const sizes = {
    md: { fs: 24, pad: '10px 22px' },
    lg: { fs: 28, pad: '12px 26px' },
  };
  const s = sizes[size];
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center',
      padding: s.pad,
      borderRadius: 999,
      fontSize: s.fs,
      fontWeight: 700,
      lineHeight: 1.2,
      background: 'rgba(2,6,23,0.5)',
      color: '#fff',
      border: `2px solid ${color}`,
      boxShadow: `0 0 10px ${color}66`,
      whiteSpace: 'nowrap',
      textShadow: '0 1px 3px rgba(0,0,0,0.9)',
    }}>
      {picker.teamName}
    </span>
  );
}

// ── live pulsing badge ───────────────────────────────────────────────────────
function LiveBadge() {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 8,
      padding: '6px 14px',
      background: 'linear-gradient(135deg, #dc2626, #ef4444)',
      borderRadius: 8,
      boxShadow: '0 0 20px rgba(239,68,68,0.6)',
    }}>
      <span className="live-dot" />
      <span style={{ fontSize: 16, fontWeight: 900, color: '#fff', letterSpacing: 2 }}>LIVE</span>
    </span>
  );
}

// ── game card (TV scale) ──────────────────────────────────────────────────────
function GameCard({ game, filteredPicks }) {
  const isLive = game.gameState === 'LIVE' || game.gameState === 'CRIT';
  const isFinal = game.gameState === 'FINAL' || game.gameState === 'OFF';
  const startET = game.startTimeUTC
    ? new Date(game.startTimeUTC).toLocaleTimeString('en-US', {
        hour: 'numeric', minute: '2-digit', timeZone: 'America/New_York',
      })
    : '';

  const awayColor = TEAM_COLORS[game.awayTeam.abbrev] || '#64748b';
  const homeColor = TEAM_COLORS[game.homeTeam.abbrev] || '#64748b';

  const TeamSide = ({ team, picks, color, side }) => {
    const isHome = side === 'home';
    const logo = (
      <div style={{ position: 'relative', width: 140, height: 140, flexShrink: 0 }}>
        <div style={{
          position: 'absolute', inset: -10, borderRadius: '50%',
          background: `radial-gradient(circle, ${color}55 0%, transparent 65%)`,
          filter: 'blur(8px)',
        }} />
        <img
          src={team.logo}
          alt={team.abbrev}
          style={{
            position: 'relative', width: 140, height: 140, objectFit: 'contain',
            filter: 'drop-shadow(0 6px 18px rgba(0,0,0,0.6))',
          }}
        />
      </div>
    );
    const nameAndPicks = (
      <div style={{
        flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column',
        alignItems: isHome ? 'flex-end' : 'flex-start', gap: 8,
      }}>
        <div style={{
          fontSize: 36, fontWeight: 900,
          color: '#fde047',
          textTransform: 'uppercase', letterSpacing: 0.5,
          textShadow: '0 2px 8px rgba(0,0,0,0.95), 0 0 3px rgba(0,0,0,1)',
          textAlign: isHome ? 'right' : 'left',
          lineHeight: 1.05,
        }}>
          {team.name}
        </div>
        <div style={{
          display: 'flex', flexWrap: 'wrap',
          gap: 6, justifyContent: isHome ? 'flex-end' : 'flex-start',
          minHeight: 30,
        }}>
          {picks.length === 0
            ? <span style={{ color: '#475569', fontStyle: 'italic', fontSize: 15 }}>no picks</span>
            : picks.map(p => <PickerPill key={p.participantId} picker={p} size="md" />)
          }
        </div>
      </div>
    );
    return (
      <div style={{
        flex: 1, display: 'flex', flexDirection: 'row',
        alignItems: 'center', minWidth: 0, gap: 14,
      }}>
        {isHome ? <>{nameAndPicks}{logo}</> : <>{logo}{nameAndPicks}</>}
      </div>
    );
  };

  return (
    <div style={{
      background: 'linear-gradient(135deg, rgba(15,23,42,0.92), rgba(2,6,23,0.92))',
      border: '1px solid rgba(148,163,184,0.2)',
      borderRadius: 20,
      padding: '20px 28px',
      boxShadow: '0 10px 40px rgba(0,0,0,0.5), inset 0 1px 0 rgba(255,255,255,0.08)',
      flex: 1,
      minHeight: 0,
      display: 'flex',
      flexDirection: 'column',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* coloured edge accents */}
      <div style={{
        position: 'absolute', left: 0, top: 0, bottom: 0, width: 6,
        background: `linear-gradient(180deg, ${awayColor}, ${homeColor})`,
      }} />

      {/* status bar */}
      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        marginBottom: 16, gap: 12,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {isLive
            ? <LiveBadge />
            : isFinal
              ? <span style={{ fontSize: 16, fontWeight: 800, color: '#94a3b8', letterSpacing: 2 }}>FINAL</span>
              : <span style={{ fontSize: 18, fontWeight: 800, color: '#cbd5e1', letterSpacing: 1 }}>{startET} ET</span>
          }
          {game.gameNumberOfSeries && (
            <span style={{
              fontSize: 14, fontWeight: 800, color: '#94a3b8',
              padding: '4px 10px', borderRadius: 6,
              background: 'rgba(148,163,184,0.15)',
              letterSpacing: 1,
            }}>
              GAME {game.gameNumberOfSeries}
            </span>
          )}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          {game.seriesStatus && (
            <span style={{
              fontSize: 16, fontWeight: 800, color: '#e2e8f0',
              textTransform: 'uppercase', letterSpacing: 1,
              padding: '4px 10px', borderRadius: 6,
              background: 'rgba(100,116,139,0.25)',
              border: '1px solid rgba(148,163,184,0.35)',
            }}>
              {game.seriesStatus}
            </span>
          )}
          {isLive && (
            <span style={{ fontSize: 18, fontWeight: 800, color: '#fbbf24', letterSpacing: 1 }}>
              {game.inIntermission ? `INT · ${game.period}` : `${game.period} · ${game.clock}`}
            </span>
          )}
        </div>
      </div>

      {/* matchup row */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        gap: 20, flex: 1,
      }}>
        <TeamSide team={game.awayTeam} picks={filteredPicks.away} color={awayColor} side="away" />

        {/* scores */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 18,
          padding: '0 8px',
        }}>
          <div style={{
            fontSize: 120, fontWeight: 900, lineHeight: 1,
            color: '#fff',
            textShadow: `0 0 24px ${awayColor}aa, 0 4px 12px rgba(0,0,0,0.6)`,
            minWidth: 70, textAlign: 'center',
            fontVariantNumeric: 'tabular-nums',
          }}>
            {game.awayTeam.score ?? '—'}
          </div>
          <div style={{
            fontSize: 36, fontWeight: 900, color: '#475569', lineHeight: 1,
          }}>
            ·
          </div>
          <div style={{
            fontSize: 120, fontWeight: 900, lineHeight: 1,
            color: '#fff',
            textShadow: `0 0 24px ${homeColor}aa, 0 4px 12px rgba(0,0,0,0.6)`,
            minWidth: 70, textAlign: 'center',
            fontVariantNumeric: 'tabular-nums',
          }}>
            {game.homeTeam.score ?? '—'}
          </div>
        </div>

        <TeamSide team={game.homeTeam} picks={filteredPicks.home} color={homeColor} side="home" />
      </div>
    </div>
  );
}

// ── player row (hot hand / blockade) ─────────────────────────────────────────
function PlayerRow({ player, filteredPickers }) {
  const teamColor = TEAM_COLORS[player.teamAbbrev] || '#64748b';
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 14,
      padding: '10px 12px',
      borderRadius: 10,
      background: 'linear-gradient(90deg, rgba(30,41,59,0.6), rgba(15,23,42,0.3))',
      borderLeft: `4px solid ${teamColor}`,
      marginBottom: 8,
    }}>
      <img
        src={`https://assets.nhle.com/logos/nhl/svg/${player.teamAbbrev}_light.svg`}
        alt={player.teamAbbrev}
        style={{ width: 60, height: 60, objectFit: 'contain', flexShrink: 0 }}
      />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: 26, fontWeight: 800, color: '#f8fafc',
          lineHeight: 1.1, marginBottom: 6,
          whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
        }}>
          {player.playerName}
        </div>
        <div style={{
          display: 'flex', flexWrap: 'wrap', gap: 6,
        }}>
          {filteredPickers.length === 0
            ? <span style={{ fontSize: 16, color: '#475569', fontStyle: 'italic' }}>—</span>
            : filteredPickers.map(p => <PickerPill key={p.participantId} picker={p} size="md" />)
          }
        </div>
      </div>
      {player.points != null && (
        <div style={{
          fontSize: 40, fontWeight: 900, color: '#fff',
          padding: '8px 18px', borderRadius: 12,
          background: teamColor,
          boxShadow: `0 0 24px ${teamColor}aa`,
          minWidth: 72, textAlign: 'center',
          lineHeight: 1,
        }}>
          {player.points}
        </div>
      )}
    </div>
  );
}

// ── section heading ───────────────────────────────────────────────────────────
function SectionLabel({ icon, label, accent }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 10,
      padding: '8px 14px',
      marginBottom: 10,
      background: `linear-gradient(90deg, ${accent}, ${accent}00)`,
      borderLeft: `4px solid ${accent}`,
      borderRadius: 4,
    }}>
      <span style={{ fontSize: 32 }}>{icon}</span>
      <span style={{
        fontSize: 30, fontWeight: 900, color: '#fff',
        textTransform: 'uppercase', letterSpacing: 2,
        textShadow: `0 0 12px ${accent}`,
      }}>
        {label}
      </span>
    </div>
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

  const selectedSet = useMemo(() => {
    if (storedIds === null) return new Set();
    return new Set(storedIds);
  }, [storedIds]);

  const toggleParticipant = (id) => {
    const current = new Set(storedIds || []);
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
  const totalCount = data?.participants?.length || 0;

  const liveGames = (data?.games || []).filter(
    g => g.gameState === 'LIVE' || g.gameState === 'CRIT'
  );

  // abbrev aliases between NHL canonical and pool option text
  const ABBREV_ALIASES = { TBL: 'TB', TB: 'TBL', UTA: 'UTH', UTH: 'UTA', ANA: 'ANH', ANH: 'ANA' };
  const liveTeams = new Set();
  liveGames.forEach(g => {
    [g.awayTeam.abbrev, g.homeTeam.abbrev].forEach(a => {
      if (!a) return;
      liveTeams.add(a);
      if (ABBREV_ALIASES[a]) liveTeams.add(ABBREV_ALIASES[a]);
    });
  });
  const isPlaying = (player) => liveTeams.has(player.teamAbbrev);
  const liveHotHand = (data?.hotHand || []).filter(isPlaying);
  const liveBlockade = (data?.blockade || []).filter(isPlaying);

  return (
    <>
      <style>{`
        @keyframes pulse-dot-live {
          0%, 100% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.4; transform: scale(0.6); }
        }
        .live-dot {
          width: 12px; height: 12px; border-radius: 50%;
          background: #fff;
          box-shadow: 0 0 12px #fff;
          animation: pulse-dot-live 1.2s ease-in-out infinite;
          display: inline-block;
        }
        .tv-shell {
          display: flex; flex-direction: column;
          height: 100vh;
          padding: 20px 28px;
          box-sizing: border-box;
          gap: 16px;
        }
        .tv-header {
          display: flex; justify-content: space-between; align-items: center;
          padding-bottom: 12px;
          border-bottom: 2px solid rgba(148,163,184,0.15);
        }
        .tv-title {
          font-size: 42px;
          font-weight: 900;
          color: #fff;
          letter-spacing: 1px;
          text-transform: uppercase;
          text-shadow: 0 0 24px rgba(239,68,68,0.5), 0 4px 12px rgba(0,0,0,0.6);
          line-height: 1;
          display: flex; align-items: center; gap: 14px;
        }
        .tv-subtitle {
          font-size: 14px; color: #64748b; font-weight: 600; letter-spacing: 1px;
          text-transform: uppercase;
        }
        .tv-main {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
          flex: 1;
          min-height: 0;
        }
        .tv-games {
          display: flex; flex-direction: column;
          gap: 16px; min-height: 0; min-width: 0;
        }
        .tv-sidebar {
          display: flex; flex-direction: column;
          gap: 16px;
          min-height: 0;
        }
        .tv-card {
          background: linear-gradient(135deg, rgba(15,23,42,0.92), rgba(2,6,23,0.92));
          border: 1px solid rgba(148,163,184,0.2);
          border-radius: 20px;
          padding: 18px 18px;
          box-shadow: 0 10px 40px rgba(0,0,0,0.5), inset 0 1px 0 rgba(255,255,255,0.08);
          flex: 1;
          display: flex;
          flex-direction: column;
          min-height: 0;
          overflow: hidden;
        }
        .tv-scroll { flex: 1; overflow-y: auto; padding-right: 6px; }
        .tv-scroll::-webkit-scrollbar { width: 6px; }
        .tv-scroll::-webkit-scrollbar-track { background: transparent; }
        .tv-scroll::-webkit-scrollbar-thumb { background: rgba(148,163,184,0.3); border-radius: 4px; }
        .tv-btn-big {
          font-size: 18px !important;
          font-weight: 800 !important;
          padding: 12px 24px !important;
          height: auto !important;
          letter-spacing: 1px;
          text-transform: uppercase;
        }
      `}</style>

      <div className="tv-shell">
        {/* header */}
        <div className="tv-header">
          <div className="tv-title">
            <span style={{ fontSize: 44 }}>🏒</span>
            <span>Live Scoreboard</span>
          </div>
          <Group gap={16}>
            {lastRefresh && (
              <div className="tv-subtitle">
                updated {lastRefresh.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', second: '2-digit' })}
              </div>
            )}
            <Tooltip label="Refresh">
              <ActionIcon variant="light" color="gray" onClick={fetchData} size="xl" radius="xl">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                </svg>
              </ActionIcon>
            </Tooltip>
            <Button
              className="tv-btn-big"
              variant="filled"
              color="blue"
              radius="xl"
              onClick={() => setDrawerOpen(true)}
            >
              Who's Here? {selectedCount}/{totalCount}
            </Button>
          </Group>
        </div>

        {/* main */}
        <div className="tv-main">
          {/* games */}
          <div className="tv-games">
            {!data ? (
              <div style={{ color: '#94a3b8', fontSize: 24, marginTop: 40, textAlign: 'center' }}>Loading games…</div>
            ) : liveGames.length === 0 ? (
              <div style={{
                flex: 1, display: 'flex', flexDirection: 'column',
                alignItems: 'center', justifyContent: 'center',
                color: '#64748b', gap: 16,
              }}>
                <span style={{ fontSize: 80 }}>🏒</span>
                <span style={{ fontSize: 28, fontWeight: 700 }}>No live games right now</span>
              </div>
            ) : (
              liveGames.map(game => (
                <GameCard
                  key={game.gameId}
                  game={game}
                  filteredPicks={filteredGame(game)}
                />
              ))
            )}
          </div>

          {/* sidebar */}
          <div className="tv-sidebar">
            {/* hot hand */}
            <div className="tv-card">
              <SectionLabel icon="🔥" label="Hot Hand" accent="#f97316" />
              <div className="tv-scroll">
                {liveHotHand.length === 0
                  ? <div style={{ color: '#475569', fontSize: 18, fontStyle: 'italic', padding: '8px 4px' }}>No Hot Hand players in live games</div>
                  : liveHotHand.map(player => (
                    <PlayerRow
                      key={player.optionId}
                      player={player}
                      filteredPickers={filterPickers(player.pickers)}
                    />
                  ))
                }
              </div>
            </div>

            {/* blockade */}
            <div className="tv-card" style={{ flex: 'none', maxHeight: '38%' }}>
              <SectionLabel icon="🧱" label="Blockade" accent="#3b82f6" />
              <div className="tv-scroll">
                {liveBlockade.length === 0
                  ? <div style={{ color: '#475569', fontSize: 18, fontStyle: 'italic', padding: '8px 4px' }}>No Blockade goalies in live games</div>
                  : liveBlockade.map(player => (
                    <PlayerRow
                      key={player.optionId}
                      player={player}
                      filteredPickers={filterPickers(player.pickers)}
                    />
                  ))
                }
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
            <Text size="lg" fw={800} c="#f8fafc">Who's Here?</Text>
            <Group gap={6}>
              <Button size="sm" variant="light" color="blue" onClick={selectAll}>All</Button>
              <Button size="sm" variant="light" color="red" onClick={selectNone}>None</Button>
            </Group>
          </Group>
        }
        position="right"
        size={360}
        overlayProps={{ opacity: 0.5 }}
        styles={{
          header: { background: '#0f172a', borderBottom: '1px solid rgba(255,255,255,0.08)', paddingBlock: 14 },
          body: { background: '#0f172a', padding: '12px 18px' },
          title: { width: '100%' },
          close: { color: '#94a3b8' },
        }}
      >
        <Stack gap={6}>
          {(data?.participants || []).map(p => (
            <Checkbox
              key={p.participantId}
              checked={selectedSet.has(p.participantId)}
              onChange={() => toggleParticipant(p.participantId)}
              label={
                <div>
                  <Text size="md" fw={700} style={{ color: '#f1f5f9', lineHeight: 1.2 }}>{p.teamName}</Text>
                  <Text size="sm" c="dimmed">{p.name}</Text>
                </div>
              }
              styles={{
                root: { padding: '6px 0', cursor: 'pointer' },
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
