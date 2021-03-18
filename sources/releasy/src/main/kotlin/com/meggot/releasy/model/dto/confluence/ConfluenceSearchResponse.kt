package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ConfluenceSearchResponse(

        var expand: String?,

        var startAt: Int?,

        var maxResults: Int?,

        var total: Long?,

        var issues: List<IssueDto>?

)
