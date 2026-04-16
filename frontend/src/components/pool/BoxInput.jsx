import { Checkbox, Text, Stack, Paper, Group } from '@mantine/core';

function BoxPanel({ options, selected, maxSelections, onToggle }) {
  return (
    <Paper withBorder p="sm" style={{ flex: 1 }}>
      {maxSelections != null && (
        <Text size="xs" c={selected.length === maxSelections ? 'green' : 'dimmed'} mb="xs" ta="right">
          {selected.length} / {maxSelections}
        </Text>
      )}
      <Stack gap="xs">
        {options.map(opt => {
          const checked = selected.includes(opt.id);
          const atMax = maxSelections != null && selected.length >= maxSelections;
          return (
            <Checkbox
              key={opt.id}
              label={
                <div>
                  <Text size="sm">{opt.optionText}</Text>
                  {opt.subtext && <Text size="xs" c="dimmed">{opt.subtext}</Text>}
                </div>
              }
              checked={checked}
              onChange={() => onToggle(opt.id)}
              disabled={!checked && atMax}
            />
          );
        })}
        {options.length === 0 && <Text size="sm" c="dimmed">No options.</Text>}
      </Stack>
    </Paper>
  );
}

function BoxInput({ options, value = [], onChange, maxSelections }) {
  const box1 = options.filter(o => o.boxGroup === 1).sort((a, b) => a.displayOrder - b.displayOrder);
  const box2 = options.filter(o => o.boxGroup === 2).sort((a, b) => a.displayOrder - b.displayOrder);

  const box1Ids = new Set(box1.map(o => o.id));
  const box2Ids = new Set(box2.map(o => o.id));

  const sel1 = value.filter(id => box1Ids.has(id));
  const sel2 = value.filter(id => box2Ids.has(id));

  function toggle(optionId) {
    if (value.includes(optionId)) {
      onChange(value.filter(id => id !== optionId));
    } else {
      onChange([...value, optionId]);
    }
  }

  return (
    <Group align="flex-start" grow gap="sm">
      <BoxPanel options={box1} selected={sel1} maxSelections={maxSelections} onToggle={toggle} />
      <BoxPanel options={box2} selected={sel2} maxSelections={maxSelections} onToggle={toggle} />
    </Group>
  );
}

export default BoxInput;
