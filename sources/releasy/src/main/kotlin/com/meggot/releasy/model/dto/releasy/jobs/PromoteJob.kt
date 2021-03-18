package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.collect.Maps
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName

@JsonTypeName("release/Promote Service to Prod")
class PromoteJob(val servicesChanged: Set<String>, val promoteSandbox: Boolean = true, val promoteProd: Boolean = true) : JenkinsJob("release/Promote Service to Prod", ReleaseStatusName.PROMOTED_SERVICE, ReleaseStatusName.FAILED_PROMOTE) {

    override fun getParameters(): HashMap<String, String> {
        val parameterMap: HashMap<String, String> = Maps.newHashMap()
        parameterMap["SANDBOX"] = promoteSandbox.toString()
        parameterMap["PRODUCTION"] = promoteProd.toString()
        parameterMap["MULTI_SERVICES"] = servicesChanged.joinToString("\n").toLowerCase()
        return parameterMap
    }

    override fun getSlackLine(): String {
        var environments = if (promoteProd) " Production " else ""
        environments += if (promoteSandbox) " Sandbox " else ""
        return ":up: Promoting services to: $environments"
    }
}