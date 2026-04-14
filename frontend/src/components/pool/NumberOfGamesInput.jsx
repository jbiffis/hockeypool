import { SegmentedControl } from '@mantine/core';

const GAME_OPTIONS = ['4', '5', '6', '7'];

function NumberOfGamesInput({ value, onChange }) {
  return (
    <SegmentedControl
      value={value || ''}
      onChange={onChange}
      data={GAME_OPTIONS}
      fullWidth
    />
  );
}

export default NumberOfGamesInput;
