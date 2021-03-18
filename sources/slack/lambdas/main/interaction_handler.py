import json
import logging

from releasy_function import *
from persistence_function import *
from slack_function import *

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# TODO: This class and interaction need some serious refactoring...
def handle_interaction(event):
    logger.info("Received button press")
    logger.info(event)
    payload = json.loads(event['payload'])
    logger.info(payload)
    userId = payload['user']['id']
    if 'actions' in payload:
        action = payload['actions'][0]
        logger.info(action)
        if action['block_id'] == "start_release":
            handle_start_release_button(action, userId)
        elif action['block_id'] == "deploy_release":
            handle_deploy_release_button(action, userId)
        elif action['action_id'].startswith("job_update"):
            handle_job_update_dropdown(action, userId)
    post_update(payload)

def handle_deploy_release_button(action, userid):
    logger.info("Received a deploy release button press from " + userid)
    releaseTitle = action['value'].split("_")[2]
    release = get_release(releaseTitle)
    logger.info(release)
    call_releasy_deploy(release)

def handle_start_release_button(action, userId):
    logger.info("Received a start release button press")
    projectCode = action['value'].split("_")[2]
    userIdProjectCode = userId + "-" + projectCode
    projectTemplate = get_template(userIdProjectCode)
    logger.info(projectTemplate)
    startReleaseResponse = call_releasy_create(projectTemplate)
    logger.info('Saving release')
    logger.info(startReleaseResponse)
    releaseTitle = startReleaseResponse.get('releaseTemplate').get('releaseTitle')
    save_release(startReleaseResponse, releaseTitle)

# Example job_update action payload!
# {
#     'type': 'static_select',
#     'action_id': 'job_update',
#     'block_id': '=UI',
#     'selected_option': {
#         'text': {
#             'type': 'plain_text',
#             'text': ':warning: Skip',
#             'emoji': True
#         },
#         'value': 'Skip_98a9e8d8-ef44-4cc0-a300-f0f8b80a0eb7'
#     },
#     'initial_option': {
#         'text': {
#             'type': 'plain_text',
#             'text': ':white_check_mark: Run',
#             'emoji': True
#         },
#         'value': 'Run_98a9e8d8-ef44-4cc0-a300-f0f8b80a0eb7'
#     },
#     'placeholder': {
#         'type': 'plain_text',
#         'text': 'Job Action',
#         'emoji': True
#     },
#     'action_ts': '1583935945.763029'
# }
def handle_job_update_dropdown(action, userId):
    logger.info("Received a job_update_dropdown change.")
    releaseTitle = action['action_id'].split("_")[2]
    logger.info("Updating job on release " + releaseTitle)
    savedRelease = get_release(releaseTitle)
    logger.info(savedRelease)
    dropdownSelectedValue = action['selected_option']['value']
    selectedDropdownAction = dropdownSelectedValue.split("_")[0]
    selectedJobId = dropdownSelectedValue.split("_")[1]
    logger.info(selectedJobId)
    logger.info(selectedDropdownAction)
    for jobStatus in savedRelease['jenkinsJobStatuses']:
        if jobStatus['job']['id'] == selectedJobId:
            if selectedDropdownAction == 'Skip':
                jobStatus['status'] = "SKIPPED"
                logger.info("Updating job " + jobStatus['job']['jobName'] + " to SKIPPED!")
            else:
                jobStatus['status'] = "PENDING"
                logger.info("Updating job " + jobStatus['job']['jobName'] + " to PENDING!")
    save_release(savedRelease, releaseTitle)