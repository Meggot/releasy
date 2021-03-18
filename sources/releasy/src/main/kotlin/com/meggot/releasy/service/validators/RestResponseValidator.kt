package com.meggot.releasy.service.validators

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.meggot.releasy.model.dto.confluence.ConfluenceSearchResponse
import com.meggot.releasy.model.dto.confluence.IssueFields
import com.meggot.releasy.model.dto.jira.JiraReleaseResponse
import com.meggot.releasy.model.dto.jira.JiraUser
import com.meggot.releasy.model.dto.jira.UpdateComponentResponse
import com.meggot.releasy.model.dto.releasy.ErrorDetail
import com.meggot.releasy.model.dto.releasy.IssueErrorDetail
import com.meggot.releasy.model.dto.releasy.ReleasyException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class RestResponseValidator {

    val log = LoggerFactory.getLogger(this::class.java)

    fun validateConfluenceSearchResponse(response: ResponseEntity<ConfluenceSearchResponse>, projectCode: String): List<ErrorDetail> {
        when {
            response.statusCode.value() == 404 -> throw ReleasyException("Project  by code $projectCode was not found.")
            response.statusCode.value() == 500 -> throw ReleasyException("Something went wrong on Confluence side when trying to retrieve $projectCode")
        }
        response.body ?: kotlin.run {
            log.error("Failed to parse response: {}", response)
            throw ReleasyException("Couldn't parse response.")
        }
        response.body!!.issues
                ?: throw ReleasyException("No issues found that are ready for release in that Project code " + projectCode)
        val errorDetails: ArrayList<ErrorDetail> = arrayListOf()
        response.body!!.issues!!.forEach { issue ->
            issue.fields
                    ?: errorDetails.add(IssueErrorDetail(issue.key, issue.fields!!.summary, "No fields found for this ticket"))
            if (issue.fields!!.components?.isEmpty()!!) {
                log.info("Issue {} has no component field", issue.key)
                errorDetails.add(IssueErrorDetail(issue.key, issue.fields!!.summary, "No component found for this ticket."))
            }
        }
        if (errorDetails.isNotEmpty()) {
            log.error("Error Details: {}", jacksonObjectMapper().writeValueAsString(errorDetails))
            log.info("Response body {}", jacksonObjectMapper().writeValueAsString(response.body!!))
            throw ReleasyException("Confluence search for issues has failed.", errorDetails)
        }
        return errorDetails
    }

    fun validateJiraPostReleaseResponse(response: ResponseEntity<JiraReleaseResponse>) {
        log.info("Validating Jira Response $response")
        when {
            response.statusCode.value() != 201 -> throw ReleasyException("Jira release has failed")
        }

        response.body ?: kotlin.run {
            log.error("Failed to parse response $response")
            throw ReleasyException("Failed to parse response");
        }
    }

    fun validateUpdateComponentResponse(response: ResponseEntity<UpdateComponentResponse>) {
        log.info("Validating update component Response $response")
        when {
            response.statusCode.value() != 204 -> throw ReleasyException("Failed to update issues with their fix version")
        }
    }

    fun validateGetAccountId(response: ResponseEntity<JiraUser>) {
        log.info("Validating get account via id response $response")
        when {
            response.statusCode.value() != 200 -> throw ReleasyException("Can't find user by that username")
        }
    }
}