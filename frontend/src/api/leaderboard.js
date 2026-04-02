import api from './client';

export function getSeasons() {
  return api.get('/seasons');
}

export function getLeaderboard(seasonId, divisionId) {
  return api.get(`/leaderboard/${seasonId}`, divisionId ? { params: { divisionId } } : undefined);
}

export function getPublicParticipantResponses(participantId) {
  return api.get(`/participants/${participantId}/responses`);
}

export function getQuestionDetail(questionId) {
  return api.get(`/questions/${questionId}`);
}
