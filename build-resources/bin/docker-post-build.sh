#!/usr/bin/env bash

echo "> Running 'docker-post-build.sh'"

set -e

# Source the bash.utils
source "${APP_BASE_DIR}/bin/bash.utils"

log.info ">> Cleaning up ${APP_NAME} build..."

# setup some variables for maven
export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=${M2_HOME}/bin:${PATH}
log.info ">> mvn clean"
mvn clean

log.info ">>> yum purge build tools"

yum remove -y \
  build-essential \
  git \
  pigz \
  rsync \
  unzip

log.info ">>> yum clean"
yum clean all -y

log.info ">> remove maven"
rm -rf "/opt/apache-maven*"
rm -rf "/opt/maven*"
rm -rf /root/.m2

log.info ">> /tmp cache cleanup"
rm -rf "/tmp/*"
rm -rf "/tmp/*.*"

log.info ">> Removing build / source folders..."

for remove_folder in ".git" "src" "target";
do
  log.info ">>> Removing ${APP_BASE_DIR}/${remove_folder}"
  rm -rf "${APP_BASE_DIR}/${remove_folder}"
done

log.info ">> Removing build / source folders complete."

log.info ">> ${APP_NAME} build cleanup complete."

log.info "> ${APP_NAME} build complete."
