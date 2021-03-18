package com.meggot.releasy.service

import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName
import org.springframework.stereotype.Service

@Service
class ReleaseUpdateHandler(var slackNotifier: SlackNotifier,
                           var slackMessageBuilder: SlackMessageBuilder,
                           var confluenceService: ConfluenceService,
                           var snsService: SnsService) {

    fun postReleaseStatusUpdate(startStatus: ReleaseStatusName = ReleaseStatusName.DRAFT, releaseStatus: ReleaseStatus) {
        slackNotifier.postTextMessageToReleasyChannel(slackMessageBuilder.generateMainChannelMessage(startStatus, releaseStatus))
        confluenceService.pushReleaseStatusToConfluence(releaseStatus)
    }

}