import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { lookupParticipant, registerParticipant, getPoolForm, submitPicks } from '../api/pool';
import QuestionCard from '../components/pool/QuestionCard';

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
      if (formRes.data.alreadySubmitted) {
        setForm(formRes.data);
        setStep('already');
      } else {
        setForm(formRes.data);
        setStep('questions');
      }
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
      if (formRes.data.alreadySubmitted) {
        setForm(formRes.data);
        setStep('already');
      } else {
        setForm(formRes.data);
        setStep('questions');
      }
    } catch (err) {
      setGlobalError(err.response?.data?.message || 'Failed to load form. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (questionId, answerUpdate) => {
    setAnswers((prev) => ({ ...prev, [questionId]: answerUpdate }));
    setErrors((prev) => {
      const next = { ...prev };
      delete next[questionId];
      return next;
    });
  };

  const validate = () => {
    const newErrors = {};
    if (!form || !form.questions) return newErrors;
    for (const q of form.questions) {
      const a = answers[q.id];
      if (q.isMandatory) {
        if (q.questionType === 'multi_select') {
          if (!a || !a.selectedOptionIds || a.selectedOptionIds.length === 0) {
            newErrors[q.id] = 'Please select at least one option.';
          }
        } else if (q.questionType === 'free_form') {
          if (!a || !a.freeFormValue || !a.freeFormValue.trim()) {
            newErrors[q.id] = 'This question is required.';
          }
        } else if (q.questionType === 'jeopardy') {
          if (!a || !a.selectedOptionId) {
            newErrors[q.id] = 'Please select an answer.';
          } else if (!a.freeFormValue || !a.freeFormValue.toString().trim()) {
            newErrors[q.id] = 'Please enter a wager amount.';
          } else {
            const wager = Number(a.freeFormValue);
            if (isNaN(wager) || wager < 1 || wager > q.maxWager) {
              newErrors[q.id] = `Wager must be between 1 and ${q.maxWager}.`;
            }
          }
        }
      }
      // Validate max selections
      if (q.questionType === 'multi_select' && q.maxSelections != null && a && a.selectedOptionIds) {
        if (a.selectedOptionIds.length > q.maxSelections) {
          newErrors[q.id] = `You can select at most ${q.maxSelections} option${q.maxSelections === 1 ? '' : 's'}.`;
        }
      }
      // Validate jeopardy wager even if not mandatory, if partially filled
      if (q.questionType === 'jeopardy' && a && a.freeFormValue) {
        const wager = Number(a.freeFormValue);
        if (isNaN(wager) || wager < 1 || wager > q.maxWager) {
          newErrors[q.id] = `Wager must be between 1 and ${q.maxWager}.`;
        }
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
          return {
            questionId: q.id,
            selectedOptionId: a.selectedOptionId || null,
            selectedOptionIds: a.selectedOptionIds || null,
            freeFormValue: a.freeFormValue || null,
          };
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
    const d = new Date(dateStr);
    return d.toLocaleString(undefined, {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  // ---- RENDER ----

  if (step === 'email') {
    return (
      <div className="pool-card pool-card-centered">
        <h1>Playoff Pool</h1>
        <p>Enter your email to get started</p>
        <p className="pool-email-note">
          Make sure to use the same email each time so your picks are recorded correctly.
        </p>
        {globalError && <div className="pool-global-error">{globalError}</div>}
        <form onSubmit={handleEmailSubmit}>
          <div className="pool-input-group">
            <label htmlFor="email">Email address</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
            />
          </div>
          <button type="submit" className="pool-btn" disabled={loading}>
            {loading ? 'Loading...' : 'Continue'}
          </button>
        </form>
      </div>
    );
  }

  if (step === 'identify') {
    if (isNew) {
      return (
        <div className="pool-card pool-card-centered">
          <h1>Welcome!</h1>
          <p>Let&apos;s set up your profile</p>
          {globalError && <div className="pool-global-error">{globalError}</div>}
          <form onSubmit={handleRegister}>
            <div className="pool-input-group">
              <label htmlFor="name">Your name</label>
              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoFocus
              />
            </div>
            <div className="pool-input-group">
              <label htmlFor="teamName">Team name</label>
              <input
                id="teamName"
                type="text"
                value={teamName}
                onChange={(e) => setTeamName(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="pool-btn" disabled={loading}>
              {loading ? 'Loading...' : 'Register & Continue'}
            </button>
          </form>
        </div>
      );
    }

    // Returning user
    return (
      <div className="pool-card pool-card-centered">
        <h1>Welcome back, {participant.name}!</h1>
        <div className="pool-welcome-back">
          <div className="team-display">Your team: {participant.teamName}</div>
        </div>
        {globalError && <div className="pool-global-error">{globalError}</div>}
        <button className="pool-btn" onClick={handleContinue} disabled={loading}>
          {loading ? 'Loading...' : 'Continue to picks'}
        </button>
      </div>
    );
  }

  if (step === 'already') {
    return (
      <div className="pool-card pool-card-centered pool-already-submitted">
        <h1>Already Submitted</h1>
        <p>You&apos;ve already submitted your picks for this round.</p>
      </div>
    );
  }

  if (step === 'questions' && form) {
    let currentRoundId = null;

    return (
      <div className="pool-container">
        <div className="pool-header">
          <h1>{form.roundName || 'Playoff Pool'}</h1>
          {form.deadline && (
            <p className="pool-deadline">Picks are due by {formatDeadline(form.deadline)}</p>
          )}
        </div>

        {globalError && <div className="pool-global-error">{globalError}</div>}

        {form.questions.map((q) => {
          let sectionHeader = null;
          if (q.roundId && q.roundId !== currentRoundId) {
            currentRoundId = q.roundId;
            sectionHeader = (
              <div className="pool-section-divider" key={`section-${q.roundId}`}>
                {q.roundName || `Round ${q.roundId}`}
              </div>
            );
          }

          return (
            <div key={q.id}>
              {sectionHeader}
              <QuestionCard
                question={q}
                answer={answers[q.id]}
                error={errors[q.id] || null}
                onChange={(update) => handleAnswerChange(q.id, update)}
              />
            </div>
          );
        })}

        <div style={{ textAlign: 'center' }}>
          <button
            className="pool-btn pool-btn-submit"
            onClick={handleSubmitPicks}
            disabled={loading}
          >
            {loading ? 'Submitting...' : 'Submit Picks'}
          </button>
        </div>
      </div>
    );
  }

  if (step === 'submitted') {
    return (
      <div className="pool-card pool-card-centered pool-success">
        <h1>Your picks have been submitted!</h1>
        <p>Thank you, {participant.name}! Good luck!</p>
      </div>
    );
  }

  return null;
}

export default PoolFormPage;
