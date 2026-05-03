import { UnstyledButton, Radio, Text, Group, SimpleGrid } from '@mantine/core';
import classes from './MultiSelect.module.css';

function BestTeamNameInput({ options, value = null, onChange, name }) {
  return (
    <div>
      <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="xs">
        {options.map((opt) => {
          const checked = value === opt.id;
          return (
            <UnstyledButton
              key={opt.id}
              onClick={() => onChange(opt.id)}
              className={classes.option}
              data-checked={checked || undefined}
            >
              <div style={{ flex: 1 }}>
                <Text fw={500} size="sm" lh={1.3}>
                  {opt.optionText}
                </Text>
              </div>
              <Group gap="sm" ml="md">
                <Radio
                  checked={checked}
                  onChange={() => {}}
                  tabIndex={-1}
                  styles={{ radio: { cursor: 'pointer' } }}
                  aria-label={opt.optionText}
                  name={name}
                />
              </Group>
            </UnstyledButton>
          );
        })}
      </SimpleGrid>
    </div>
  );
}

export default BestTeamNameInput;
