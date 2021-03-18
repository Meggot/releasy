package com.meggot.releasy.jenkins

import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.model.BuildResult
import com.offbytwo.jenkins.model.BuildWithDetails
import com.offbytwo.jenkins.model.JobWithDetails
import com.offbytwo.jenkins.model.QueueItem
import com.meggot.releasy.model.dto.releasy.jobs.JenkinsJob
import com.meggot.releasy.model.dto.jenkins.JenkinsJobStatus
import com.meggot.releasy.model.dto.jenkins.JobStatusName
import com.meggot.releasy.model.dto.releasy.ReleasyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.http.client.HttpResponseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JenkinsRunner(private val jenkins: JenkinsServer) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun processJenkinsJob(jenkinsJob: JenkinsJob): JenkinsJobStatus {
        var result = triggerJenkinsJob(jenkinsJob.jobName, jenkinsJob.getParameters())
        var buildResult = result.getOrThrow()
        var isSuccess = when (buildResult.result) {
            BuildResult.SUCCESS -> true
            else -> false
        }
        log.info("Result of job: ${jenkinsJob.jobName} = $buildResult")
        return JenkinsJobStatus(jenkinsJob,
                mapBuildResultToJobStatusName(buildResult.result),
                isSuccess,
                result.getOrThrow().url)

    }

    private suspend fun triggerJenkinsJob(
            name: String,
            buildParams: Map<String, String>
    ): Result<BuildWithDetails> = withContext(Dispatchers.IO) {
        var job = getJob(name)
        val queue = job.build(buildParams)
        var qi = jenkins.getQueueItem(queue)
        log.info("job $name queued")
        while (qi.executable == null || !qi.isCancelled && job.isInQueue) {
            // Waiting on queue
            // Update job and queue details
            delay(500)
            job = jenkins.getJob(name)
            qi = jenkins.getQueueItem(queue)
        }
        if (qi.isCancelled) {
            // job was cancelled
            return@withContext Result.failure<BuildWithDetails>(ReleasyException("Job was cancelled"))
        } else {
            val buildDetails = waitForBuild(qi)
            return@withContext Result.success(buildDetails)
        }
    }

    // "Hi, I'm from Releasy recruiting and I have a great opportunity working with Groovy at Jenkins"
    private fun getJob(name: String) = jenkins.getJob(name) ?: throw RuntimeException("Job with name $name not found")

    /**
     * Await the result of a build.
     * TODO::org.apache.http.client.HttpResponseException: status code: 504, reason phrase: GATEWAY_TIMEOUT is sometimes returned
     */
    private suspend fun waitForBuild(qi: QueueItem): BuildWithDetails {
        log.info("job ${qi.task.name} running")
        var numberOfTries = 0;
        do {
            delay(500)
            var isBuilding = true
            try {
                val build = jenkins.getBuild(qi)
                val details = build?.details()
                isBuilding = details!!.isBuilding
            } catch (exception: HttpResponseException) {
                log.error("Exception $exception")
                numberOfTries = numberOfTries++
            }
        } while (isBuilding && numberOfTries < 3)
        val result = jenkins.getBuild(qi).details()
        log.info("Job ${qi.task.name} finished")
        return result
    }

    private fun mapBuildResultToJobStatusName(buildResult: BuildResult): JobStatusName {
        return when (buildResult) {
            BuildResult.FAILURE -> JobStatusName.FAILED
            BuildResult.UNSTABLE -> JobStatusName.FAILED
            BuildResult.REBUILDING -> JobStatusName.RETRYING
            BuildResult.BUILDING -> JobStatusName.IN_PROGRESS
            BuildResult.ABORTED -> JobStatusName.FAILED
            BuildResult.SUCCESS -> JobStatusName.SUCCESSFUL
            BuildResult.UNKNOWN -> JobStatusName.FAILED
            BuildResult.NOT_BUILT -> JobStatusName.PENDING
            BuildResult.CANCELLED -> JobStatusName.FAILED
        }
    }
}