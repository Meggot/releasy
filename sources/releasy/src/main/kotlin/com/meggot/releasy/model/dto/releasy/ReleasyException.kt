package com.meggot.releasy.model.dto.releasy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.google.common.collect.Lists
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ReleasyException(message: String,
                       var details: List<ErrorDetail>? = Lists.newArrayList()) : Exception(message) {
    companion object {
        fun body(ex: ReleasyException): Any {
            return object {
                var message = ex.message
                var body = ex.details
            }
        }
    }
}

