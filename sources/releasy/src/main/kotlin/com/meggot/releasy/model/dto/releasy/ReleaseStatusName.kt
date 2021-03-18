package com.meggot.releasy.model.dto.releasy

enum class ReleaseStatusName(var prettyName: String, var releaseStage: Int) {

    DRAFT("Draft", 0),
    QUEUED("Queued", 1),
    NOTES_CREATED("Notes Created", 2),
    RUNNING_JENKINS_WORKFLOW("Running Jenkins Workflow", 3),
    RELEASED("Released service successfully", 3),
    FAILED_RELEASE("Cannot release service", 3),
    FAILED_V2_ACCEPTANCE_TESTS("Failed v2 acceptance tests", 3),
    FAILED_PLATFORM_INTEGRATION_TESTS("Failed platform integration tests", 3),
    DEPLOYED_UAT("Deployed to UAT", 4),
    PROMOTED_SERVICE("Promoted service", 4),
    FAILED_PROMOTE("Failed to promote service", 4),
    FAILED_DEPLOY_TO_UAT("Failed to deploy to UAT", 4),
    FAILED_DEPLOY_TO_SANDBOX("Failed to deploy to sandbox", 4),
    DEPLOYED_SANDBOX("Deployed to Sandbox", 5),
    FAILED_DEPLOY_TO_PRODUCTION("Failed to deploy to Production", 5),
    DEPLOYED_PRODUCTION("Deployed to Production", 6)

}