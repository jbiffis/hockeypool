import { Select } from '@mantine/core';

function BestTeamNameInput({ options, value = null, onChange, name }) {
  const data = options.map((opt) => ({ value: String(opt.id), label: opt.optionText }));
  return (
    <Select
      name={name}
      data={data}
      value={value != null ? String(value) : null}
      onChange={(val) => onChange(val ? Number(val) : null)}
      placeholder="Select a team name"
      searchable
      clearable
      nothingFoundMessage="No matches"
    />
  );
}

export default BestTeamNameInput;
