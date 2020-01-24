#!/usr/bin/env groovy

String tarquinBranch = "develop"
if(env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME=="master") {
    tarquinBranch = env.BRANCH_NAME;
}

library "tarquin@$tarquinBranch"

pipelineAlmStandardBuild {
	productionProfiles = "docker,helm"
}