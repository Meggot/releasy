package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps.newHashMap
import com.meggot.releasy.model.dto.releasy.Environment
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName

@JsonTypeName("Platform-integration-test")
class PlatformIntegrationJob(val environment: Environment, val servicesChanged: Set<String>, val skipDownStream: Boolean = true) : JenkinsJob("Platform-integration-test", ReleaseStatusName.RELEASED, ReleaseStatusName.FAILED_PLATFORM_INTEGRATION_TESTS) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = newHashMap()
        parameterMap["ENVIRONMENT"] = environment.prettyName
        parameterMap["SKIP_DOWNSTREAM"] = skipDownStream.toString()
        parameterMap["DEPLOYING_SERVICES"] = servicesChanged.joinToString("\n").toLowerCase()
        return parameterMap

    }

    override fun getSlackLine(): String {
        return ":thermometer: Platform Integration Tests on $environment "
    }
}
