def test_fit_requires_articles(client):
    resp = client.post("/recommend/fit", json={})
    assert resp.status_code == 400


def test_fit(client):
    articles = [{"id": "x", "title": "Some article", "summary": "some article text"}]
    resp = client.post("/recommend/fit", json={"articles": articles})
    assert resp.status_code == 200
    assert resp.get_json()["articleCount"] == 1


def test_score_requires_params(fitted_client):
    resp = fitted_client.post("/recommend/score", json={"topics": []})
    assert resp.status_code == 400


def test_score_relevance(fitted_client):
    ml = fitted_client.post(
        "/recommend/score", json={"topics": ["python", "machine learning"], "articleId": "a1"}
    ).get_json()["score"]
    cooking = fitted_client.post(
        "/recommend/score", json={"topics": ["python", "machine learning"], "articleId": "a2"}
    ).get_json()["score"]
    assert ml > cooking


def test_top_requires_topics(fitted_client):
    resp = fitted_client.post("/recommend/top", json={})
    assert resp.status_code == 400


def test_top_ranks_relevant_first(fitted_client):
    resp = fitted_client.post(
        "/recommend/top", json={"topics": ["neural networks", "deep learning"], "n": 3}
    )
    assert resp.status_code == 200
    body = resp.get_json()
    assert body["count"] >= 1
    assert body["recommendations"][0]["articleId"] == "a3"
