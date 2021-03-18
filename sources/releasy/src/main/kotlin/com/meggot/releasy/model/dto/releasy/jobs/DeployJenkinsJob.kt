package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps.newHashMap
import com.meggot.releasy.model.dto.releasy.Environment

@JsonTypeName("Deploy")
class DeployJenkinsJob(val environment: Environment, val releaseName: String, val skipDownStream: Boolean = true) : JenkinsJob("Deploy", environment.deployedStatusName, environment.failedStatusName) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = newHashMap()
        parameterMap["ENVIRONMENT"] = environment.prettyName
        parameterMap["SKIP_DOWNSTREAM"] = skipDownStream.toString()
        parameterMap["RELEASE_NAME"] = releaseName
        return parameterMap
    }

    override fun getSlackLine(): String {
        return ":dizzy: Deploy to $environment"
    }

}