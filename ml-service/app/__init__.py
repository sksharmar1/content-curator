from flask import Flask
from flask_cors import CORS

def create_app():
    app = Flask(__name__)
    CORS(app)

    from app.routes.health_routes import health_bp
    from app.routes.sentiment_routes import sentiment_bp
    from app.routes.recommend_routes import recommend_bp

    app.register_blueprint(health_bp)
    app.register_blueprint(sentiment_bp)
    app.register_blueprint(recommend_bp)

    return app
