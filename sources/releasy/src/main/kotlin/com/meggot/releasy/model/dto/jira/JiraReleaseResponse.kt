package com.meggot.releasy.model.dto.jira

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class JiraReleaseResponse(
        var id: String,
        var description: String,
        var name: String,
        var released: Boolean

)