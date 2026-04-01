import api from './client';

export function getQuestions(roundId) {
  return api.get(`/admin/rounds/${roundId}/questions`);
}

export function getQuestion(roundId, questionId) {
  return api.get(`/admin/rounds/${roundId}/questions/${questionId}`);
}

export function createQuestion(roundId, data) {
  return api.post(`/admin/rounds/${roundId}/questions`, data);
}

export function updateQuestion(roundId, questionId, data) {
  return api.put(`/admin/rounds/${roundId}/questions/${questionId}`, data);
}

export function deleteQuestion(roundId, questionId) {
  return api.delete(`/admin/rounds/${roundId}/questions/${questionId}`);
}
