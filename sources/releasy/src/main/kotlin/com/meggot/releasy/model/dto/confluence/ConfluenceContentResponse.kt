package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ConfluenceContentResponse(

        var start: Int,
        var limit: Int,
        var size: Int,
        var results: List<ConfluenceChildPage>?,
        var _links: ConfluenceLinks

) {
    class ConfluenceLinks(
            var base: String,
            var context: String,
            var next: String?,
            var self: String
    )
}
