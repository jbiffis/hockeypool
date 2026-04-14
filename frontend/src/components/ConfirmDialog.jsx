import { Modal, Text, Group, Button } from '@mantine/core';

function ConfirmDialog({ isOpen, message, onConfirm, onCancel }) {
  return (
    <Modal opened={isOpen} onClose={onCancel} title="Confirm" centered size="sm">
      <Text mb="lg">{message || 'Are you sure?'}</Text>
      <Group justify="flex-end">
        <Button variant="default" onClick={onCancel}>Cancel</Button>
        <Button color="red" onClick={onConfirm}>Confirm</Button>
      </Group>
    </Modal>
  );
}

export default ConfirmDialog;
