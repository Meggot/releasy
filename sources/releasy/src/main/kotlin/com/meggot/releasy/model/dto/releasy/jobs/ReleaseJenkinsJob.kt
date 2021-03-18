package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps.newHashMap
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName

@JsonTypeName("release/release-service")
class ReleaseJenkinsJob(var service: String) : JenkinsJob("release/release-service", ReleaseStatusName.RELEASED, ReleaseStatusName.FAILED_RELEASE) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = newHashMap()
        parameterMap["SERVICE"] = service
        return parameterMap
    }

    override fun getSlackLine(): String {
        return ":building_construction: Release service $service"
    }

}