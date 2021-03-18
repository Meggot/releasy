package com.meggot.releasy.model.dto.jenkins

import com.fasterxml.jackson.annotation.JsonIgnore
import com.meggot.releasy.model.dto.releasy.jobs.JenkinsJob

class JenkinsJobStatus(var job: JenkinsJob,
                       var status: JobStatusName = JobStatusName.PENDING,
                       @JsonIgnore var success: Boolean = true,
                       @JsonIgnore var buildLink: String? = "") {
    @JsonIgnore
    fun getJobSlackLine(): String {
        var outputLine = ""
        outputLine += status.slackIconDisplay
        outputLine += if (buildLink.equals("")) job.getSlackLine() else " <$buildLink|" + job.getSlackLine() + ">"
        return outputLine
    }
}
