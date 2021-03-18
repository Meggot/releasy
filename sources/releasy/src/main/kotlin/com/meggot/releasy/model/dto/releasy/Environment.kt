package com.meggot.releasy.model.dto.releasy

enum class Environment(var prettyName: String, var deployedStatusName: ReleaseStatusName, var failedStatusName: ReleaseStatusName) {
    UAT("uat", ReleaseStatusName.DEPLOYED_UAT, ReleaseStatusName.FAILED_DEPLOY_TO_UAT),
    PRODUCTION("production", ReleaseStatusName.DEPLOYED_PRODUCTION, ReleaseStatusName.FAILED_DEPLOY_TO_PRODUCTION),
    SANDBOX("sandbox", ReleaseStatusName.DEPLOYED_SANDBOX, ReleaseStatusName.FAILED_DEPLOY_TO_SANDBOX)
}