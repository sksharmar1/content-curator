from flask import Blueprint, request, jsonify
from app.models.recommender import recommender

recommend_bp = Blueprint("recommend", __name__)

@recommend_bp.route("/recommend/fit", methods=["POST"])
def fit():
    data = request.get_json()
    articles = data.get("articles", [])
    if not articles:
        return jsonify({"error": "articles array required"}), 400

    recommender.fit(articles)
    return jsonify({"status": "fitted", "articleCount": len(articles)})


@recommend_bp.route("/recommend/score", methods=["POST"])
def score():
    data = request.get_json()
    topics     = data.get("topics", [])
    article_id = data.get("articleId", "")

    if not topics or not article_id:
        return jsonify({"error": "topics and articleId required"}), 400

    score = recommender.score(topics, article_id)
    return jsonify({"articleId": article_id, "score": score})


@recommend_bp.route("/recommend/top", methods=["POST"])
def top():
    data   = request.get_json()
    topics = data.get("topics", [])
    n      = data.get("n", 20)

    if not topics:
        return jsonify({"error": "topics required"}), 400

    results = recommender.top_articles(topics, n)
    return jsonify({"recommendations": results, "count": len(results)})
