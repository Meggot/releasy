import json
import logging

from slack_function import *
from lambda_function import *
from persistence_function import *

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handle_slash_command(event):
    logger.info("Received new /command")
    logger.info(event)
    command = event['command']

    name = event['user_name'].split('.')[0].capitalize()
    post_ephemeral_message(event['channel_id'], event['user_id'],
                           [create_section("Received your {} command {}, we are processing it now.. :bow:".format(command, name))])

    if command == '/releasy-create':
        handle_create_release(event)
    elif command == '/releasy-start':
        handle_start_release(event)
    elif command == '/releasy-status':
        handle_status_release(event)
    elif command == '/releasy-stop':
        handle_stop_release(event)


def handle_create_release(event):
    logger.info('Received a create release command with text ' + event['text'])
    team_name = event['text']
    name = event['user_name'].split('.')[0].capitalize()
    if team_name is None:
        post_ephemeral_message(event['channel_id'], event['user_id'], [create_section("You have to provide a team name {} you jabroni".format(name))])
        return

    username = event['user_name']

    response = call_releasy_validate(username.replace("_", "."), event)

    blocks = validate_response_to_slack_blocks(response)

    release_title = response.get('releaseTitle')

    if release_title is not None:
        save_template(response, event['user_id'] + "-" + team_name)

    post_ephemeral_message(event['channel_id'], event['user_id'], blocks)

def handle_start_release(event):
    logger.info('Received a start release command with text ' + event['text'])
    name = event['user_name'].split('.')[0].capitalize()
    release_title = event['text']

    if release_title is None:
        post_ephemeral_message(event['channel_id'], event['user_id'],
                              [create_section("Woah {}, you need to provide a release title. Like: '#512'".format(name))])

    saved_release = get_release(release_title)
    if saved_release is None:
        post_ephemeral_message(event['channel_id'], event['user_id'],
                               [create_section("Hmm {}, cannot find a release by that title. Did you make sure to add the hashtag?".format(name))])

    blocks = create_slack_block_of_release(saved_release)

    post_ephemeral_message(event['channel_id'], event['user_id'], blocks)


def handle_status_release(event):
    logger.info('Received a status release command with text ' + event['text'])

def handle_stop_release(event):
    logger.info('Received a stop release command with text ' + event['text'])


