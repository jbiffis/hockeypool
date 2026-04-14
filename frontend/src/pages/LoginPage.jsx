import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Container, Title, PasswordInput, Button, Alert, Stack } from '@mantine/core';

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
    <Container size="xs" mt={120}>
      <Title order={1} mb="lg">Admin Login</Title>
      <form onSubmit={handleSubmit}>
        <Stack>
          {error && <Alert color="red">{error}</Alert>}
          <PasswordInput
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoFocus
          />
          <Button type="submit" loading={submitting}>
            Log In
          </Button>
        </Stack>
      </form>
    </Container>
  );
}

export default LoginPage;
