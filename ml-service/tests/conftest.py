import pytest

from main import app


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as c:
        yield c


@pytest.fixture
def fitted_client(client):
    """Client with the recommender fitted on a small corpus."""
    articles = [
        {"id": "a1", "category": "tech", "title": "Machine learning with Python",
         "summary": "machine learning and python for data science"},
        {"id": "a2", "category": "food", "title": "Italian cooking",
         "summary": "italian cooking recipes with fresh pasta"},
        {"id": "a3", "category": "tech", "title": "Deep learning guide",
         "summary": "deep learning neural networks with pytorch"},
    ]
    resp = client.post("/recommend/fit", json={"articles": articles})
    assert resp.status_code == 200
    return client
