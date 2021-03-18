package com.meggot.releasy.service

import com.amazonaws.services.sns.AmazonSNS
import com.fasterxml.jackson.databind.ObjectMapper
import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import org.springframework.stereotype.Service

@Service
class SnsService(var snsClient: AmazonSNS,
                 var objectMapper: ObjectMapper) {

    var topic = "arn:aws:sns:eu-west-1:880293583565:releasy-updates"

    fun publishUpdate(releaseStatus: ReleaseStatus) {
        snsClient.publish(topic, objectMapper.writeValueAsString(releaseStatus))
    }

}