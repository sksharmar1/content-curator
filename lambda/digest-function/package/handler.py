import json
import os
import boto3
import logging
from datetime import datetime, timezone

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# AWS clients
sqs = boto3.client("sqs",    region_name=os.getenv("AWS_REGION", "us-east-1"))
ses = boto3.client("ses",    region_name=os.getenv("AWS_REGION", "us-east-1"))

SENDER_EMAIL      = os.getenv("SENDER_EMAIL", "noreply@contentcurator.ai")
RECOMMENDATION_URL = os.getenv("RECOMMENDATION_SERVICE_URL", "http://localhost:8083")

def lambda_handler(event, context):
    """
    Triggered by SQS digest-scheduled queue.
    Fetches top recommendations for each user and sends a weekly email digest.
    """
    logger.info(f"Processing {len(event.get('Records', []))} digest events")

    for record in event.get("Records", []):
        try:
            body    = json.loads(record["body"])
            user_id = body.get("userId")
            email   = body.get("email")
            name    = body.get("displayName", "Learner")

            if not user_id or not email:
                logger.warning("Missing userId or email in event")
                continue

            # Fetch top recommendations via internal service call
            recommendations = fetch_recommendations(user_id)

            if not recommendations:
                logger.info(f"No recommendations for user {user_id}, skipping digest")
                continue

            # Send email digest
            send_digest_email(email, name, recommendations)
            logger.info(f"Digest sent to {email}")

        except Exception as e:
            logger.error(f"Failed to process digest for record: {e}")

    return {"statusCode": 200, "body": "Digest processing complete"}


def fetch_recommendations(user_id: str) -> list:
    """Fetch top 10 recommendations from the recommendation service."""
    import urllib.request
    try:
        url = f"{RECOMMENDATION_URL}/api/recommendations?limit=10"
        req = urllib.request.Request(url, headers={
            "X-Internal-User-Id": user_id  # internal service header
        })
        with urllib.request.urlopen(req, timeout=10) as resp:
            return json.loads(resp.read())
    except Exception as e:
        logger.warning(f"Failed to fetch recommendations: {e}")
        return []


def send_digest_email(to_email: str, name: str, recommendations: list):
    """Send HTML email digest via SES."""
    now       = datetime.now(timezone.utc)
    week_str  = now.strftime("%B %d, %Y")
    subject   = f"Your weekly learning digest — {week_str}"

    # Build article list HTML
    articles_html = ""
    for i, rec in enumerate(recommendations[:10], 1):
        score_pct = round(rec.get("score", 0) * 100)
        category  = rec.get("feedCategory", "General")
        title     = rec.get("articleTitle", "Untitled")
        cat_color = {
            "AI/ML":  "#818cf8",
            "Cloud":  "#38bdf8",
            "Java":   "#4ade80",
        }.get(category, "#9ca3af")

        articles_html += f"""
        <tr>
          <td style="padding: 12px 0; border-bottom: 1px solid #2a2a4a;">
            <span style="background: {cat_color}22; color: {cat_color};
                         padding: 2px 8px; border-radius: 10px;
                         font-size: 11px; font-weight: 600;">
              {category}
            </span>
            <br/>
            <span style="color: #e2e8f0; font-size: 15px;
                         font-weight: 500; line-height: 1.5;">
              {title}
            </span>
            <br/>
            <span style="color: #6b7280; font-size: 12px;">
              {score_pct}% match to your interests
            </span>
          </td>
        </tr>"""

    html_body = f"""
    <!DOCTYPE html>
    <html>
    <body style="background: #0f0f1a; font-family: Arial, sans-serif;
                 margin: 0; padding: 20px;">
      <div style="max-width: 600px; margin: 0 auto;">
        <div style="background: #1a1a2e; border-radius: 12px;
                    padding: 32px; border: 1px solid #2a2a4a;">

          <h1 style="color: #7c83fd; margin: 0 0 8px;">ContentCurator</h1>
          <p style="color: #888; margin: 0 0 24px;">
            Weekly digest for {name} — {week_str}
          </p>

          <h2 style="color: #e2e8f0; font-size: 18px; margin: 0 0 16px;">
            Your top picks this week
          </h2>

          <table style="width: 100%; border-collapse: collapse;">
            {articles_html}
          </table>

          <div style="margin-top: 24px; padding-top: 24px;
                      border-top: 1px solid #2a2a4a; text-align: center;">
            <a href="http://localhost:3000/feed"
               style="background: #7c83fd; color: #fff; padding: 12px 24px;
                      border-radius: 8px; text-decoration: none;
                      font-weight: 600; font-size: 14px;">
              View your full feed
            </a>
          </div>

          <p style="color: #555; font-size: 11px;
                    text-align: center; margin-top: 24px;">
            AI Content Curator &middot; Personalised learning, every week
          </p>
        </div>
      </div>
    </body>
    </html>"""

    try:
        ses.send_email(
            Source=SENDER_EMAIL,
            Destination={"ToAddresses": [to_email]},
            Message={
                "Subject": {"Data": subject, "Charset": "UTF-8"},
                "Body":    {"Html": {"Data": html_body, "Charset": "UTF-8"}}
            }
        )
    except ses.exceptions.MessageRejected as e:
        logger.warning(f"SES send failed (likely sandbox mode): {e}")
    except Exception as e:
        logger.error(f"Failed to send email: {e}")
