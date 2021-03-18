package com.meggot.releasy.model.dto.releasy.jobs

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.meggot.releasy.model.dto.releasy.ReleaseStatusName
import java.util.*
import kotlin.collections.HashMap

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "jobName")
@JsonSubTypes(
        JsonSubTypes.Type(value = DeployJenkinsJob::class, name = "Deploy"),
        JsonSubTypes.Type(value = PlatformIntegrationJob::class, name = "Platform-integration-test"),
        JsonSubTypes.Type(value = ReleaseJenkinsJob::class, name = "release/release-service"),
        JsonSubTypes.Type(value = PromoteJob::class, name = "release/Promote Service to Prod"),
        JsonSubTypes.Type(value = StackWatcherJob::class, name = "Stack Watcher"),
        JsonSubTypes.Type(value = V2AcceptanceTestsJob::class, name = "V2 Acceptance Tests")
)
abstract class JenkinsJob(@JsonIgnore var jobName: String, @JsonIgnore var targetStatus: ReleaseStatusName, @JsonIgnore var failureStatus: ReleaseStatusName, var id: String = UUID.randomUUID().toString()) {

    @JsonIgnore
    abstract fun getParameters(): HashMap<String, String>

    @JsonIgnore
    abstract fun getSlackLine(): String
}