import { UnstyledButton, Checkbox, Text, Image, Group, Badge, SimpleGrid } from '@mantine/core';
import classes from './MultiSelect.module.css';

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
    <div>
      {maxSelections != null && (
        <Text size="sm" c="dimmed" mb="xs">
          Select {maxSelections}
        </Text>
      )}
      <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="xs">
      {options.map((opt) => {
        const checked = value.includes(opt.id);
        const disabled = !checked && atLimit;
        return (
          <UnstyledButton
            key={opt.id}
            onClick={() => !disabled && handleToggle(opt.id)}
            className={classes.option}
            data-checked={checked || undefined}
            data-disabled={disabled || undefined}
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
    </SimpleGrid>
    </div>
  );
}

export default MultiSelect;
