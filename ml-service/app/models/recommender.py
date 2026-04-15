from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from app.models.model_store import save_model, load_model
import numpy as np
import logging

logger = logging.getLogger(__name__)

class ContentRecommender:
    def __init__(self):
        self.vectorizer    = TfidfVectorizer(
            max_features=1000, stop_words="english", ngram_range=(1, 2))
        self.article_vectors = None
        self.article_ids     = []

    def fit(self, articles: list[dict]):
        if not articles:
            return
        corpus = [
            f"{a.get('category','')} {a.get('title','')} {a.get('summary','')}"
            for a in articles
        ]
        self.article_ids     = [a["id"] for a in articles]
        self.article_vectors = self.vectorizer.fit_transform(corpus)

        # Persist model to S3
        save_model(self, "tfidf-recommender")
        logger.info(f"Fitted and saved recommender with {len(articles)} articles")

    def score(self, user_topics: list[str], article_id: str) -> float:
        if self.article_vectors is None or article_id not in self.article_ids:
            return 0.0
        query     = " ".join(user_topics)
        query_vec = self.vectorizer.transform([query])
        idx       = self.article_ids.index(article_id)
        return round(float(cosine_similarity(query_vec, self.article_vectors[idx])[0][0]), 4)

    def top_articles(self, user_topics: list[str], n: int = 20) -> list[dict]:
        if self.article_vectors is None:
            return []
        query        = " ".join(user_topics)
        query_vec    = self.vectorizer.transform([query])
        similarities = cosine_similarity(query_vec, self.article_vectors)[0]
        top_indices  = np.argsort(similarities)[::-1][:n]
        return [
            {"articleId": self.article_ids[i], "score": round(float(similarities[i]), 4)}
            for i in top_indices if similarities[i] > 0.0
        ]

def _load_or_create() -> ContentRecommender:
    """Try loading from S3, fall back to empty instance."""
    existing = load_model("tfidf-recommender")
    if existing and isinstance(existing, ContentRecommender):
        logger.info("Loaded recommender from S3")
        return existing
    logger.info("Starting with fresh recommender")
    return ContentRecommender()

recommender = _load_or_create()
