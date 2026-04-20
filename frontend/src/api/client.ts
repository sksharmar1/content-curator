import axios from 'axios';

const IS_PROD = window.location.hostname !== 'localhost';

const BASE = 'https://{svc}-route-sksharmar1-dev.apps.rm2.thpm.p1.openshiftapps.com';

const USER_PROFILE_URL   = IS_PROD
  ? 'https://user-profile-route-sksharmar1-dev.apps.rm2.thpm.p1.openshiftapps.com'
  : 'http://localhost:8081';
const CONTENT_URL        = IS_PROD
  ? 'https://content-ingestion-route-sksharmar1-dev.apps.rm2.thpm.p1.openshiftapps.com'
  : 'http://localhost:8082';
const RECOMMENDATION_URL = IS_PROD
  ? 'https://recommendation-route-sksharmar1-dev.apps.rm2.thpm.p1.openshiftapps.com'
  : 'http://localhost:8083';
const ANALYTICS_URL      = IS_PROD
  ? 'https://analytics-route-sksharmar1-dev.apps.rm2.thpm.p1.openshiftapps.com'
  : 'http://localhost:8084';

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
