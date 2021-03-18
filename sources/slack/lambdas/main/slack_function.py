import json
import logging
import os
from botocore.vendored import requests

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def post_ephemeral_message(channel_id, user_id, blocks):
    response_url = 'https://slack.com/api/chat.postEphemeral'

    request = {
        'channel': channel_id,
        'user': user_id,
        'attachments': [],
        'blocks': blocks
    }

    res = requests.post(response_url, json.dumps(request), headers=generate_headers())
    logger.info('slash command response')
    logger.info(res.text)

def post_update(payload):
    url = "https://slack.com/api/chat.update"
    body = {
        "channel": payload['channel']['id'],
        "ts": payload['container']['message_ts'],
        "text": "update that b",
        "blocks": []
    }
    headers = generate_headers()
    response = requests.post(url, json.dumps(body), headers=headers)
    logger.info("update response")
    logger.info(response.text)

def generate_headers():
    return {
        'Content-Type': 'application/json; charset=utf8',
        'Authorization': 'Bearer {}'.format(os.environ['SLACK_TOKEN'])
    }


def create_section(text):
    return {
        "type": "section",
        "text": {
            "type": "mrkdwn",
            "text": text
        }
    }


def create_error():
    return {
        "isBase64Encoded": False,
        "statusCode": 200,
        "headers": {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        "body": {
            "response_type": "ephemeral",
            "replace_original": False,
            "text": "Sorry, that didn't work. Please try again."
        }
    }


def create_start_buttons():
    return {
        "type": "actions",
        "block_id": "actionblock789",
        "elements": [
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Yes"
                },
                "style": "primary",
                "value": "click_me_456"
            },
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Not right now"
                }
            }
        ]
    }


def create_selection():
    return {
        "type": "actions",
        "elements": [
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Farmhouse",
                    "emoji": True
                },
                "value": "click_me_1"
            },
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Kin Khao",
                    "emoji": True
                },
                "value": "click_me_2"
            },
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Ler Ros",
                    "emoji": True
                },
                "value": "click_me_3"
            }
        ]
    }

def create_slack_block_of_release(release):
    blocks = []
    blocks.append({
        "type": "section",
        "text": {
            "type": "mrkdwn",
            "text": "*These are the following jobs to be run for the release*\n\n\n\n\n\n\n"
                    "Please press the button to start the jobs"
        },
        "accessory": {
            "type": "image",
            "image_url": "https://media1.tenor.com/images/d78d8c537f1aabe217e6302becf66483/tenor.gif?itemid=8682388",
            "alt_text": "happy_bat"
        }
    })
    for job in release['jenkinsJobStatuses']:
        jobDetails = job['job']
        fieldsText = ">" + jobDetails['jobName']
        if 'environment' in jobDetails:
            fieldsText += " " + jobDetails['environment']
        if 'service' in jobDetails:
            fieldsText += " " + jobDetails['service']
        if job['status'] == "PENDING":
            initialOption = {
                "text": {
                    "type": "plain_text",
                    "text": ":white_check_mark: Run",
                    "emoji": True
                },
                "value": "Run_" + jobDetails['id']
            }
        else:
            initialOption = {
                "text": {
                    "type": "plain_text",
                    "text": ":warning: Skip",
                    "emoji": True
                },
                "value": "Skip_" + jobDetails['id']
            }
        blocks.append({
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": fieldsText
            },
            "accessory": {
                "type": "static_select",
                "placeholder": {
                    "type": "plain_text",
                    "text": "Job Action",
                    "emoji": True
                },
                "action_id": "job_update_" + release['releaseTemplate']['releaseTitle'],
                "initial_option": initialOption,
                "options": [
                    {
                        "text": {
                            "type": "plain_text",
                            "text": ":warning: Skip",
                            "emoji": True
                        },
                        "value": "Skip_" + jobDetails['id']
                    },
                    {
                        "text": {
                            "type": "plain_text",
                            "text": ":white_check_mark: Run",
                            "emoji": True
                        },
                        "value": "Run_" + jobDetails['id']
                    }
                ]
            }
        })
    blocks.append({
        "type": "actions",
        "block_id": "deploy_release",
        "elements": [
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Deploy Release"
                },
                "style": "primary",
                "value": "deploy_release_" + release['releaseTemplate']['releaseTitle']
            },
            {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Cancel"
                }
            }
        ]
    })
    return blocks

def validate_response_to_slack_blocks(response):
    blocks = []

    release_title = response.get('releaseTitle')

    if release_title is None:
        errorMessage = response.get('message')
        blocks.append({
            "type": "section",
            "text": {
				"type": "mrkdwn",
				"text": "Looks like something went wrong..\n\n" + errorMessage
			},
			"accessory": {
				"type": "image",
				"image_url": "https://media1.tenor.com/images/9da77d73c29eab1814ab4ca4f42948ea/tenor.gif?itemid=7883209",
				"alt_text": "plants"
			}
        })
        body = response['body']
        for body_detail in body:
            blocks.append({
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": "*" + body_detail.get('key') + "* _" + body_detail.get('summary') + "_\n>" + body_detail.get('message')
                }
            })
    else:
        blocks.append({
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*Release " + release_title + "*"
            }
        })
        blocks.append({"type": "divider"})
        services = response['serviceToTicketCodes']
        for service_name, tickets in services.items():
            text = "*" + service_name.title() + "*"
            for ticket in tickets:
                text += "\n• <https://meggotdigital.atlassian.net/browse/" + ticket + "|" + ticket + ">"

            blocks.append({
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": text
                }
            })
        blocks.append({"type": "divider"})
        blocks.append({
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "Do you want to release these items?"
            }
        })
        blocks.append({
            "type": "actions",
            "block_id": "start_release",
            "elements": [
                {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "text": "Start Release"
                    },
                    "style": "primary",
                    "value": "start_release_" + response['projectCode']
                },
                {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "text": "Cancel"
                    }
                }
            ]
        })
    return blocks
