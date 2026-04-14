import { TextInput } from '@mantine/core';

function FreeFormInput({ value = '', onChange }) {
  return (
    <TextInput
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder="Your answer"
    />
  );
}

export default FreeFormInput;
