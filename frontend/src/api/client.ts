import axios from 'axios';

// Single entry point: the API gateway routes /api/** to the right service.
// Set REACT_APP_API_URL at build time (e.g. https://content-curator-gateway.onrender.com).
// Falls back to direct local service ports for local dev without the gateway.
const GATEWAY = process.env.REACT_APP_API_URL;

const USER_PROFILE_URL   = GATEWAY || 'http://localhost:8081';
const CONTENT_URL        = GATEWAY || 'http://localhost:8082';
const RECOMMENDATION_URL = GATEWAY || 'http://localhost:8083';
const ANALYTICS_URL      = GATEWAY || 'http://localhost:8084';

let authToken: string | null = null;

export const setToken = (token: string) => { authToken = token; };
export const clearToken = () => { authToken = null; };
export const getToken = () => authToken;

const withAuth = () => ({
  headers: { Authorization: `Bearer ${authToken}` }
});

export const api = {
  auth: {
    login: (email: string, password: string) =>
      axios.post(`${USER_PROFILE_URL}/api/auth/login`, { email, password }),
    register: (email: string, password: string, displayName: string) =>
      axios.post(`${USER_PROFILE_URL}/api/auth/register`, { email, password, displayName }),
  },
  user: {
    getMe: () =>
      axios.get(`${USER_PROFILE_URL}/api/users/me`, withAuth()),
    addInterest: (topic: string) =>
      axios.post(`${USER_PROFILE_URL}/api/users/me/interests`, { topic }, withAuth()),
  },
  recommendations: {
    getTop: (limit = 20) =>
      axios.get(`${RECOMMENDATION_URL}/api/recommendations?limit=${limit}`, withAuth()),
    markRead: (articleId: string) =>
      axios.post(`${RECOMMENDATION_URL}/api/recommendations/${articleId}/read`, {}, withAuth()),
  },
  analytics: {
    recordRead: (articleId: string, articleTitle: string, feedCategory: string) =>
      axios.post(`${ANALYTICS_URL}/api/analytics/read`,
        { articleId, articleTitle, feedCategory }, withAuth()),
    saveNote: (articleId: string, content: string) =>
      axios.post(`${ANALYTICS_URL}/api/analytics/notes`,
        { articleId, content }, withAuth()),
    getDashboard: () =>
      axios.get(`${ANALYTICS_URL}/api/analytics/dashboard`, withAuth()),
  },
  feeds: {
    register: (name: string, url: string, category: string) =>
      axios.post(`${CONTENT_URL}/api/feeds`, { name, url, category }, withAuth()),
    poll: () =>
      axios.post(`${CONTENT_URL}/api/feeds/poll`, {}, withAuth()),
  },
};
