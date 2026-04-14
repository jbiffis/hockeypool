import api from './client';

export function getParticipants(seasonId) {
  const params = {};
  if (seasonId) params.seasonId = seasonId;
  return api.get('/admin/participants', { params });
}

export function updateParticipantPaid(id, paid) {
  return api.patch(`/admin/participants/${id}/paid`, { paid });
}

export function getParticipantResponses(participantId) {
  return api.get(`/admin/participants/${participantId}/responses`);
}
