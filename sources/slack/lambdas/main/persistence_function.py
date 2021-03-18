import boto3

def save_template(template, userIdProjectCode):
    dynamodb = boto3.resource('dynamodb', region_name='eu-west-1')
    table = dynamodb.Table('releasy_templates')
    table.put_item(
        Item={
            'user_id_project_code': userIdProjectCode,
            'template': template
        }
    )


def get_template(userIdProjectCode):
    dynamodb = boto3.resource('dynamodb', region_name='eu-west-1')
    table = dynamodb.Table('releasy_templates')
    databaseResponse = table.get_item(
        Key={
            'user_id_project_code': userIdProjectCode
        }
    )
    item = databaseResponse.get('Item')
    if item is not None:
        return item.get('template')
    return

def save_release(releaseStatus, releaseTitle):
    dynamodb = boto3.resource('dynamodb', region_name='eu-west-1')
    table = dynamodb.Table('releasy_releases')
    table.put_item(
        Item={
            'release_title': releaseTitle,
            'release': releaseStatus
        }
    )

def get_release(releaseTitle):
    dynamodb = boto3.resource('dynamodb', region_name='eu-west-1')
    table = dynamodb.Table('releasy_releases')
    databaseResponse = table.get_item(
        Key={
            'release_title': releaseTitle
        }
    )
    item = databaseResponse.get('Item')
    if item is not None:
        return item.get('release')
    return