package com.meggot.releasy.configuration

import com.google.common.collect.Lists
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets


@Configuration
open class RestTemplateConfiguration {

    @Value("\${releasy.confluence.authToken}")
    lateinit var confluenceAuthToken: String

    @Bean
    open fun confluenceRestTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        val interceptors: MutableList<ClientHttpRequestInterceptor> = Lists.newArrayList(RequestResponseLoggingInterceptor())
        interceptors.add(HeaderRequestInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
        interceptors.add(HeaderRequestInterceptor("Authorization", confluenceAuthToken))
        interceptors.add(HeaderRequestInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
        restTemplate.interceptors = interceptors
        restTemplate.errorHandler = ReleasyErrorHandler()
        return restTemplate
    }

    @Bean
    open fun slackRestTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        val interceptors: MutableList<ClientHttpRequestInterceptor> = Lists.newArrayList(RequestResponseLoggingInterceptor())
        interceptors.add(HeaderRequestInterceptor("Content-type", "application/json"))
        restTemplate.interceptors = interceptors
        restTemplate.errorHandler = ReleasyErrorHandler()
        return restTemplate
    }

    class HeaderRequestInterceptor(private val headerName: String, private val headerValue: String) : ClientHttpRequestInterceptor {
        @Throws(IOException::class)
        override fun intercept(request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution): ClientHttpResponse {
            request.getHeaders().set(headerName, headerValue)
            return execution.execute(request, body)
        }

    }

    class ReleasyErrorHandler : ResponseErrorHandler {
        override fun hasError(var1: ClientHttpResponse?): Boolean {
            return false
        }

        override fun handleError(var1: ClientHttpResponse?) {

        }

        override fun handleError(url: URI?, method: HttpMethod?, response: ClientHttpResponse?) {
            this.handleError(response)
        }
    }

    class RequestResponseLoggingInterceptor : ClientHttpRequestInterceptor {

        private val log = LoggerFactory.getLogger(this::class.java)

        @Throws(IOException::class)
        override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
            logRequest(request, body)
            val response = execution.execute(request, body)
            logResponse(response)
            return response
        }

        @Throws(IOException::class)
        private fun logRequest(request: HttpRequest, body: ByteArray) {
            log.info("===========================request begin================================================")
            log.info("URI         : {}", request.uri)
            log.info("Method      : {}", request.method)
            log.info("Headers     : {}", request.headers)
            log.info("Request body: {}", String(body, StandardCharsets.UTF_8))
            log.info("==========================request end================================================")
        }

        @Throws(IOException::class)
        private fun logResponse(response: ClientHttpResponse) {
            log.info("============================response begin==========================================")
            log.info("Status code  : {}", response.statusCode)
            log.info("Status text  : {}", response.statusText)
            log.info("Headers      : {}", response.headers)
            log.info("=======================response end=================================================")
        }
    }
}