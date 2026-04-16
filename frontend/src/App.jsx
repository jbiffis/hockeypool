import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import PoolFormPage from './pages/PoolFormPage';
import LoginPage from './pages/LoginPage';
import AdminLayout from './components/AdminLayout';
import AdminDashboard from './pages/AdminDashboard';
import SeasonsPage from './pages/SeasonsPage';
import RoundsPage from './pages/RoundsPage';
import RoundDetailPage from './pages/RoundDetailPage';
import QuestionDetailPage from './pages/QuestionDetailPage';
import ParticipantsPage from './pages/ParticipantsPage';
import ParticipantDetailPage from './pages/ParticipantDetailPage';
import DivisionsPage from './pages/DivisionsPage';
import ResponsesPage from './pages/ResponsesPage';
import LeaderboardPage from './pages/LeaderboardPage';
import PublicParticipantDetailPage from './pages/PublicParticipantDetailPage';
import QuestionPage from './pages/QuestionPage';
import SignupPage from './pages/SignupPage';
import UploadsPage from './pages/UploadsPage';

function ProtectedRoute() {
  const { authenticated, loading } = useAuth();
  if (loading) return null;
  if (!authenticated) return <Navigate to="/admin/login" replace />;
  return <Outlet />;
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<PoolFormPage />} />
      <Route path="/pool/season/:seasonId" element={<PoolFormPage />} />
      <Route path="/pool/season/:seasonId/round/:roundId" element={<PoolFormPage />} />
      <Route path="/pool/round/:roundId" element={<PoolFormPage />} />
      <Route path="/season/:seasonId/signup" element={<SignupPage />} />
      <Route path="/standings/:seasonId" element={<LeaderboardPage />} />
      <Route path="/standings/:seasonId/participant/:participantId" element={<PublicParticipantDetailPage />} />
      <Route path="/standings/:seasonId/question/:questionId" element={<QuestionPage />} />
      <Route path="/admin/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="seasons" element={<SeasonsPage />} />
          <Route path="rounds" element={<RoundsPage />} />
          <Route path="rounds/:roundId" element={<RoundDetailPage />} />
          <Route path="rounds/:roundId/questions/:questionId" element={<QuestionDetailPage />} />
          <Route path="participants" element={<ParticipantsPage />} />
          <Route path="participants/:participantId" element={<ParticipantDetailPage />} />
          <Route path="divisions" element={<DivisionsPage />} />
          <Route path="rounds/:roundId/responses" element={<ResponsesPage />} />
          <Route path="uploads" element={<UploadsPage />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
