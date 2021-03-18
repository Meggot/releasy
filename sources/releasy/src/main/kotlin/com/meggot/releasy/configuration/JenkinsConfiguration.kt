package com.meggot.releasy.configuration

import com.offbytwo.jenkins.JenkinsServer
import lombok.extern.slf4j.Slf4j
import org.apache.http.client.HttpResponseException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI
import java.util.*

@Configuration
@Slf4j
open class JenkinsConfiguration {

    @Value("\${releasy.jenkins.username}")
    lateinit var username: String

    @Value("\${releasy.jenkins.password}")
    lateinit var password: String

    @Value("\${releasy.jenkins.url}")
    lateinit var jenkinsUrl: String

    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    open fun jenkinsServer(): JenkinsServer {
        val server = JenkinsServer(URI(jenkinsUrl), this.username, this.password)
        // Validate credentials
        try {
            log.info("Attemping to connect to jenkins...")
            server.jobs
            log.info("Connected to Jenkins service.")
        } catch (e: HttpResponseException) {
            if (e.statusCode == 401 || e.statusCode == 403) {
                // Invalid credentials
                throw RuntimeException("Jenkins doesn't like your credentials", e)
            }
            // Don't know, just rethrow the original
            throw e
        }
        return server
    }
}