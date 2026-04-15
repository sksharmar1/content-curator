import boto3
import pickle
import os
import logging

logger = logging.getLogger(__name__)

S3_ENDPOINT  = os.getenv("AWS_ENDPOINT_URL", "http://localhost:4566")
AWS_REGION   = os.getenv("AWS_REGION", "us-east-1")
MODELS_BUCKET = os.getenv("S3_MODELS_BUCKET", "content-curator-models")

def get_s3():
    return boto3.client(
        "s3",
        endpoint_url=S3_ENDPOINT,
        region_name=AWS_REGION,
        aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID", "test"),
        aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY", "test"),
    )

def save_model(model, model_name: str):
    """Serialize and save a scikit-learn model to S3."""
    try:
        data = pickle.dumps(model)
        s3   = get_s3()
        key  = f"models/{model_name}/latest.pkl"
        s3.put_object(Bucket=MODELS_BUCKET, Key=key, Body=data)
        logger.info(f"Saved model {model_name} to s3://{MODELS_BUCKET}/{key}")
        return True
    except Exception as e:
        logger.warning(f"Failed to save model to S3: {e}")
        return False

def load_model(model_name: str):
    """Load a scikit-learn model from S3."""
    try:
        s3  = get_s3()
        key = f"models/{model_name}/latest.pkl"
        obj = s3.get_object(Bucket=MODELS_BUCKET, Key=key)
        return pickle.loads(obj["Body"].read())
    except Exception as e:
        logger.warning(f"Failed to load model from S3: {e}")
        return None
