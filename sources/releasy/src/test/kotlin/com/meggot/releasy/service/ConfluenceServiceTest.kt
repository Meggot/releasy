package com.meggot.releasy.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.meggot.releasy.model.dto.releasy.ReleasyException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ConfluenceServiceTest {

    private val confluenceChildHtmlBuilderMock = mockk<ConfluenceChildHtmlBuilder>()

    private val restTemplateMock = mockk<RestTemplate>()

    private val objectMapperMock = jacksonObjectMapper()

    private val confluenceService = ConfluenceService(confluenceChildHtmlBuilderMock, restTemplateMock, objectMapperMock)

    @Test
    fun `Error is thrown when Jira Username cannot be mapped to a Jira Account ID`() {
        val responseEntity = ResponseEntity("", HttpStatus.NOT_FOUND)
        every { restTemplateMock.getForEntity(any<String>(), String::class.java) } returns responseEntity

        assertFailsWith<ReleasyException> {
            confluenceService.mapJiraUsernamesIntoJiraAccountIds(listOf<String>("Joseph.Rowe", "Bradley.Williams"))
        }
    }

    @Test
    fun `Assert that Jira Usernames are mapped to Jira Account IDs correctly`() {
        val joe = "{\n" +
                "    \"self\": \"https://meggot.atlassian.net/rest/api/2/user?accountId=5d19e65d5e43080ce8bf5e2c\",\n" +
                "    \"accountId\": \"5d19e65d5e43080ce8bf5e2c\",\n" +
                "    \"accountType\": \"atlassian\",\n" +
                "    \"emailAddress\": \"joseph.rowe@meggot.com\",\n" +
                "    \"avatarUrls\": {\n" +
                "        \"48x48\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5d19e65d5e43080ce8bf5e2c/d1b21cbf-4a4f-456e-8055-8fea1fc513d2/128?size=48&s=48\",\n" +
                "        \"24x24\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5d19e65d5e43080ce8bf5e2c/d1b21cbf-4a4f-456e-8055-8fea1fc513d2/128?size=24&s=24\",\n" +
                "        \"16x16\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5d19e65d5e43080ce8bf5e2c/d1b21cbf-4a4f-456e-8055-8fea1fc513d2/128?size=16&s=16\",\n" +
                "        \"32x32\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5d19e65d5e43080ce8bf5e2c/d1b21cbf-4a4f-456e-8055-8fea1fc513d2/128?size=32&s=32\"\n" +
                "    },\n" +
                "    \"displayName\": \"Joseph Rowe\",\n" +
                "    \"active\": true,\n" +
                "    \"timeZone\": \"Europe/London\",\n" +
                "    \"locale\": \"en_GB\",\n" +
                "    \"groups\": {\n" +
                "        \"size\": 5,\n" +
                "        \"items\": []\n" +
                "    },\n" +
                "    \"applicationRoles\": {\n" +
                "        \"size\": 1,\n" +
                "        \"items\": []\n" +
                "    },\n" +
                "    \"expand\": \"groups,applicationRoles\"\n" +
                "}"

        val brad = "{\n" +
                "    \"self\": \"https://meggot.atlassian.net/rest/api/2/user?accountId=5b2cf22c53650a265f9a1f3e\",\n" +
                "    \"accountId\": \"5b2cf22c53650a265f9a1f3e\",\n" +
                "    \"accountType\": \"atlassian\",\n" +
                "    \"emailAddress\": \"bradley.williams@meggot.com\",\n" +
                "    \"avatarUrls\": {\n" +
                "        \"48x48\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5b2cf22c53650a265f9a1f3e/6520a393-ec43-4b17-b91f-f0ed18612cbd/128?size=48&s=48\",\n" +
                "        \"24x24\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5b2cf22c53650a265f9a1f3e/6520a393-ec43-4b17-b91f-f0ed18612cbd/128?size=24&s=24\",\n" +
                "        \"16x16\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5b2cf22c53650a265f9a1f3e/6520a393-ec43-4b17-b91f-f0ed18612cbd/128?size=16&s=16\",\n" +
                "        \"32x32\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/5b2cf22c53650a265f9a1f3e/6520a393-ec43-4b17-b91f-f0ed18612cbd/128?size=32&s=32\"\n" +
                "    },\n" +
                "    \"displayName\": \"Williams, Bradley\",\n" +
                "    \"active\": true,\n" +
                "    \"timeZone\": \"GMT\",\n" +
                "    \"locale\": \"en\",\n" +
                "    \"groups\": {\n" +
                "        \"size\": 5,\n" +
                "        \"items\": []\n" +
                "    },\n" +
                "    \"applicationRoles\": {\n" +
                "        \"size\": 1,\n" +
                "        \"items\": []\n" +
                "    },\n" +
                "    \"expand\": \"groups,applicationRoles\"\n" +
                "}"

        val responseEntities = mapOf("joseph.rowe" to ResponseEntity(joe, HttpStatus.OK), "bradley.williams" to ResponseEntity(brad, HttpStatus.OK))
        val usernames = listOf<String>("joseph.rowe", "bradley.williams")
        usernames.forEach() {
            every { restTemplateMock.getForEntity("${ConfluenceService.JIRA_API}/user/?username=$it", String::class.java) } returns responseEntities[it]
        }
        val ids = confluenceService.mapJiraUsernamesIntoJiraAccountIds(usernames)
        assertTrue(ids.size == 2)
        assertEquals(ids[0], "5d19e65d5e43080ce8bf5e2c")
        assertEquals(ids[1], "5b2cf22c53650a265f9a1f3e")
    }
}