package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps.newHashMap
import com.meggot.releasy.model.dto.releasy.Environment
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName

@JsonTypeName("Stack Watcher")
class StackWatcherJob(var environment: Environment, var skipDownStream: Boolean = true) : JenkinsJob("Stack Watcher", ReleaseStatusName.RELEASED, ReleaseStatusName.FAILED_RELEASE) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = newHashMap()
        parameterMap["ENVIRONMENT"] = environment.prettyName
        parameterMap["SKIP_DOWNSTREAM"] = skipDownStream.toString()
        return parameterMap

    }

    override fun getSlackLine(): String {
        return ":eye: Stack Watcher on $environment"
    }
}