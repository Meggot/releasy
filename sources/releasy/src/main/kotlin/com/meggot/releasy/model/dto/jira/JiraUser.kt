package com.meggot.releasy.model.dto.jira

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder
import javax.annotation.Nullable

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Nullable
@JsonInclude(JsonInclude.Include.NON_NULL)
class JiraUser(
        val accountId: String,
        val emailAddress: String
)