package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ConfluenceChildPage(
        var id: String,
        var title: String,
        var type: String,
        var space: Space?,
        var status: String,
        var ancestors: List<Ancestors>?,
        var body: Body?,
        var version: Version?) {


    class Ancestors(var id: String)

    class Body(var storage: ChildStorage)

    class ChildStorage(var value: String, var representation: String)

    class Space(var key: String)

    class Version(var number: Int)

}