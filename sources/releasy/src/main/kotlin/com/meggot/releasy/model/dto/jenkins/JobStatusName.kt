package com.meggot.releasy.model.dto.jenkins

enum class JobStatusName(var prettyName: String, var slackIconDisplay: String, var confluenceStatusDisplay: String) {

    PENDING("Pending", ":spacer:", ""),
    IN_PROGRESS("In Progress", ":still_waiting:", "<ac:emoticon ac:name=\"information\"> </ac:emoticon>"),
    SUCCESSFUL("Successful", ":white_check_mark:", "<ac:emoticon ac:name=\"tick\"> </ac:emoticon>"),
    SKIPPED("Skipped", ":fast_forward:", "<ac:emoticon ac:name=\"warning\"> </ac:emoticon>"),
    RETRYING("Retrying", ":still_waiting:", "<ac:emoticon ac:name=\"warning\"> </ac:emoticon>"),
    FAILED("Failed", ":x:", "<ac:emoticon ac:name=\"cross\"> </ac:emoticon>")
}