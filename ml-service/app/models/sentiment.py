from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import re

analyzer = SentimentIntensityAnalyzer()

def analyze(text: str) -> dict:
    scores = analyzer.polarity_scores(text)
    compound = scores["compound"]

    if compound >= 0.05:
        label = "POSITIVE"
    elif compound <= -0.05:
        label = "NEGATIVE"
    else:
        label = "NEUTRAL"

    usefulness = _extract_usefulness(text)

    return {
        "score": round(compound, 4),
        "label": label,
        "positive": round(scores["pos"], 4),
        "negative": round(scores["neg"], 4),
        "neutral":  round(scores["neu"], 4),
        "usefulnessScore": usefulness
    }

def _extract_usefulness(text: str):
    # Match "90% useful", "8/10", "7 out of 10"
    text_lower = text.lower()

    m = re.search(r"(\d+)%\s*useful", text_lower)
    if m:
        return round(int(m.group(1)) / 100, 2)

    m = re.search(r"(\d+)\s*(?:out of|/)\s*10", text_lower)
    if m:
        return round(int(m.group(1)) / 10, 2)

    return None
