export interface User {
  id: string;
  email: string;
  displayName: string;
  interests: string[];
  createdAt: string;
}

export interface Article {
  articleId: string;
  articleTitle: string;
  feedCategory: string;
  score: number;
  scoredAt: string;
}

export interface ReadEvent {
  articleId: string;
  articleTitle: string;
  feedCategory: string;
  readAt: string;
}

export interface Note {
  id: string;
  articleId: string;
  content: string;
  sentimentScore: number | null;
  sentimentLabel: string | null;
  usefulnessScore: number | null;
  createdAt: string;
}

export interface Dashboard {
  totalArticlesRead: number;
  totalNotes: number;
  predictedDaysToCert: number | null;
  categoryBreakdown: Record<string, number>;
  averageSentiment: number;
  averageUsefulness: number;
  recentReads: ReadEvent[];
  notes: Note[];
}

export interface Feed {
  id: string;
  name: string;
  url: string;
  category: string;
  lastPolledAt: string | null;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  userId: string;
}
