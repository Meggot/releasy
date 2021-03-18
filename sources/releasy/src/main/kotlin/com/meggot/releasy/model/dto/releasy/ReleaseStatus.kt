package com.meggot.releasy.model.dto.releasy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.meggot.releasy.model.dto.jenkins.JenkinsJobStatus

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ReleaseStatus(
        var status: ReleaseStatusName,
        var jiraReleaseLink: String,
        var jiraReleaseId: String,
        var releaseTemplate: ReleaseTemplate,
        var confluenceLink: String,
        var confluenceReleasePageId: String,
        var jenkinsJobStatuses: List<JenkinsJobStatus> = ArrayList()
)