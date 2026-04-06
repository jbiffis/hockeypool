import { Routes, Route } from 'react-router-dom';
import './App.css';
import PoolFormPage from './pages/PoolFormPage';
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
      </Route>
    </Routes>
  );
}

export default App;
