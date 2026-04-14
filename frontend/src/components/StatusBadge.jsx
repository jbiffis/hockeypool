import { Badge } from '@mantine/core';

const colorMap = {
  draft: 'gray',
  open: 'green',
  closed: 'yellow',
  scored: 'blue',
  active: 'green',
  archived: 'gray',
};

function StatusBadge({ status }) {
  return (
    <Badge color={colorMap[status] || 'gray'} variant="light">
      {status}
    </Badge>
  );
}

export default StatusBadge;
