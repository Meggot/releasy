package com.meggot.releasy.model.dto.confluence

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lombok.Builder

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class IssueFields(
        var summary: String,
        var status: IssueStatus,
        var components: List<ComponentDto>?,
        var customfield_11700: String? //This is the open PR fields on each issue.
        /** Comes back like this.
         * {pullrequest={dataType=pullrequest, state=MERGED, stateCount=1}, json={\"cachedValue\":{\"errors\":[],\"summary\":{\"pullrequest\":{\"overall\":{\"count\":1,\"lastUpdated\":\"2020-02-20T10:51:16.000+0000\",\"stateCount\":1,\"state\":\"MERGED\",\"dataType\":\"pullrequest\",\"open\":false},\"byInstanceType\":{\"GitHub\":{\"count\":1,\"name\":\"GitHub\"}}}}},\"isStale\":true}}
         */

) {

    @JsonIgnore
    fun getPRJson(): String {
        return this.customfield_11700!!.split("json=")[1]
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PrInfo(var cachedValue: CachedValue)

    @JsonIgnoreProperties(ignoreUnknown = true)
    class CachedValue(var summary: Summary)

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Summary(var pullrequest: PullRequest)

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PullRequest(var overall: Overall)

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Overall(var count: Int, var lastUpdated: String, var stateCount: Int, var state: String, var dataType: String, var open: Boolean)
}