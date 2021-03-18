package com.meggot.releasy.model.dto.jira

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UpdateComponentRequest(
        var update: UpdateBody
) {
    class UpdateBody(var fixVersions: List<FixVersions>)

    class FixVersions(var set: List<FixVersion>) {
        class FixVersion(var name: String)
    }
}