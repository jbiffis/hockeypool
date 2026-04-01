import api from './client';

export function lookupParticipant(email, seasonId) {
  return api.post('/pool/lookup', { email, seasonId });
}

export function registerParticipant(email, name, teamName, seasonId) {
  return api.post('/pool/register', { email, name, teamName, seasonId });
}

export function getPoolForm(participantId, roundId, seasonId) {
  const params = {};
  if (participantId) params.participantId = participantId;
  if (roundId) params.roundId = roundId;
  if (seasonId) params.seasonId = seasonId;
  return api.get('/pool/form', { params });
}

export function submitPicks(participantId, answers) {
  return api.post('/pool/submit', { participantId, answers });
}
