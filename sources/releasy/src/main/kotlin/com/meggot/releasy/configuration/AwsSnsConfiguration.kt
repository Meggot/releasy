package com.meggot.releasy.configuration

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class AwsSnsConfiguration {

    @Bean
    open fun amazonSnsClient(): AmazonSNS {
        return AmazonSNSClientBuilder.defaultClient()
    }
}