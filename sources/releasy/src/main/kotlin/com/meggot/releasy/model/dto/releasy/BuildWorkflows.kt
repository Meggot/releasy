package com.meggot.releasy.model.dto.releasy

import com.meggot.releasy.model.dto.jenkins.JenkinsJobStatus
import com.meggot.releasy.model.dto.jenkins.JobStatusName
import com.meggot.releasy.model.dto.releasy.jobs.*
import org.springframework.stereotype.Service

@Service
class BuildWorkflows {

    val services = listOf(
            "api-coordinator",
            "audit",
            "bank",
            "beneficiary",
            "booking",
            "content",
            "corridor",
            "country",
            "currency",
            "customer",
            "data-store",
            "document",
            "dsar",
            "duedil",
            "edd",
            "email",
            "fixedorder",
            "fraud",
            "fund",
            "hook",
            "kyc",
            "location",
            "order",
            "party",
            "quote",
            "rate",
            "reconciliation",
            "remittance",
            "salesforce-adapter",
            "sanction",
            "scheduler",
            "sender",
            "sms",
            "template",
            "ticket",
            "token",
            "transfer",
            "user")

    fun getWorkflow(releaseTemplate: ReleaseTemplate): List<JenkinsJobStatus> {
        val releaseJobs = createReleaseJobs(releaseTemplate.serviceToTicketCodes)
                .map { JenkinsJobStatus(it, JobStatusName.PENDING) }
                .toCollection(mutableListOf())

        val serviceKeyList = releaseTemplate.serviceToTicketCodes.keys.filter { services.contains(it.toLowerCase()) }.toCollection(HashSet())

        // No fluent function to do this =(
        releaseJobs.addAll(listOf(
                JenkinsJobStatus(DeployJenkinsJob(Environment.UAT, releaseTemplate.releaseTitle)),
                JenkinsJobStatus(StackWatcherJob(Environment.UAT)),
                JenkinsJobStatus(PlatformIntegrationJob(Environment.UAT, serviceKeyList)),
                JenkinsJobStatus(V2AcceptanceTestsJob(Environment.UAT, serviceKeyList)),
                JenkinsJobStatus(PromoteJob(serviceKeyList)),
                JenkinsJobStatus(DeployJenkinsJob(Environment.SANDBOX, releaseTemplate.releaseTitle)),
                JenkinsJobStatus(DeployJenkinsJob(Environment.PRODUCTION, releaseTemplate.releaseTitle))
        ))

        return releaseJobs
    }

    private fun createReleaseJobs(serviceToTickets: Map<String, List<String>>): List<JenkinsJob> {
        return serviceToTickets.filter { services.contains(it.key.toLowerCase()) }
                .map {
                    ReleaseJenkinsJob(it.key.toLowerCase())
                }.toCollection(mutableListOf())
    }
}
