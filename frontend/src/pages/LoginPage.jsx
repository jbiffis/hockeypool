import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function LoginPage() {
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(password);
      navigate('/admin');
    } catch {
      setError('Invalid password');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{ maxWidth: 360, margin: '120px auto', padding: '0 1rem' }}>
      <h1>Admin Login</h1>
      <form onSubmit={handleSubmit}>
        {error && <div className="error-message">{error}</div>}
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
            autoFocus
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Logging in\u2026' : 'Log In'}
        </button>
      </form>
    </div>
  );
}

export default LoginPage;
