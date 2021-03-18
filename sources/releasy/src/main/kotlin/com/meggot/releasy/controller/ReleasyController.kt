package com.meggot.releasy.controller

import com.meggot.releasy.model.dto.releasy.*
import com.meggot.releasy.service.ReleasyService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/release/")
class ReleasyController(private val releasyService: ReleasyService) {
    // {note} SomeClass::class.java = getClass()
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("validate/{projectCode}", produces = ["application/json"])
    fun slackPostRelease(@PathVariable projectCode: String,
                         @RequestBody getReleaseTemplateBody: GetReleaseTemplateBody): ReleaseTemplate {
        log.info("Received a validate project request on Project Code {} by release manager {}", projectCode, getReleaseTemplateBody.releaseManager)
        if (!ProjectCode.values().map(ProjectCode::name).contains(projectCode)) {
            throw ReleasyException("No such project code $projectCode")
        }
        return releasyService.createReleaseTemplate(projectCode, getReleaseTemplateBody)
    }

    @PostMapping
    fun postRelease(@RequestBody releaseTemplate: ReleaseTemplate): ReleaseStatus {
        log.info("Received a post release request using release template: $releaseTemplate")
        return releasyService.postReleaseTemplate(releaseTemplate)
    }


    @PostMapping("deploy")
    fun slackRunRelease(@RequestBody releaseStatus: ReleaseStatus) {
        log.info("Running release deploy {}", releaseStatus.releaseTemplate.releaseTitle)
        releasyService.runJenkinsJobs(releaseStatus)
    }

}
