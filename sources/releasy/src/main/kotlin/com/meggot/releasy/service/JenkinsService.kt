package com.meggot.releasy.service

import com.meggot.releasy.jenkins.JenkinsRunner
import com.meggot.releasy.model.dto.jenkins.JenkinsJobStatus
import com.meggot.releasy.model.dto.jenkins.JobStatusName
import com.meggot.releasy.model.dto.releasy.ReleaseStatus
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName
import com.meggot.releasy.model.dto.releasy.ReleasyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@Slf4j
class JenkinsService(var jenkinsRunner: JenkinsRunner,
                     var releaseUpdateHandler: ReleaseUpdateHandler) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun releaseServices(releaseStatus: ReleaseStatus) {
        var preJobStatus = releaseStatus.status
        releaseStatus.status = ReleaseStatusName.RUNNING_JENKINS_WORKFLOW
        releaseStatus.jenkinsJobStatuses
                .filter { it.status == JobStatusName.PENDING }
                .map {
                    runJenkinsJobOnRelease(releaseStatus, it)
                            .onFailure {
                                releaseUpdateHandler.postReleaseStatusUpdate(releaseStatus = releaseStatus)
                                throw ReleasyException("Looks like something went wrong. ${it.message}")
                            }
                }
        releaseUpdateHandler.postReleaseStatusUpdate(preJobStatus, releaseStatus)
    }

    private suspend fun runJenkinsJobOnRelease(release: ReleaseStatus, jobStatusInput: JenkinsJobStatus): Result<JenkinsJobStatus> = withContext(Dispatchers.IO) {
        var job = jobStatusInput.job
        log.info("Running job {}", job.jobName)
        jobStatusInput.status = JobStatusName.IN_PROGRESS
        releaseUpdateHandler.postReleaseStatusUpdate(releaseStatus = release)
        val jobStatus = jenkinsRunner.processJenkinsJob(job)
        jobStatusInput.buildLink = jobStatus.buildLink
        jobStatusInput.success = jobStatus.success
        jobStatusInput.job = jobStatus.job
        jobStatusInput.status = jobStatus.status
        log.info("Job {} has status of {}. Success? {}", jobStatus.job.jobName, jobStatus.status, jobStatus.success)
        if (jobStatus.success) {
            release.status = jobStatus.job.targetStatus
            return@withContext Result.success(jobStatusInput)
        } else {
            release.status = job.failureStatus
            return@withContext Result.failure<JenkinsJobStatus>(RuntimeException("Build was not a success: ${job.jobName}"))
        }
    }
}