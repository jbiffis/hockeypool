import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { lookupParticipant, registerParticipant, getPoolForm, submitPicks } from '../api/pool';
import QuestionCard from '../components/pool/QuestionCard';
import { Container, Title, TextInput, Button, Card, Alert, Stack, Text, Divider } from '@mantine/core';

function PoolFormPage() {
  const { roundId, seasonId } = useParams();
  const [step, setStep] = useState('email');
  const [email, setEmail] = useState('');
  const [participant, setParticipant] = useState(null);
  const [isNew, setIsNew] = useState(false);
  const [name, setName] = useState('');
  const [teamName, setTeamName] = useState('');
  const [form, setForm] = useState(null);
  const [answers, setAnswers] = useState({});
  const [errors, setErrors] = useState({});
  const [globalError, setGlobalError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleEmailSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) return;
    setGlobalError(null);
    setLoading(true);
    try {
      const res = await lookupParticipant(email.trim(), seasonId ? Number(seasonId) : undefined);
      setParticipant(res.data);
      setIsNew(false);
      setStep('identify');
    } catch (err) {
      if (err.response && err.response.status === 404) {
        setIsNew(true);
        setStep('identify');
      } else {
        setGlobalError(err.response?.data?.message || 'Something went wrong. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!name.trim() || !teamName.trim()) return;
    setGlobalError(null);
    setLoading(true);
    try {
      const res = await registerParticipant(email.trim(), name.trim(), teamName.trim(), seasonId ? Number(seasonId) : undefined);
      setParticipant(res.data);
      const formRes = await getPoolForm(res.data.id, roundId, seasonId ? Number(seasonId) : undefined);
      if (formRes.data.alreadySubmitted) { setForm(formRes.data); setStep('already'); }
      else { setForm(formRes.data); setStep('questions'); }
    } catch (err) {
      setGlobalError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleContinue = async () => {
    setGlobalError(null);
    setLoading(true);
    try {
      const formRes = await getPoolForm(participant.id, roundId, seasonId ? Number(seasonId) : undefined);
      if (formRes.data.alreadySubmitted) { setForm(formRes.data); setStep('already'); }
      else { setForm(formRes.data); setStep('questions'); }
    } catch (err) {
      setGlobalError(err.response?.data?.message || 'Failed to load form. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (questionId, answerUpdate) => {
    setAnswers((prev) => ({ ...prev, [questionId]: answerUpdate }));
    setErrors((prev) => { const next = { ...prev }; delete next[questionId]; return next; });
  };

  const validate = () => {
    const newErrors = {};
    if (!form || !form.questions) return newErrors;
    for (const q of form.questions) {
      const a = answers[q.id];
      if (q.isMandatory) {
        if (q.questionType === 'multi_select') {
          if (!a || !a.selectedOptionIds || a.selectedOptionIds.length === 0) newErrors[q.id] = 'Please select at least one option.';
        } else if (q.questionType === 'free_form' || q.questionType === 'number_of_games') {
          if (!a || !a.freeFormValue || !a.freeFormValue.trim()) newErrors[q.id] = 'This question is required.';
        } else if (q.questionType === 'jeopardy') {
          if (!a || !a.selectedOptionId) newErrors[q.id] = 'Please select an answer.';
          else if (!a.freeFormValue || !a.freeFormValue.toString().trim()) newErrors[q.id] = 'Please enter a wager amount.';
          else {
            const wager = Number(a.freeFormValue);
            if (isNaN(wager) || wager < 1 || wager > q.maxWager) newErrors[q.id] = `Wager must be between 1 and ${q.maxWager}.`;
          }
        }
      }
      if (q.questionType === 'multi_select' && q.maxSelections != null && a && a.selectedOptionIds) {
        if (a.selectedOptionIds.length > q.maxSelections) newErrors[q.id] = `You can select at most ${q.maxSelections} option${q.maxSelections === 1 ? '' : 's'}.`;
      }
      if (q.questionType === 'jeopardy' && a && a.freeFormValue) {
        const wager = Number(a.freeFormValue);
        if (isNaN(wager) || wager < 1 || wager > q.maxWager) newErrors[q.id] = `Wager must be between 1 and ${q.maxWager}.`;
      }
    }
    return newErrors;
  };

  const handleSubmitPicks = async () => {
    setGlobalError(null);
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      const firstErrorId = form.questions.find((q) => validationErrors[q.id])?.id;
      if (firstErrorId) {
        const el = document.getElementById(`question-${firstErrorId}`);
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      return;
    }
    setLoading(true);
    try {
      const answersArray = form.questions
        .filter((q) => answers[q.id])
        .map((q) => {
          const a = answers[q.id];
          return { questionId: q.id, selectedOptionId: a.selectedOptionId || null, selectedOptionIds: a.selectedOptionIds || null, freeFormValue: a.freeFormValue || null };
        });
      await submitPicks(participant.id, answersArray);
      setStep('submitted');
    } catch (err) {
      setGlobalError(err.response?.data?.message || 'Failed to submit picks. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const formatDeadline = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: '2-digit' });
  };

  if (step === 'email') {
    return (
      <Container size="xs" mt={80}>
        <Card withBorder padding="xl" radius="md">
          <Title order={1} ta="center" mb="sm">Playoff Pool</Title>
          <Text ta="center" c="dimmed" mb="md">Enter your email to get started</Text>
          <Text ta="center" size="xs" c="dimmed" mb="lg">Make sure to use the same email each time so your picks are recorded correctly.</Text>
          {globalError && <Alert color="red" mb="md">{globalError}</Alert>}
          <form onSubmit={handleEmailSubmit}>
            <Stack>
              <TextInput label="Email address" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
              <Button type="submit" loading={loading} fullWidth>Continue</Button>
            </Stack>
          </form>
        </Card>
      </Container>
    );
  }

  if (step === 'identify') {
    if (isNew) {
      return (
        <Container size="xs" mt={80}>
          <Card withBorder padding="xl" radius="md">
            <Title order={1} ta="center" mb="sm">Welcome!</Title>
            <Text ta="center" c="dimmed" mb="lg">Let&apos;s set up your profile</Text>
            {globalError && <Alert color="red" mb="md">{globalError}</Alert>}
            <form onSubmit={handleRegister}>
              <Stack>
                <TextInput label="Your name" value={name} onChange={(e) => setName(e.target.value)} required autoFocus />
                <TextInput label="Team name" value={teamName} onChange={(e) => setTeamName(e.target.value)} required />
                <Button type="submit" loading={loading} fullWidth>Register & Continue</Button>
              </Stack>
            </form>
          </Card>
        </Container>
      );
    }
    return (
      <Container size="xs" mt={80}>
        <Card withBorder padding="xl" radius="md" ta="center">
          <Title order={1} mb="sm">Welcome back, {participant.name}!</Title>
          <Text c="dimmed" mb="lg">Your team: {participant.teamName}</Text>
          {globalError && <Alert color="red" mb="md">{globalError}</Alert>}
          <Button loading={loading} fullWidth onClick={handleContinue}>Continue to picks</Button>
        </Card>
      </Container>
    );
  }

  if (step === 'already') {
    return (
      <Container size="xs" mt={80}>
        <Card withBorder padding="xl" radius="md" ta="center">
          <Title order={1} mb="sm">Already Submitted</Title>
          <Text c="dimmed">You&apos;ve already submitted your picks for this round.</Text>
        </Card>
      </Container>
    );
  }

  if (step === 'questions' && form) {
    let currentRoundId = null;
    return (
      <Container size="sm" mt="md" mb="xl">
        <Title order={1} ta="center" mb="xs">{form.roundName || 'Playoff Pool'}</Title>
        {form.deadline && <Text ta="center" c="dimmed" mb="lg">Picks are due by {formatDeadline(form.deadline)}</Text>}
        {globalError && <Alert color="red" mb="md">{globalError}</Alert>}
        <Stack gap="md">
          {form.questions.map((q) => {
            let sectionHeader = null;
            if (q.roundId && q.roundId !== currentRoundId) {
              currentRoundId = q.roundId;
              sectionHeader = <Divider label={q.roundName || `Round ${q.roundId}`} labelPosition="center" key={`section-${q.roundId}`} />;
            }
            return (
              <div key={q.id}>
                {sectionHeader}
                <QuestionCard question={q} answer={answers[q.id]} error={errors[q.id] || null} onChange={(update) => handleAnswerChange(q.id, update)} />
              </div>
            );
          })}
        </Stack>
        <Button fullWidth mt="lg" size="lg" loading={loading} onClick={handleSubmitPicks}>
          Submit Picks
        </Button>
      </Container>
    );
  }

  if (step === 'submitted') {
    return (
      <Container size="xs" mt={80}>
        <Card withBorder padding="xl" radius="md" ta="center">
          <Title order={1} mb="sm">Your picks have been submitted!</Title>
          <Text c="dimmed">Thank you, {participant.name}! Good luck!</Text>
        </Card>
      </Container>
    );
  }

  return null;
}

export default PoolFormPage;
