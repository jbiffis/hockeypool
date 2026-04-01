function StatusBadge({ status }) {
  const colorMap = {
    draft: 'badge-gray',
    open: 'badge-green',
    closed: 'badge-yellow',
    scored: 'badge-blue',
  };

  const className = `status-badge ${colorMap[status] || 'badge-gray'}`;

  return <span className={className}>{status}</span>;
}

export default StatusBadge;
