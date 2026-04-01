import api from './client';

export function getOptions(questionId) {
  return api.get(`/admin/questions/${questionId}/options`);
}

export function createOption(questionId, data) {
  return api.post(`/admin/questions/${questionId}/options`, data);
}

export function updateOption(questionId, optionId, data) {
  return api.put(`/admin/questions/${questionId}/options/${optionId}`, data);
}

export function deleteOption(questionId, optionId) {
  return api.delete(`/admin/questions/${questionId}/options/${optionId}`);
}

export function updateOptionPoints(questionId, optionId, points) {
  return api.patch(`/admin/questions/${questionId}/options/${optionId}/points`, { points });
}
