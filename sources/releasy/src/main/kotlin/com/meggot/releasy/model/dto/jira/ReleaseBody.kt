package com.meggot.releasy.model.dto.jira

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.meggot.releasy.model.dto.releasy.ProjectCode
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ReleaseBody(
        var description: String,
        var name: String,
        var project: ProjectCode
)