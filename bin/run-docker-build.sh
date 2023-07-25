#!/usr/bin/env bash

echo "> Running 'run-docker-build.sh'"

# Need to get ctakes assets before docker build
pwd
echo ">> Installing awscli & downloading ctakes resources to ./ctakes-res.zip..."

pip install awscli
aws s3 cp --no-progress s3://biomed-data/ctakes/ctakes-res.zip ./ctakes-res.zip

if [[ -x build-tools/bin/suite-runner ]]; then
    # export DOCKER_FROM_REPO='base-image'
    export DOCKER_BUILD_OPTIONS='--skip-from'
    build-tools/bin/suite-runner
    exit_code=$?

    if [[ ${exit_code} -gt 0 ]]; then
       exit ${exit_code}
    fi

    # if [[ -x ./build-tools/bin/stage.deploy ]]; then
    #   for branch in master dev; do
    #     for endpoint in nlp; do
    #       ./build-tools/bin/stage.deploy \
    #         --deploy-clusters "dev-ci-eks" \
    #         --deploy-branches "$branch" \
    #         --deploy-name "mdl-ctakes-${endpoint}-ci-${branch}"
    #     done
    #   done
    # else
    #     echo "The script does not exist or did not have execute permissions: /build-tools/bin/stage.deploy"
    #     stat ./build-tools/bin/stage.deploy
    # fi
else
    echo "The script does not exist or did not have execute permissions: build-tools/bin/suite-runner"
    stat build-tools/bin/suite-runner
fi
