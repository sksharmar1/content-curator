import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { Article } from '../types';
import CategoryBadge from '../components/common/CategoryBadge';
import Spinner from '../components/common/Spinner';

const FeedPage: React.FC = () => {
  const [articles, setArticles]       = useState<Article[]>([]);
  const [loading, setLoading]         = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [refreshing, setRefreshing]   = useState(false);
  const [polling, setPolling]         = useState(false);
  const [limit, setLimit]             = useState(20);
  const [lastRefresh, setLastRefresh] = useState(new Date());
  const navigate = useNavigate();

  const loadFeed = useCallback(async (lim: number, append = false) => {
    try {
      const res = await api.recommendations.getTop(lim);
      setArticles(append
        ? prev => [...prev, ...res.data.slice(prev.length)]
        : res.data);
      setLastRefresh(new Date());
    } catch (e) {
      console.error('Failed to load feed', e);
    } finally {
      setLoading(false);
      setLoadingMore(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { loadFeed(20); }, [loadFeed]);

  const refresh = async () => {
    setRefreshing(true);
    await loadFeed(limit);
  };

  const pollAndRefresh = async () => {
    setPolling(true);
    try {
      await api.feeds.poll();
      // Wait for ingestion + SQS consumer to process
      await new Promise(r => setTimeout(r, 8000));
      await loadFeed(limit);
    } finally {
      setPolling(false);
    }
  };

  const loadMore = () => {
    setLoadingMore(true);
    const newLimit = limit + 20;
    setLimit(newLimit);
    loadFeed(newLimit, true);
  };

  const handleArticleClick = async (article: Article) => {
    await api.recommendations.markRead(article.articleId);
    await api.analytics.recordRead(
      article.articleId, article.articleTitle, article.feedCategory);
    navigate(`/article/${article.articleId}`, { state: { article } });
  };

  if (loading) return (
    <div style={center}>
      <Spinner size={48} />
      <p style={{ color: '#888', marginTop: '1rem' }}>
        Loading your personalised feed...
      </p>
    </div>
  );

  return (
    <div style={pageStyle}>
      <div style={header}>
        <div>
          <h2 style={{ color: '#fff', margin: '0 0 4px' }}>Your Feed</h2>
          <span style={{ color: '#555', fontSize: '0.75rem' }}>
            Updated {lastRefresh.toLocaleTimeString()}
          </span>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <span style={{ color: '#888', fontSize: '0.85rem' }}>
            {articles.length} articles
          </span>
          <button style={ghostBtn} onClick={refresh} disabled={refreshing}>
            {refreshing ? 'Refreshing...' : 'Refresh'}
          </button>
          <button style={primaryBtn} onClick={pollAndRefresh} disabled={polling}>
            {polling ? 'Polling feeds...' : 'Fetch new articles'}
          </button>
        </div>
      </div>

      {polling && (
        <div style={pollingBanner}>
          <Spinner size={16} />
          <span>Fetching new articles from RSS feeds — this takes ~8 seconds...</span>
        </div>
      )}

      {articles.length === 0 ? (
        <div style={emptyState}>
          <p style={{ color: '#888' }}>No recommendations yet.</p>
          <p style={{ color: '#666', fontSize: '0.85rem' }}>
            Add interests in your profile, then click "Fetch new articles".
          </p>
        </div>
      ) : (
        <>
          <div style={grid}>
            {articles.map((article, i) => (
              <div key={article.articleId} style={card}
                onClick={() => handleArticleClick(article)}>
                <div style={cardHeader}>
                  <CategoryBadge category={article.feedCategory} />
                  <span style={{ color: '#555', fontSize: '0.75rem' }}>
                    #{i + 1}
                  </span>
                </div>
                <h3 style={titleStyle}>{article.articleTitle}</h3>
                <div style={cardFooter}>
                  <div style={scoreBar}>
                    <div style={{
                      ...scoreFill,
                      width: `${Math.round(article.score * 100)}%`,
                      background: article.score > 0.2 ? '#7c83fd' : '#444'
                    }} />
                  </div>
                  <span style={{ color: '#666', fontSize: '0.75rem' }}>
                    {Math.round(article.score * 100)}% match
                  </span>
                </div>
              </div>
            ))}
          </div>

          <div style={center}>
            {loadingMore
              ? <Spinner />
              : (
                <button style={ghostBtn} onClick={loadMore}>
                  Load more articles
                </button>
              )
            }
          </div>
        </>
      )}
    </div>
  );
};

const pageStyle: React.CSSProperties = {
  maxWidth: '900px', margin: '0 auto', padding: '2rem 1rem'
};
const header: React.CSSProperties = {
  display: 'flex', justifyContent: 'space-between',
  alignItems: 'flex-start', marginBottom: '1.5rem'
};
const grid: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
  gap: '1rem', marginBottom: '1.5rem'
};
const card: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '10px',
  padding: '1.25rem', cursor: 'pointer',
  border: '1px solid #2a2a4a',
  transition: 'border-color 0.2s, transform 0.1s',
};
const cardHeader: React.CSSProperties = {
  display: 'flex', justifyContent: 'space-between',
  alignItems: 'center', marginBottom: '0.75rem'
};
const titleStyle: React.CSSProperties = {
  color: '#e2e8f0', fontSize: '0.95rem',
  fontWeight: 500, lineHeight: 1.5, margin: '0 0 1rem'
};
const cardFooter: React.CSSProperties = {
  display: 'flex', alignItems: 'center', gap: '0.5rem'
};
const scoreBar: React.CSSProperties = {
  flex: 1, height: '4px',
  background: '#2a2a4a', borderRadius: '2px', overflow: 'hidden'
};
const scoreFill: React.CSSProperties = {
  height: '100%', borderRadius: '2px', transition: 'width 0.3s'
};
const center: React.CSSProperties = {
  display: 'flex', flexDirection: 'column',
  alignItems: 'center', padding: '2rem'
};
const emptyState: React.CSSProperties = {
  textAlign: 'center', padding: '4rem 2rem'
};
const primaryBtn: React.CSSProperties = {
  background: '#7c83fd', color: '#fff', border: 'none',
  borderRadius: '8px', padding: '8px 1.25rem',
  cursor: 'pointer', fontWeight: 600, fontSize: '0.85rem'
};
const ghostBtn: React.CSSProperties = {
  background: 'transparent', color: '#7c83fd',
  border: '1px solid #7c83fd', borderRadius: '8px',
  padding: '8px 1.25rem', cursor: 'pointer', fontSize: '0.85rem'
};
const pollingBanner: React.CSSProperties = {
  display: 'flex', alignItems: 'center', gap: '0.75rem',
  background: '#1a1a3a', border: '1px solid #7c83fd40',
  borderRadius: '8px', padding: '0.75rem 1rem',
  marginBottom: '1.5rem', color: '#888', fontSize: '0.85rem'
};

export default FeedPage;
