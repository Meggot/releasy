from interaction_handler import *
from slash_handlers import *

def lambda_handler(event, context):
    if 'payload' in event:
        handle_interaction(event)
    else:
        handle_slash_command(event)