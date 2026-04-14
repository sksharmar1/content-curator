from flask import Blueprint, request, jsonify
from app.models.sentiment import analyze

sentiment_bp = Blueprint("sentiment", __name__)

@sentiment_bp.route("/analyze/sentiment", methods=["POST"])
def analyze_sentiment():
    data = request.get_json()
    if not data or "text" not in data:
        return jsonify({"error": "text field required"}), 400

    text = data["text"]
    if not text or not text.strip():
        return jsonify({"error": "text cannot be empty"}), 400

    result = analyze(text)
    return jsonify(result)


@sentiment_bp.route("/analyze/batch", methods=["POST"])
def analyze_batch():
    data = request.get_json()
    if not data or "texts" not in data:
        return jsonify({"error": "texts array required"}), 400

    results = [
        {"text": t[:100] + "..." if len(t) > 100 else t, **analyze(t)}
        for t in data["texts"]
    ]
    return jsonify({"results": results})
