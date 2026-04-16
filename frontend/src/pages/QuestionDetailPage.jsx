import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getQuestion, updateQuestion, getQuestions } from '../api/questions';
import { getOptions, createOption, updateOption, deleteOption } from '../api/options';
import QuestionForm from '../components/QuestionForm';
import OptionForm from '../components/OptionForm';
import ConfirmDialog from '../components/ConfirmDialog';
import PreviewModal from '../components/PreviewModal';
import { Title, Card, Button, Group, Alert, Text, Badge, Image, Stack } from '@mantine/core';

function QuestionDetailPage() {
  const { roundId, questionId } = useParams();
  const navigate = useNavigate();
  const [question, setQuestion] = useState(null);
  const [allQuestions, setAllQuestions] = useState([]);
  const [options, setOptions] = useState([]);
  const [showOptionForm, setShowOptionForm] = useState(false);
  const [editingOptionId, setEditingOptionId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [showPreview, setShowPreview] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { fetchData(); }, [roundId, questionId]);

  async function fetchData() {
    try {
      const [questionRes, questionsRes, optionsRes] = await Promise.all([
        getQuestion(roundId, questionId), getQuestions(roundId), getOptions(questionId),
      ]);
      setQuestion(questionRes.data);
      setAllQuestions(questionsRes.data);
      setOptions(optionsRes.data);
    } catch { setError('Failed to load question data'); }
  }

  async function handleUpdateQuestion(data) {
    try { await updateQuestion(roundId, questionId, data); fetchData(); }
    catch { setError('Failed to update question'); }
  }

  async function handleCreateOption(data) {
    try { await createOption(questionId, data); setShowOptionForm(false); fetchData(); }
    catch { setError('Failed to create option'); }
  }

  async function handleUpdateOption(optionId, data) {
    try { await updateOption(questionId, optionId, data); setEditingOptionId(null); fetchData(); }
    catch { setError('Failed to update option'); }
  }

  async function handleDeleteOption(optionId) {
    try { await deleteOption(questionId, optionId); setConfirmDelete(null); fetchData(); }
    catch { setError('Failed to delete option'); }
  }

  if (!question) return <Text>Loading...</Text>;

  return (
    <div>
      <Button variant="subtle" size="compact-sm" onClick={() => navigate(`/admin/rounds/${roundId}`)} mb="sm">
        ← Back to Round
      </Button>

      <Group justify="space-between" mb="md">
        <Title order={1}>Question: {question.title}</Title>
        {options.length > 0 && <Button variant="default" onClick={() => setShowPreview(true)}>Preview</Button>}
      </Group>

      {error && <Alert color="red" mb="md">{error}</Alert>}

      <Card withBorder mb="lg" padding="md">
        <Title order={3} mb="sm">Edit Question</Title>
        <QuestionForm question={question} allQuestions={allQuestions} onSubmit={handleUpdateQuestion} onCancel={() => navigate(`/admin/rounds/${roundId}`)} />
      </Card>

      <Group justify="space-between" mb="sm">
        <Title order={2}>Options</Title>
        {!showOptionForm && <Button onClick={() => setShowOptionForm(true)}>Add Option</Button>}
      </Group>

      <Stack gap="sm">
        {options.map(opt =>
          editingOptionId === opt.id ? (
            <Card key={opt.id} withBorder padding="sm">
              <OptionForm option={opt} questionType={question.questionType} onSubmit={(data) => handleUpdateOption(opt.id, data)} onCancel={() => setEditingOptionId(null)} />
            </Card>
          ) : (
            <Card key={opt.id} withBorder padding="sm">
              <Group justify="space-between">
                <Group>
                  <Text size="sm" c="dimmed">#{opt.displayOrder}</Text>
                  <Text size="sm" fw={500}>{opt.optionText}</Text>
                  {opt.points != null && <Badge variant="light" color="blue" size="sm">{opt.points} pts</Badge>}
                  {opt.boxGroup != null && <Badge variant="outline" color="gray" size="sm">Box {opt.boxGroup}</Badge>}
                </Group>
                <Group gap="xs">
                  <Button size="compact-sm" variant="default" onClick={() => setEditingOptionId(opt.id)}>Edit</Button>
                  <Button size="compact-sm" color="red" variant="light" onClick={() => setConfirmDelete(opt.id)}>Delete</Button>
                </Group>
              </Group>
              {opt.imageUrl && <Image src={opt.imageUrl} alt="" maw={100} mah={60} fit="contain" mt="xs" />}
            </Card>
          )
        )}
        {options.length === 0 && <Text c="dimmed" ta="center">No options yet.</Text>}
      </Stack>

      {showOptionForm && (
        <Card withBorder mt="md" padding="md">
          <Title order={4} mb="sm">New Option</Title>
          <OptionForm option={null} questionType={question.questionType} onSubmit={handleCreateOption} onCancel={() => setShowOptionForm(false)} />
        </Card>
      )}

      <ConfirmDialog isOpen={confirmDelete !== null} message="Are you sure you want to delete this option?" onConfirm={() => handleDeleteOption(confirmDelete)} onCancel={() => setConfirmDelete(null)} />
      <PreviewModal isOpen={showPreview} onClose={() => setShowPreview(false)} questions={[{ ...question, options }]} roundName="Question Preview" />
    </div>
  );
}

export default QuestionDetailPage;
