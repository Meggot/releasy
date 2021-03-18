package com.meggot.releasy.model.dto.releasy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class IssueErrorDetail(
        var key: String,
        var summary: String,
        var message: String
) : ErrorDetail
