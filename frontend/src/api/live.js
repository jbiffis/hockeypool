import api from './client';

export function getLiveData(seasonId = 2) {
  return api.get('/live', { params: { seasonId } });
}
