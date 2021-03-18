# release_bot

Welcome to the release_bot repo!

## Next steps
You should update this README once your bot is up and running, so if you're reading this, you probably want to get started, right?

Let's go! 👇

## Build everything!
* Clone this repo! `git clone git@github.com:meggot/botify-release_bot.git`
* Next, `cd` in to the repo and run `. ./run.sh` script. _(The first . is important for keeping AWS environment variables available if you run multiple times in the same shell)_
* This will create all of the AWS components required for your lambda to run.
* When complete, it will output the URL for your API-Gateway. This will look something like:  
`base_url = https://<SOME-UUID>.execute-api.eu-west-1.amazonaws.com/prod`

## Link to Slack
Now that your bot has been created, you need to create its Slack integration.
To do this, complete the following steps:
* Go to https://api.slack.com/apps and click 'Create New App'.
* Enter the name for your app and click 'Create App'.
* Under 'Features and Functionality', select 'Slash Commands'.
* Click 'Create New Command'.
* Enter the 'Name' for your command (This is what your users will type to invoke your bot, e.g. /botify).
* Copy the 'base_url' that was output from the `run.sh` script in to the 'Request URL'.
* Add a description (This will be shown when someone enters the Slash Command in Slack)
* Save the Slash Command.
* On the right hand menu, select 'Install App', and click the 'Install App to Workspace' button.
* Click 'Allow' to approve the permission request.
* That's it!

## Try it!
Go to Slack and invoke your bot by entering the Slash command you defined. It should say 'Hello world!'

## Next?
Change the `main.py` function to do whatever you want your bot to do, then execute `. ./run.sh` again to update everything.
And don't forget to update this README!
