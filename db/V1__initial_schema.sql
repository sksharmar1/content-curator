CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(36)  PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);
CREATE TABLE IF NOT EXISTS user_interests (
    user_id VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic   VARCHAR(100) NOT NULL,
    weight  INT          NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, topic)
);
CREATE TABLE IF NOT EXISTS feeds (
    id             VARCHAR(36)  PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    url            VARCHAR(500) NOT NULL UNIQUE,
    category       VARCHAR(100) NOT NULL,
    last_polled_at TIMESTAMPTZ,
    active         BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS articles (
    id           VARCHAR(36)   PRIMARY KEY,
    feed_id      VARCHAR(36)   NOT NULL REFERENCES feeds(id),
    title        VARCHAR(500)  NOT NULL,
    summary      TEXT,
    url          VARCHAR(1000) NOT NULL,
    url_hash     VARCHAR(64)   NOT NULL UNIQUE,
    status       VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    published_at TIMESTAMPTZ,
    ingested_at  TIMESTAMPTZ   NOT NULL
);
CREATE TABLE IF NOT EXISTS article_tags (
    article_id VARCHAR(36)  NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag        VARCHAR(100) NOT NULL
);
INSERT INTO feeds (id, name, url, category) VALUES
    ('feed-001', 'Towards Data Science',      'https://towardsdatascience.com/feed',                'AI/ML'),
    ('feed-002', 'DeepLearning.AI The Batch', 'https://www.deeplearning.ai/the-batch/feed/',        'AI/ML'),
    ('feed-003', 'Spring Blog',               'https://spring.io/blog.atom',                        'Java'),
    ('feed-004', 'AWS News',                  'https://aws.amazon.com/blogs/aws/feed/',              'Cloud')
ON CONFLICT DO NOTHING;

-- Recommendation service tables
CREATE TABLE IF NOT EXISTS user_interest_snapshots (
    user_id    VARCHAR(36) PRIMARY KEY,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS snapshot_topics (
    user_id VARCHAR(36)  NOT NULL REFERENCES user_interest_snapshots(user_id) ON DELETE CASCADE,
    topic   VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS recommendation_scores (
    id           VARCHAR(36)    PRIMARY KEY,
    user_id      VARCHAR(36)    NOT NULL,
    article_id   VARCHAR(36)    NOT NULL,
    article_title VARCHAR(500)  NOT NULL,
    feed_category VARCHAR(100)  NOT NULL,
    score        DOUBLE PRECISION NOT NULL,
    dismissed    BOOLEAN        NOT NULL DEFAULT FALSE,
    read         BOOLEAN        NOT NULL DEFAULT FALSE,
    scored_at    TIMESTAMPTZ    NOT NULL,
    UNIQUE (user_id, article_id)
);

CREATE INDEX IF NOT EXISTS idx_rec_scores_user_score
    ON recommendation_scores(user_id, score DESC)
    WHERE dismissed = FALSE AND read = FALSE;
