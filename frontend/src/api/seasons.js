import api from './client';

export function getSeasons() {
  return api.get('/admin/seasons');
}

export function getSeasonSignup(seasonId) {
  return api.get(`/seasons/${seasonId}/signup`);
}

export function submitSignup(seasonId, email) {
  return api.post(`/seasons/${seasonId}/signup`, { email });
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
