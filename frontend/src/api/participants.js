import api from './client';

export function getParticipants(seasonId) {
  const params = {};
  if (seasonId) params.seasonId = seasonId;
  return api.get('/admin/participants', { params });
}

export function getParticipantResponses(participantId) {
  return api.get(`/admin/participants/${participantId}/responses`);
}
