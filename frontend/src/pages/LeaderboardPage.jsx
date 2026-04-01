import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getSeasons, getLeaderboard } from '../api/leaderboard';
import '../Leaderboard.css';

function LeaderboardPage() {
  const { seasonId } = useParams();
  const navigate = useNavigate();
  const [seasons, setSeasons] = useState([]);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sortKey, setSortKey] = useState('overallTotal');
  const [sortDir, setSortDir] = useState('desc');

  useEffect(() => {
    getSeasons().then(res => {
      setSeasons(res.data);
      if (!seasonId && res.data.length > 0) {
        navigate(`/standings/${res.data[0].id}`, { replace: true });
      }
    });
  }, []);

  useEffect(() => {
    if (!seasonId) return;
    setLoading(true);
    getLeaderboard(seasonId).then(res => {
      setData(res.data);
      setLoading(false);
    });
  }, [seasonId]);

  function handleSort(key) {
    if (sortKey === key) {
      setSortDir(d => d === 'desc' ? 'asc' : 'desc');
    } else {
      setSortKey(key);
      setSortDir(key === 'teamName' || key === 'name' ? 'asc' : 'desc');
    }
  }

  const sortedEntries = useMemo(() => {
    if (!data) return [];
    const entries = [...data.entries];
    entries.sort((a, b) => {
      let va, vb;
      if (sortKey === 'teamName') {
        va = a.teamName.toLowerCase();
        vb = b.teamName.toLowerCase();
      } else if (sortKey === 'name') {
        va = a.name.toLowerCase();
        vb = b.name.toLowerCase();
      } else if (sortKey === 'overallTotal') {
        va = a.overallTotal;
        vb = b.overallTotal;
      } else if (sortKey.startsWith('round_')) {
        const rid = parseInt(sortKey.replace('round_', ''));
        va = a.roundScores[rid] ?? -1;
        vb = b.roundScores[rid] ?? -1;
      }
      if (va < vb) return sortDir === 'asc' ? -1 : 1;
      if (va > vb) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });
    return entries;
  }, [data, sortKey, sortDir]);

  function thClass(key, extra = '') {
    return `${extra} ${sortKey === key ? 'sorted' : ''}`.trim();
  }

  function sortIcon(key) {
    if (sortKey !== key) return '\u25B4\u25BE';
    return sortDir === 'asc' ? '\u25B4' : '\u25BE';
  }

  // Compute rank based on overallTotal (handles ties)
  const ranks = useMemo(() => {
    const byTotal = [...sortedEntries].sort((a, b) => b.overallTotal - a.overallTotal);
    const map = {};
    let rank = 1;
    for (let i = 0; i < byTotal.length; i++) {
      if (i > 0 && byTotal[i].overallTotal < byTotal[i - 1].overallTotal) {
        rank = i + 1;
      }
      map[byTotal[i].participantId] = rank;
    }
    return map;
  }, [sortedEntries]);

  return (
    <div className="lb-page">
      <div className="lb-header">
        <h1>Playoff Pool Standings</h1>
        <div className="lb-header-right">
          <label>Season</label>
          <select
            className="lb-season-select"
            value={seasonId || ''}
            onChange={e => navigate(`/standings/${e.target.value}`)}
          >
            {seasons.map(s => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="lb-container">
        {loading ? (
          <div className="lb-loading">Loading standings...</div>
        ) : !data || data.entries.length === 0 ? (
          <div className="lb-empty">No participants found for this season.</div>
        ) : (
          <div className="lb-table-wrapper">
            <table className="lb-table">
              <thead>
                <tr>
                  <th className="col-rank">#</th>
                  <th className={thClass('teamName')} onClick={() => handleSort('teamName')}>
                    Team <span className="sort-icon">{sortIcon('teamName')}</span>
                  </th>
                  <th className={thClass('name')} onClick={() => handleSort('name')}>
                    Name <span className="sort-icon">{sortIcon('name')}</span>
                  </th>
                  {data.rounds.map(r => (
                    <th
                      key={r.id}
                      className={thClass(`round_${r.id}`, r.scored ? '' : 'col-round-unscored')}
                      onClick={() => r.scored ? handleSort(`round_${r.id}`) : null}
                      style={r.scored ? {} : { cursor: 'default' }}
                    >
                      {r.name} <span className="sort-icon">{r.scored ? sortIcon(`round_${r.id}`) : ''}</span>
                    </th>
                  ))}
                  <th className={thClass('overallTotal', 'col-total')} onClick={() => handleSort('overallTotal')}>
                    Total <span className="sort-icon">{sortIcon('overallTotal')}</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {sortedEntries.map(entry => (
                  <tr key={entry.participantId}>
                    <td className="col-rank">{ranks[entry.participantId]}</td>
                    <td className="col-team">
                      <Link to={`/standings/${seasonId}/participant/${entry.participantId}`}>
                        {entry.teamName}
                      </Link>
                    </td>
                    <td className="col-name">
                      <Link to={`/standings/${seasonId}/participant/${entry.participantId}`}>
                        {entry.name}
                      </Link>
                    </td>
                    {data.rounds.map(r => {
                      const score = entry.roundScores[r.id];
                      return score != null ? (
                        <td key={r.id} className="col-score">{score}</td>
                      ) : (
                        <td key={r.id} className="col-unscored">&mdash;</td>
                      );
                    })}
                    <td className="col-total">{entry.overallTotal}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default LeaderboardPage;
