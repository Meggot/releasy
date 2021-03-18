package com.meggot.releasy.service

import com.meggot.releasy.model.dto.jenkins.JenkinsResult
import com.meggot.releasy.model.dto.releasy.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
open class ReleasyService(private val jiraReleaseService: JiraReleaseService,
                          private var jenkinsService: JenkinsService,
                          private val confluenceService: ConfluenceService,
                          private var releaseUpdateHandler: ReleaseUpdateHandler,
                          private var buildWorkflows: BuildWorkflows) {


    fun createReleaseTemplate(projectCode: String, getReleaseTemplateBody: GetReleaseTemplateBody): ReleaseTemplate {
        confluenceService.mapJiraUsernamesIntoJiraAccountIds(getReleaseTemplateBody.releaseManager)
        if (!jiraReleaseService.isUserAdministratorOnProject(projectCode)) {
            throw ReleasyException("Releasy user is not authorized to create versions on project " + projectCode + ". Please add" +
                    " 'Releasy' user as an Administrator in Jira.")
        }
        return ReleaseTemplate("#" + confluenceService.getNextReleaseNumber().toString(),
                jiraReleaseService.getServicesAndTicketsMap(projectCode),
                getReleaseTemplateBody.releaseManager,
                ProjectCode.valueOf(projectCode))
    }

    fun postReleaseTemplate(releaseTemplate: ReleaseTemplate): ReleaseStatus {
        val jiraRelease = jiraReleaseService.postJiraRelease(releaseTemplate)

        jiraReleaseService.attachReleaseToTickets(releaseTemplate.releaseTitle, releaseTemplate.serviceToTicketCodes)


        val workflows = buildWorkflows.getWorkflow(releaseTemplate)

        val releaseStatus = ReleaseStatus(ReleaseStatusName.NOTES_CREATED,
                jiraReleaseService.getUrlForJiraRelease(releaseTemplate.projectCode.name, jiraRelease.id),
                jiraRelease.id,
                releaseTemplate,
                "",
                "",
                workflows)

        confluenceService.pushReleaseStatusToConfluence(releaseStatus)

        releaseUpdateHandler.postReleaseStatusUpdate(releaseStatus = releaseStatus)

        return releaseStatus
    }

    @Async
    open fun runJenkinsJobs(release: ReleaseStatus): JenkinsResult {
        GlobalScope.launch {
            jenkinsService.releaseServices(release)
        }

        return JenkinsResult(true)
    }

}
