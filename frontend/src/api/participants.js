import api from './client';

export function getParticipants(seasonId) {
  const params = {};
  if (seasonId) params.seasonId = seasonId;
  return api.get('/admin/participants', { params });
}

export function updateParticipant(id, data) {
  return api.put(`/admin/participants/${id}`, data);
}

export function updateParticipantPaid(id, paid) {
  return api.patch(`/admin/participants/${id}/paid`, { paid });
}

export function deleteParticipant(id) {
  return api.delete(`/admin/participants/${id}`);
}

export function getParticipantResponses(participantId) {
  return api.get(`/admin/participants/${participantId}/responses`);
}

export function deleteParticipantResponse(participantId, roundId) {
  return api.delete(`/admin/participants/${participantId}/responses/${roundId}`);
}
