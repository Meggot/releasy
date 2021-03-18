package com.meggot.releasy.model.dto.releasy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder
import java.util.*

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class ReleaseTemplate(

        var releaseTitle: String,

        var serviceToTicketCodes: HashMap<String, ArrayList<String>>,

        var releaseManager: List<String>,

        var projectCode: ProjectCode

)
