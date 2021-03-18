package com.meggot.releasy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.Lists
import com.meggot.releasy.model.dto.confluence.ConfluenceChildPage
import com.meggot.releasy.model.dto.confluence.ConfluenceContentResponse
import com.meggot.releasy.model.dto.jira.JiraUser
import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import com.meggot.releasy.model.dto.releasy.ReleasyException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ConfluenceService(private val confluenceChildHtmlBuilder: ConfluenceChildHtmlBuilder,
                        private val confluenceRestTemplate: RestTemplate,
                        private val objectMapper: ObjectMapper) {

    companion object {
        val SPACE = ConfluenceChildPage.Space("EN")
        val NON_NUMERIC_REGEX = Regex("[^0-9]")
        const val TYPE = "40"
        const val STATUS = "current"
        const val PAGES_LINK = "https://meggotdigital.atlassian.net/wiki/spaces/EN/pages"
        const val CONFLUENCE_API = "https://meggotdigital.atlassian.net/wiki"
        const val RELEASE_PARENT_PAGE_ID = "46858298"
        const val JIRA_API = "https://meggotdigital.atlassian.net/rest/api/2"
        const val CONFLUENCE_URL = "$CONFLUENCE_API/rest/api/content/$RELEASE_PARENT_PAGE_ID/child/page?limit=500"
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getNextReleaseNumber(): Int {
        var confluenceContentResponse = confluenceRestTemplate.getForEntity(CONFLUENCE_URL, ConfluenceContentResponse::class.java)
        log.info("Confluence content response received: {}", objectMapper.writeValueAsString(confluenceContentResponse.body))
        if (confluenceContentResponse.hasBody()) {
            confluenceContentResponse = getLatestPage(confluenceContentResponse)
        }
        val currentReleaseTitle = confluenceContentResponse.body!!.results!![confluenceContentResponse.body!!.results!!.size - 1].title
        val currentReleaseNumber = Integer.parseInt(currentReleaseTitle.replace(NON_NUMERIC_REGEX, ""))
        return currentReleaseNumber + 1
    }

    private fun getLatestPage(_confluenceContentResponse: ResponseEntity<ConfluenceContentResponse>): ResponseEntity<ConfluenceContentResponse> {
        var confluenceContentResponse = _confluenceContentResponse
        while (!isLatestPage(confluenceContentResponse)) {
            confluenceContentResponse = getNextPage(confluenceContentResponse)
        }
        return confluenceContentResponse
    }

    private fun isLatestPage(confluenceContentResponse: ResponseEntity<ConfluenceContentResponse>): Boolean {
        return confluenceContentResponse.body!!._links.next != null
    }

    private fun getNextPage(confluenceContentResponse: ResponseEntity<ConfluenceContentResponse>): ResponseEntity<ConfluenceContentResponse> {
        val url = confluenceContentResponse.body!!._links.base + confluenceContentResponse.body!!._links.next
        log.info("Trying URL: $url")
        return confluenceRestTemplate.getForEntity(url, ConfluenceContentResponse::class.java)
    }

    fun pushReleaseStatusToConfluence(releaseStatus: ReleaseStatus): String {
        log.info("Pushing release status of release {} to confluence", releaseStatus.releaseTemplate.releaseTitle)
        val jiraAccountIds = mapJiraUsernamesIntoJiraAccountIds(releaseStatus.releaseTemplate.releaseManager)
        return if (releaseStatus.confluenceReleasePageId == "") {
            createReleasePage(releaseStatus, jiraAccountIds)
        } else {
            val releasePageResponse = confluenceRestTemplate.getForEntity("$CONFLUENCE_API/rest/api/content/${releaseStatus.confluenceReleasePageId}?expand=version", ConfluenceChildPage::class.java)
            updateReleasePage(releaseStatus, jiraAccountIds, releasePageResponse.body!!)
        }
    }

    private fun createReleasePage(releaseStatus: ReleaseStatus, jiraAccountIds: List<String>): String {
        val content = confluenceChildHtmlBuilder.buildContent(releaseStatus, jiraAccountIds)
        log.info("Creating confluence table: {}", objectMapper.writeValueAsString(content))
        val confluenceChildPage = generateConfluenceChildPage("3412", releaseStatus, content, 1)
        val response = confluenceRestTemplate.postForEntity("$CONFLUENCE_API/rest/api/content", confluenceChildPage, ConfluenceChildPage::class.java)
        releaseStatus.confluenceReleasePageId = response.body!!.id
        releaseStatus.confluenceLink = generateConfluenceUrl(response.body!!)
        log.info("Response: {}", objectMapper.writeValueAsString(response))
        return generateConfluenceUrl(response.body!!)
    }

    private fun updateReleasePage(releaseStatus: ReleaseStatus, jiraAccountIds: List<String>, confluenceChildPage: ConfluenceChildPage): String {
        val content = confluenceChildHtmlBuilder.buildContent(releaseStatus, jiraAccountIds)
        log.info("Updating confluence table: {} on page {}", objectMapper.writeValueAsString(content), confluenceChildPage.id)
        val releaseVersion = confluenceChildPage.version!!.number + 1
        val updatedPage = generateConfluenceChildPage(confluenceChildPage.id, releaseStatus, content, releaseVersion)
        confluenceRestTemplate.put("$CONFLUENCE_API/rest/api/content/${confluenceChildPage.id}", updatedPage)
        log.info("Response: {}", objectMapper.writeValueAsString(updatedPage))
        return generateConfluenceUrl(confluenceChildPage)
    }

    private fun generateConfluenceChildPage(id: String, releaseStatus: ReleaseStatus, content: String, version: Int): ConfluenceChildPage {
        return ConfluenceChildPage(id,
                releaseStatus.releaseTemplate.releaseTitle,
                TYPE,
                SPACE,
                STATUS,
                Lists.newArrayList(ConfluenceChildPage.Ancestors(RELEASE_PARENT_PAGE_ID)),
                ConfluenceChildPage.Body(ConfluenceChildPage.ChildStorage(content, "storage")),
                ConfluenceChildPage.Version(version))
    }

    private fun generateConfluenceUrl(confluenceChildPage: ConfluenceChildPage): String {
        return "$PAGES_LINK/${confluenceChildPage.id}/${confluenceChildPage.title}"
    }


    fun mapJiraUsernamesIntoJiraAccountIds(username: List<String>): List<String> {
        return username.map {
            val responseEntity = confluenceRestTemplate.getForEntity("$JIRA_API/user/?username=$it", String::class.java)
            if (responseEntity.statusCode == HttpStatus.NOT_FOUND) {
                throw ReleasyException("User by the name of $it does not exist")
            }
            (objectMapper.readValue<JiraUser>(responseEntity.body!!)).accountId
        }
    }
}