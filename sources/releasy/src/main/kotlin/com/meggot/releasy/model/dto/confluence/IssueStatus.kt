package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class IssueStatus(
        val self: String,
        val description: String,
        val iconUrl: String,
        val name: String,
        val id: String) {
}