import api from './client';

export function getRounds(seasonId) {
  const params = {};
  if (seasonId) params.seasonId = seasonId;
  return api.get('/admin/rounds', { params });
}

export function getRound(id) {
  return api.get(`/admin/rounds/${id}`);
}

export function createRound(data) {
  return api.post('/admin/rounds', data);
}

export function updateRound(id, data) {
  return api.put(`/admin/rounds/${id}`, data);
}

export function deleteRound(id) {
  return api.delete(`/admin/rounds/${id}`);
}

export function updateRoundStatus(id, status) {
  return api.patch(`/admin/rounds/${id}/status`, { status });
}

export function getResponsesByRound(roundId) {
  return api.get(`/admin/rounds/${roundId}/responses`);
}
