package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ConfluenceJqlRequest(

        var jql: String,

        var fieldsByKeys: String,

        var fields: List<String>,

        var startAt: Long

)
