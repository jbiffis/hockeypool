import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getSeasonSignup, submitSignup } from '../api/seasons';
import '../Signup.css';

function SignupPage() {
  const { seasonId } = useParams();
  const [season, setSeason] = useState(null);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState(null); // 'success' | 'duplicate' | 'error'
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getSeasonSignup(seasonId)
      .then(res => { setSeason(res.data); setLoading(false); })
      .catch(() => setLoading(false));
  }, [seasonId]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setStatus(null);
    try {
      await submitSignup(seasonId, email);
      setStatus('success');
    } catch (err) {
      if (err.response?.status === 409) {
        setStatus('duplicate');
      } else {
        setStatus('error');
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="su-page">
        <div className="su-loading">Loading&hellip;</div>
      </div>
    );
  }

  if (!season) {
    return (
      <div className="su-page">
        <div className="su-not-found">Season not found.</div>
      </div>
    );
  }

  return (
    <div className="su-page">
      <header className="su-header">
        <div className="su-header-inner">
          <div className="su-eyebrow">Playoff Pool</div>
          <h1 className="su-season-name">{season.name}</h1>
        </div>
      </header>

      <main className="su-main">
        {season.signupContent && (
          <div className="su-content">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {season.signupContent}
            </ReactMarkdown>
          </div>
        )}

        <div className="su-form-section">
          {status === 'success' ? (
            <div className="su-success">
              <div className="su-success-icon">&#10003;</div>
              <div className="su-success-heading">You&rsquo;re in.</div>
              <p className="su-success-body">
                We&rsquo;ve got your spot locked in for the {season.name} pool.
                We&rsquo;ll be in touch with next steps.
              </p>
            </div>
          ) : (
            <>
              <div className="su-form-heading">Reserve your spot</div>
              <form className="su-form" onSubmit={handleSubmit}>
                <div className="su-field-row">
                  <input
                    className="su-email-input"
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                    disabled={submitting}
                  />
                  <button
                    className="su-submit-btn"
                    type="submit"
                    disabled={submitting}
                  >
                    {submitting ? 'Signing up\u2026' : 'Sign me up'}
                  </button>
                </div>
                {status === 'duplicate' && (
                  <p className="su-field-error">That email is already registered for this season.</p>
                )}
                {status === 'error' && (
                  <p className="su-field-error">Something went wrong. Please try again.</p>
                )}
              </form>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

export default SignupPage;
