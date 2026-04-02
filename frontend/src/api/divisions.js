import api from './client';

export function getDivisions(seasonId) {
  return api.get('/divisions', { params: { seasonId } });
}

export function getAdminDivisions(seasonId) {
  return api.get('/admin/divisions', { params: { seasonId } });
}

export function createDivision(seasonId, name) {
  return api.post('/admin/divisions', { seasonId, name });
}

export function addParticipantToDivision(divisionId, participantId) {
  return api.post(`/admin/divisions/${divisionId}/participants/${participantId}`);
}

export function removeParticipantFromDivision(divisionId, participantId) {
  return api.delete(`/admin/divisions/${divisionId}/participants/${participantId}`);
}
