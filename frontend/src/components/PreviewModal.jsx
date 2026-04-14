import { Modal, Stack } from '@mantine/core';
import QuestionCard from './pool/QuestionCard';

function PreviewModal({ isOpen, onClose, questions, roundName }) {
  if (!questions || questions.length === 0) return null;

  return (
    <Modal
      opened={isOpen}
      onClose={onClose}
      title={`Preview: ${roundName || 'Round'}`}
      size="lg"
      centered
    >
      <Stack>
        {questions.map((q) => (
          <QuestionCard
            key={q.id}
            question={q}
            answer={{}}
            error={null}
            onChange={() => {}}
          />
        ))}
      </Stack>
    </Modal>
  );
}

export default PreviewModal;
