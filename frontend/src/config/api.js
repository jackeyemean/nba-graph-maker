// API Configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  GENERATE_GRAPH: `${API_BASE_URL}/api/graph/generate`,
  GET_PLAYERS: `${API_BASE_URL}/api/graph/players`,
  GET_TEAMS: `${API_BASE_URL}/api/graph/teams`,
};

export default API_BASE_URL;
