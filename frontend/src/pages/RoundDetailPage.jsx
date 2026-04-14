import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRound, updateRound, updateRoundStatus, getRounds } from '../api/rounds';
import { getQuestions, createQuestion, updateQuestion, deleteQuestion } from '../api/questions';
import { getOptions } from '../api/options';
import RoundForm from '../components/RoundForm';
import QuestionForm from '../components/QuestionForm';
import StatusBadge from '../components/StatusBadge';
import ConfirmDialog from '../components/ConfirmDialog';
import PreviewModal from '../components/PreviewModal';
import { Title, Table, Button, Group, Alert, Card, Anchor, Text } from '@mantine/core';

const STATUS_FORWARD = { draft: 'open', open: 'closed', closed: 'scored' };
const STATUS_BACKWARD = { scored: 'closed', closed: 'open', open: 'draft' };

function RoundDetailPage() {
  const { roundId } = useParams();
  const navigate = useNavigate();
  const [round, setRound] = useState(null);
  const [allRounds, setAllRounds] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [showQuestionForm, setShowQuestionForm] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [showPreview, setShowPreview] = useState(false);
  const [previewQuestions, setPreviewQuestions] = useState([]);
  const [error, setError] = useState(null);
  const dragItem = useRef(null);
  const dragOverItem = useRef(null);

  useEffect(() => { fetchData(); }, [roundId]);

  async function fetchData() {
    try {
      const [roundRes, roundsRes, questionsRes] = await Promise.all([getRound(roundId), getRounds(), getQuestions(roundId)]);
      setRound(roundRes.data);
      setAllRounds(roundsRes.data);
      setQuestions(questionsRes.data);
    } catch { setError('Failed to load round data'); }
  }

  async function handleUpdateRound(data) {
    try { await updateRound(roundId, data); fetchData(); }
    catch { setError('Failed to update round'); }
  }

  function getNextDisplayOrder() {
    if (questions.length === 0) return 1;
    return Math.max(...questions.map(q => q.displayOrder ?? 0)) + 1;
  }

  async function handleCreateQuestion(data) {
    try { await createQuestion(roundId, data); setShowQuestionForm(false); fetchData(); }
    catch { setError('Failed to create question'); }
  }

  async function handleDragEnd() {
    const fromIdx = dragItem.current;
    const toIdx = dragOverItem.current;
    dragItem.current = null;
    dragOverItem.current = null;
    if (fromIdx === null || toIdx === null || fromIdx === toIdx) return;
    const reordered = [...questions];
    const [moved] = reordered.splice(fromIdx, 1);
    reordered.splice(toIdx, 0, moved);
    const updates = reordered.map((q, i) => ({ ...q, displayOrder: i + 1 }));
    setQuestions(updates);
    try {
      await Promise.all(
        updates.filter((q) => q.displayOrder !== questions.find(oq => oq.id === q.id)?.displayOrder)
          .map(q => updateQuestion(roundId, q.id, { ...q }))
      );
      fetchData();
    } catch { setError('Failed to reorder questions'); fetchData(); }
  }

  async function handleDeleteQuestion(questionId) {
    try { await deleteQuestion(roundId, questionId); setConfirmDelete(null); fetchData(); }
    catch { setError('Failed to delete question'); }
  }

  async function handlePreview() {
    try {
      const withOptions = await Promise.all(questions.map(async (q) => {
        const optRes = await getOptions(q.id);
        return { ...q, options: optRes.data };
      }));
      setPreviewQuestions(withOptions);
      setShowPreview(true);
    } catch { setError('Failed to load preview'); }
  }

  if (!round) return <Text>Loading...</Text>;

  return (
    <div>
      <Button variant="subtle" size="compact-sm" onClick={() => navigate('/admin/rounds')} mb="sm">
        ← Back to Rounds
      </Button>

      <Group justify="space-between" mb="md">
        <Group>
          <Title order={1}>Round: {round.name}</Title>
          <StatusBadge status={round.status} />
        </Group>
        <Group>
          {STATUS_BACKWARD[round.status] && (
            <Button variant="default" size="sm" onClick={async () => { await updateRoundStatus(roundId, STATUS_BACKWARD[round.status]); fetchData(); }}>
              ← {STATUS_BACKWARD[round.status]}
            </Button>
          )}
          {STATUS_FORWARD[round.status] && (
            <Button size="sm" onClick={async () => { await updateRoundStatus(roundId, STATUS_FORWARD[round.status]); fetchData(); }}>
              {STATUS_FORWARD[round.status]} →
            </Button>
          )}
        </Group>
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      <Card withBorder mb="lg" padding="md">
        <Title order={3} mb="sm">Edit Round</Title>
        <RoundForm round={round} allRounds={allRounds} onSubmit={handleUpdateRound} onCancel={() => navigate('/admin/rounds')} />
      </Card>

      <Group justify="space-between" mb="sm">
        <Title order={2}>Questions</Title>
        <Group>
          {!showQuestionForm && <Button onClick={() => setShowQuestionForm(true)}>Add Question</Button>}
          {questions.length > 0 && <Button variant="default" onClick={handlePreview}>Preview</Button>}
        </Group>
      </Group>

      {showQuestionForm && (
        <Card withBorder mb="md" padding="md">
          <Title order={3} mb="sm">New Question</Title>
          <QuestionForm question={{ displayOrder: getNextDisplayOrder() }} allQuestions={questions} onSubmit={handleCreateQuestion} onCancel={() => setShowQuestionForm(false)} />
        </Card>
      )}

      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th style={{ width: 30 }}></Table.Th>
            <Table.Th>Title</Table.Th>
            <Table.Th>Type</Table.Th>
            <Table.Th>Mandatory</Table.Th>
            <Table.Th>Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {questions.map((q, idx) => (
            <Table.Tr
              key={q.id}
              draggable
              onDragStart={() => { dragItem.current = idx; }}
              onDragEnter={() => { dragOverItem.current = idx; }}
              onDragOver={(e) => e.preventDefault()}
              onDragEnd={handleDragEnd}
              style={{ cursor: 'grab' }}
            >
              <Table.Td style={{ cursor: 'grab', color: 'var(--mantine-color-gray-5)', textAlign: 'center' }}>☰</Table.Td>
              <Table.Td>
                <Anchor component="button" onClick={() => navigate(`/admin/rounds/${roundId}/questions/${q.id}`)}>
                  {q.title}
                </Anchor>
              </Table.Td>
              <Table.Td>{q.questionType?.replace(/_/g, ' ')}</Table.Td>
              <Table.Td>{q.isMandatory ? 'Yes' : 'No'}</Table.Td>
              <Table.Td>
                <Group gap="xs">
                  <Button size="compact-sm" variant="default" onClick={() => navigate(`/admin/rounds/${roundId}/questions/${q.id}`)}>Edit</Button>
                  <Button size="compact-sm" color="red" variant="light" onClick={() => setConfirmDelete(q.id)}>Delete</Button>
                </Group>
              </Table.Td>
            </Table.Tr>
          ))}
          {questions.length === 0 && (
            <Table.Tr><Table.Td colSpan={5} ta="center" c="dimmed">No questions yet.</Table.Td></Table.Tr>
          )}
        </Table.Tbody>
      </Table>

      <ConfirmDialog isOpen={confirmDelete !== null} message="Are you sure you want to delete this question?" onConfirm={() => handleDeleteQuestion(confirmDelete)} onCancel={() => setConfirmDelete(null)} />
      <PreviewModal isOpen={showPreview} onClose={() => setShowPreview(false)} questions={previewQuestions} roundName={round.name} />
    </div>
  );
}

export default RoundDetailPage;
