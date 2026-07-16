def test_positive_sentiment(client):
    resp = client.post("/analyze/sentiment", json={"text": "I love this fantastic article"})
    assert resp.status_code == 200
    body = resp.get_json()
    assert body["label"] == "POSITIVE"
    assert body["positive"] > body["negative"]


def test_negative_sentiment(client):
    resp = client.post("/analyze/sentiment", json={"text": "This is terrible, awful garbage"})
    assert resp.status_code == 200
    assert resp.get_json()["label"] == "NEGATIVE"


def test_missing_text_field(client):
    resp = client.post("/analyze/sentiment", json={})
    assert resp.status_code == 400


def test_empty_text(client):
    resp = client.post("/analyze/sentiment", json={"text": "   "})
    assert resp.status_code == 400


def test_batch(client):
    resp = client.post("/analyze/batch", json={"texts": ["great stuff", "horrible mess"]})
    assert resp.status_code == 200
    results = resp.get_json()["results"]
    assert len(results) == 2
    assert results[0]["label"] == "POSITIVE"
    assert results[1]["label"] == "NEGATIVE"


def test_batch_missing_texts(client):
    resp = client.post("/analyze/batch", json={})
    assert resp.status_code == 400
