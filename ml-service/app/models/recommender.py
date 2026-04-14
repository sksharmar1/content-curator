from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

class ContentRecommender:
    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            max_features=1000,
            stop_words="english",
            ngram_range=(1, 2)
        )
        self.article_vectors = None
        self.article_ids = []

    def fit(self, articles: list[dict]):
        """
        articles: list of {id, title, summary, category}
        """
        if not articles:
            return

        corpus = [
            f"{a.get('category','')} {a.get('title','')} {a.get('summary','')}"
            for a in articles
        ]
        self.article_ids = [a["id"] for a in articles]
        self.article_vectors = self.vectorizer.fit_transform(corpus)

    def score(self, user_topics: list[str], article_id: str) -> float:
        """
        Score a single article against user topics.
        Returns 0.0 to 1.0.
        """
        if self.article_vectors is None or article_id not in self.article_ids:
            return 0.0

        query = " ".join(user_topics)
        query_vec = self.vectorizer.transform([query])

        idx = self.article_ids.index(article_id)
        article_vec = self.article_vectors[idx]

        similarity = cosine_similarity(query_vec, article_vec)[0][0]
        return round(float(similarity), 4)

    def top_articles(self, user_topics: list[str], n: int = 20) -> list[dict]:
        """
        Return top-n article IDs with scores for given user topics.
        """
        if self.article_vectors is None:
            return []

        query = " ".join(user_topics)
        query_vec = self.vectorizer.transform([query])

        similarities = cosine_similarity(query_vec, self.article_vectors)[0]
        top_indices = np.argsort(similarities)[::-1][:n]

        return [
            {"articleId": self.article_ids[i], "score": round(float(similarities[i]), 4)}
            for i in top_indices
            if similarities[i] > 0.0
        ]

# Singleton instance
recommender = ContentRecommender()
