import json
import logging
import os
from botocore.vendored import requests

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def call_releasy_validate(name, event):
    logger.info("calling releasy")

    url = "http://" + os.environ['RELEASY_URL'] + "/release/validate/" + event['text']
    request = {
        "releaseManager": [
            name
        ]
    }
    headers = {
        'Content-Type': 'application/json; charset=utf8'
    }
    response = requests.post(url, json.dumps(request), headers=headers)
    return json.loads(response.text)


def call_releasy_create(template):
    logger.info("calling Releasy to start release")

    url = "http://" + os.environ['RELEASY_URL'] + "/release/"
    headers = {
        'Content-Type': 'application/json; charset=utf8'
    }
    response = requests.post(url, json.dumps(template), headers=headers)
    return json.loads(response.text)


def call_releasy_deploy(release):
    logger.info("calling Releasy to deploy a release")

    url = "http://" + os.environ['RELEASY_URL'] + "/release/deploy"
    headers = {
        'Content-Type': 'application/json; charset=utf8'
    }
    requests.post(url, json.dumps(release), headers=headers)
