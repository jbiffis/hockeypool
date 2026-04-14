import { Radio, Stack, NumberInput, Group, Text, Image } from '@mantine/core';

function JeopardyInput({ options, selectedOptionId, wagerValue = '', maxWager, onOptionChange, onWagerChange, name }) {
  return (
    <Stack gap="sm">
      <Radio.Group
        value={selectedOptionId != null ? String(selectedOptionId) : ''}
        onChange={(val) => onOptionChange(Number(val))}
        name={name}
      >
        <Stack gap="xs">
          {options.map((opt) => (
            <Radio
              key={opt.id}
              value={String(opt.id)}
              label={
                <Group gap="sm">
                  {opt.imageUrl && <Image src={opt.imageUrl} alt="" w={32} h={32} radius="sm" fit="contain" />}
                  <div>
                    <Text size="sm">{opt.optionText}</Text>
                    {opt.subtext && <Text size="xs" c="dimmed">{opt.subtext}</Text>}
                  </div>
                </Group>
              }
            />
          ))}
        </Stack>
      </Radio.Group>
      <NumberInput
        label={`Your wager (max: ${maxWager})`}
        value={wagerValue !== '' ? Number(wagerValue) : ''}
        onChange={(val) => onWagerChange(val != null ? String(val) : '')}
        min={1}
        max={maxWager}
        style={{ maxWidth: 200 }}
      />
    </Stack>
  );
}

export default JeopardyInput;
