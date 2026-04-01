import api from './client';

export function getSeasons() {
  return api.get('/admin/seasons');
}

export function getSeason(id) {
  return api.get(`/admin/seasons/${id}`);
}

export function createSeason(data) {
  return api.post('/admin/seasons', data);
}

export function updateSeason(id, data) {
  return api.put(`/admin/seasons/${id}`, data);
}

export function deleteSeason(id) {
  return api.delete(`/admin/seasons/${id}`);
}
