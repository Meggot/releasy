package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps.newHashMap
import com.meggot.releasy.model.dto.releasy.Environment
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName

@JsonTypeName("V2 Acceptance Tests")
class V2AcceptanceTestsJob(val environment: Environment, val servicesChanged: Set<String>, val skipDownStream: Boolean = true) : JenkinsJob("V2 Acceptance Tests", ReleaseStatusName.RELEASED, ReleaseStatusName.FAILED_V2_ACCEPTANCE_TESTS) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = newHashMap()
        parameterMap["ENVIRONMENT"] = environment.prettyName
        parameterMap["SKIP_DOWNSTREAM"] = skipDownStream.toString()
        parameterMap["DEPLOYING_SERVICES"] = servicesChanged.joinToString("\n").toLowerCase()
        return parameterMap

    }

    override fun getSlackLine(): String {
        return ":thermometer: V2 Acceptance Tests on $environment"
    }
}
