package com.meggot.releasy.service

import com.meggot.releasy.model.dto.jenkins.JobStatusName
import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import org.springframework.stereotype.Service

@Service
class ConfluenceChildHtmlBuilder {

    fun buildContent(releaseStatus: ReleaseStatus, releaseManagersAccountIds: List<String>): String {
        val releaseSummary = generateSummary(releaseStatus.releaseTemplate.serviceToTicketCodes)

        val releaseManager = generateReleaseManagers(releaseManagersAccountIds)

        val releaseNotes = wrapInLink(releaseStatus.jiraReleaseLink)

        val testingNotes = generateTestingNotes(releaseStatus)

        return "<p><i>Please note that any edits made to this page while Releasy runs will be overwritten. Please include any notes by adding a Comment at the bottom of the page rather than by editing the page itself.</i></p>" +
                "<table>" +
                "<tr> <th> <strong> Release ${releaseStatus.releaseTemplate.releaseTitle} Status </strong> </th> <td> ${releaseStatus.status.prettyName}</td> </tr>" +
                "<tr> <th> Release Manager </th> <td> $releaseManager </td> </tr> " +
                "<tr> <th> Summary </th> <td> $releaseSummary </td> </tr> " +
                "<tr> <th> Release Notes </th> <td> $releaseNotes </td> </tr>" +
                "<tr> <th> Testing Notes </th> <td> $testingNotes </td> </tr>" +
                "<tr> <th> Test Failure Sign Off </th> <td> -- TBA -- </td> </tr>" +
                "<tr> <th> Deployments PR </th> <td> -- TBA-- </td> </tr>" +
                "<tr> <th> Release Sign Off </th> <td> -- TBA-- </td> </tr>" +
                "</table>"
    }


    private fun generateSummary(servicesToProjects: HashMap<String, ArrayList<String>>): String {
        var summary = ""
        servicesToProjects.forEach { t, u ->
            run {
                summary += "<strong> $t :</strong>"
                u.forEachIndexed { _, ticket ->
                    summary += "<ac:structured-macro ac:name='jira'>" +
                            "<ac:parameter ac:name='columns'>key,summary,type,created,updated,due,assignee,reporter,priority,status,resolution</ac:parameter>" +
                            "<ac:parameter ac:name='key'>$ticket</ac:parameter>" +
                            "</ac:structured-macro>"
                }
                summary += "<hr></hr>"
            }
        }
        return summary;
    }

    private fun generateReleaseManagers(jiraAccountId: List<String>): String {
        return jiraAccountId.map { "<ac:link> <ri:user ri:account-id='$it'/></ac:link>" }.joinToString { "$it" }
    }

    private fun wrapInLink(jiraLink: String): String {
        return "<a href='$jiraLink'>$jiraLink</a>"
    }

    private fun generateTestingNotes(releaseStatus: ReleaseStatus): String {
        if (releaseStatus.jenkinsJobStatuses.isEmpty()) {
            return "--TBA--"
        }
        var testingNotes = ""
        var testsNumber = 0;
        var testsPassed = 0;
        releaseStatus.jenkinsJobStatuses.stream()
                .filter {
                    it.job.jobName.equals("Platform-integration-test")
                            || it.job.jobName.equals("V2 Acceptance Tests")
                            && it.buildLink != null
                }.forEach {
                    val link = wrapInLink(it.buildLink!!)
                    testsNumber += 1
                    testingNotes += "${it.status.confluenceStatusDisplay} ${it.status.prettyName} ${it.job.jobName} $link"
                    testingNotes += "<hr></hr>"
                    if (it.status == JobStatusName.SUCCESSFUL) testsPassed += 1
                }
        testingNotes += "Tests ($testsPassed/$testsNumber) have passed."
        return testingNotes;
    }

}
