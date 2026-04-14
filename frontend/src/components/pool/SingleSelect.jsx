import { Radio, Stack, Image, Group, Text } from '@mantine/core';

function SingleSelect({ options, value, onChange, name }) {
  return (
    <Radio.Group value={value != null ? String(value) : ''} onChange={(val) => onChange(Number(val))} name={name}>
      <Stack gap="xs">
        {options.map((opt) => (
          <Radio
            key={opt.id}
            value={String(opt.id)}
            label={
              <Group gap="sm">
                {opt.imageUrl && <Image src={opt.imageUrl} alt="" w={32} h={32} radius="sm" fit="contain" />}
                <Text size="sm">{opt.optionText}</Text>
              </Group>
            }
          />
        ))}
      </Stack>
    </Radio.Group>
  );
}

export default SingleSelect;
