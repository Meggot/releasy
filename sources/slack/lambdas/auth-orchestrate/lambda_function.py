import json
import boto3
import logging
import hashlib
import hmac
import os
from urllib.parse import parse_qsl

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def lambda_handler(event, context):
    blocks = []
    logger.info(event)
    ''' Capture the necessary auth data.'''

    if event_has_slack_headers(event):
        slack_signature = event['headers']['X-Slack-Signature']
        slack_request_timestamp = event['headers']['X-Slack-Request-Timestamp']
        if not verify_slack_request(slack_signature, slack_request_timestamp, event['body']):
            blocks.append(create_section("Oi don't call this from anywhere but /release"))
            return create_response({"blocks": blocks})
        else:
            body = dict(parse_qsl(event['body']))
            call_integration_lambda(body)
            return create_empty_response()
    else:
        response = {"message": "unauthorized, naughty naughty"}
        return create_response(response, 403)


def event_has_slack_headers(event):
    headers = event.get('headers')
    return headers is not None and 'X-Slack-Signature' in headers and 'X-Slack-Request-Timestamp' in headers


def create_empty_response():
    return {
        "isBase64Encoded": False,
        "statusCode": 200,
        "headers": {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    }


def create_section(text):
    return {
        "type": "section",
        "text": {
            "type": "mrkdwn",
            "text": text
        }
    }


def create_response(body, status_code=200):
    return {
            "isBase64Encoded": False,
            "statusCode": status_code,
            "headers": {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            "body": json.dumps(body)
        }


def call_integration_lambda(payload):
    lambda_client = boto3.client('lambda')
    logger.info("Calling releasy main lambda")
    lambda_client.invoke(
        FunctionName="releasy_main",
        InvocationType="Event",
        Payload=bytes(json.dumps(payload), encoding='utf8')
    )


def verify_slack_request(slack_signature=None, slack_request_timestamp=None, request_body=None):
    """Verify the POST request."""
    slack_signing_secret = os.environ['SLACK_SIGNING_SECRET']

    ''' Form the basestring as stated in the Slack API docs. We need to make a bytestring. '''
    basestring = f"v0:{slack_request_timestamp}:{request_body}".encode('utf-8')

    ''' Make the Signing Secret a bytestring too. '''
    slack_signing_secret_bytes = bytes(slack_signing_secret, 'utf-8')

    ''' Create a new HMAC "signature", and return the string presentation. '''
    my_signature = 'v0=' + hmac.new(slack_signing_secret_bytes, basestring, hashlib.sha256).hexdigest()

    ''' Compare the the Slack provided signature to ours.
    If they are equal, the request should be verified successfully.
    Log the unsuccessful requests for further analysis
    (along with another relevant info about the request). '''

    if hmac.compare_digest(my_signature, slack_signature):
        logger.info(f"Verification successful: computed: {my_signature} incoming: {slack_signature}")
        return True
    else:
        logger.warning(f"Verification failed. my_signature: {my_signature} incoming: {slack_signature}")
        return False
