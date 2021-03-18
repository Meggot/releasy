package com.meggot.releasy.service

import com.meggot.releasy.model.dto.releasy.ProjectCode
import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName
import org.springframework.stereotype.Service

@Service
class SlackMessageBuilder {

    fun generateMainChannelMessage(startStatus: ReleaseStatusName = ReleaseStatusName.DRAFT, releaseStatus: ReleaseStatus): String {
        return generateTitle(releaseStatus.releaseTemplate.releaseTitle,
                releaseStatus.releaseTemplate.projectCode,
                releaseStatus.releaseTemplate.releaseManager) +
                generateHeadline(startStatus, releaseStatus) +
                generateStatusBlock(releaseStatus)
    }

    private fun generateTitle(releaseTitle: String, projectCode: ProjectCode, releaseManager: List<String>): String {
        var releaseManagers = releaseManager.map { "<@$it>" }
        return ":rocket: *${projectCode.prettyName} Release $releaseTitle* _managed by $releaseManagers _\n"
    }


    private fun generateHeadline(startStatus: ReleaseStatusName, releaseStatus: ReleaseStatus): String {
        return ":dizzy: *Status:* ${releaseStatus.status.prettyName} _(previously was ${startStatus.prettyName})_\n "
    }


    private fun generateStatusBlock(releaseStatus: ReleaseStatus): String {
        var statusBlock = ""
        val releaseStage = releaseStatus.status.releaseStage
        if (releaseStage > 1) {
            statusBlock = ":jira: _ Release: ${releaseStatus.jiraReleaseLink} _\n" +
                    ":spiral_note_pad: _ Notes: ${releaseStatus.confluenceLink} _\n" +
                    getPrettyTicketLists(releaseStatus)
        }
        if (releaseStage > 2) {
            var buildFailed = false
            if (releaseStatus.jenkinsJobStatuses.size > 0) {
                releaseStatus.jenkinsJobStatuses.forEach {
                    statusBlock += " \n>" + it.getJobSlackLine()
                    if (!it.success) {
                        buildFailed = true
                    }
                }
            }
            if (buildFailed) {
                statusBlock += "\nThe build has failed somewhere.. please investigate and continue."
            }
        }

        return statusBlock
    }

    private fun getPrettyTicketLists(releaseStatus: ReleaseStatus): String {
        var ticketText = ""
        var serviceNumber = 1;
        var numberOfTickets = 0;
        releaseStatus.releaseTemplate.serviceToTicketCodes.forEach { service, tickets ->
            run {
                ticketText += "\n :general_jenkins: ${serviceNumber++}. $service: "
                tickets.forEachIndexed { index, ticketCode ->
                    run {
                        numberOfTickets++
                        ticketText += "[<https://meggotdigital.atlassian.net/browse/$ticketCode|$ticketCode>] "
                    }
                }
            }
        }
        val riskLevel = when {
            numberOfTickets <= 5 -> ":beers: Low Risk :beers:"
            numberOfTickets <= 10 -> ":worried: Medium Risk :worried:"
            numberOfTickets <= 15 -> ":rage: High Risk :rage:"
            else -> ":rip: Extremely High Risk :rip:"
        }
        ticketText += "\n_ :female-police-officer: There are $numberOfTickets tickets. This is classed as:_ $riskLevel _release._"
        return ticketText
    }

}
