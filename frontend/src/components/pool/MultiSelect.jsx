import { UnstyledButton, Checkbox, Text, Image, Group, Badge, Stack } from '@mantine/core';

function MultiSelect({ options, value = [], onChange, name, maxSelections }) {
  const atLimit = maxSelections != null && value.length >= maxSelections;

  const handleToggle = (optId) => {
    if (value.includes(optId)) {
      onChange(value.filter((id) => id !== optId));
    } else if (!atLimit) {
      onChange([...value, optId]);
    }
  };

  return (
    <Stack gap="xs">
      {maxSelections != null && (
        <Text size="sm" c="dimmed">
          Select {maxSelections}
        </Text>
      )}
      {options.map((opt) => {
        const checked = value.includes(opt.id);
        const disabled = !checked && atLimit;
        return (
          <UnstyledButton
            key={opt.id}
            onClick={() => !disabled && handleToggle(opt.id)}
            data-checked={checked || undefined}
            style={{
              display: 'flex',
              alignItems: 'center',
              width: '100%',
              padding: 'var(--mantine-spacing-sm)',
              borderRadius: 'var(--mantine-radius-sm)',
              border: `1px solid ${checked ? 'var(--mantine-color-blue-filled)' : 'var(--mantine-color-gray-3)'}`,
              backgroundColor: checked ? 'var(--mantine-color-blue-light)' : 'var(--mantine-color-white)',
              opacity: disabled ? 0.45 : 1,
              cursor: disabled ? 'not-allowed' : 'pointer',
              transition: 'background-color 150ms ease, border-color 150ms ease',
            }}
          >
            {opt.imageUrl && (
              <Image src={opt.imageUrl} alt="" w={40} h={40} radius="sm" fit="contain" />
            )}
            <div style={{ flex: 1, marginLeft: opt.imageUrl ? 'var(--mantine-spacing-md)' : 0 }}>
              <Text fw={500} size="sm" lh={1.3}>
                {opt.optionText}
              </Text>
              {opt.subtext && (
                <Text c="dimmed" size="xs" lh={1.3} mt={2}>
                  {opt.subtext}
                </Text>
              )}
            </div>
            <Group gap="sm" ml="md">
              {opt.points != null && (
                <Badge variant="light" color="blue" size="sm">{opt.points} pts</Badge>
              )}
              <Checkbox
                checked={checked}
                onChange={() => {}}
                tabIndex={-1}
                styles={{ input: { cursor: 'pointer' } }}
                aria-label={opt.optionText}
              />
            </Group>
          </UnstyledButton>
        );
      })}
    </Stack>
  );
}

export default MultiSelect;
