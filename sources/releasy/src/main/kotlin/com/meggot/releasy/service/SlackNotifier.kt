package com.meggot.releasy.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.meggot.releasy.model.dto.slack.SlackMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class SlackNotifier(
        @Qualifier("slackRestTemplate") var slackRestTemplate: RestTemplate
) {
    var slackApiUrl = "https://hooks.slack.com/services/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"

    private val log = LoggerFactory.getLogger(this::class.java)

    fun postTextMessageToReleasyChannel(message: String) {
        slackRestTemplate.postForEntity(URI(slackApiUrl), jacksonObjectMapper().writeValueAsString(SlackMessage(message)), String::class.java)
    }
}