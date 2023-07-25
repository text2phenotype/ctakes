#!/usr/bin/env bash

echo "> Running 'docker-build.sh'"

set -e

# Source the bash.utils
source "${APP_BASE_DIR}/bin/bash.utils"

# setup some variables for maven
export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=${M2_HOME}/bin:${PATH}

# NPI build requires a lot of memory
export MAVEN_OPTS="-Xmx8G -Xss128M -XX:+CMSClassUnloadingEnabled"

log.info ">> Downloading dumb-init..."
# Add dumb-init to assist with proper signal handling
curl -Ls https://github.com/Yelp/dumb-init/releases/download/v1.2.0/dumb-init_1.2.0_amd64 -o /usr/local/bin/dumb-init
chmod +x /usr/local/bin/dumb-init

log.info ">> Creating additional folders..."

for new_folder in "webapps/nlp" "shared/classes" "server/classes" "common/classes" "temp" "conf"; do
  log.info ">>> Creating ${CATALINA_HOME}/${new_folder}"
  mkdir -v -p "${CATALINA_HOME}/${new_folder}"
done

log.info ">> Additional folders created."

log.info "> Installing ${APP_NAME}..."

log.info ">> mvn clean package"
mvn --batch-mode clean package -Dlog4j.configuration=file:src/test/resources/TeamCity.log4j.properties

log.info ">> Moving ctakes-rest-service-1.0-SNAPSHOT -> ${CATALINA_HOME}/webapps/nlp"
mv "${APP_BASE_DIR}/target/ctakes-rest-service-1.0-SNAPSHOT"/* "${CATALINA_HOME}/webapps/nlp"

if [[ -n $INCLUDE_RES ]]; then
  log.info ">> AWS CLI downloading ctakes resources..."
  declare ctakes_tmp="${APP_BASE_DIR}/ctakes-res.zip"
  # wget --no-verbose "${CTAKES_RESOURCE_ARCHIVE}" -O "${ctakes_tmp}"

  log.info ">>> Unzipping ctakes resources..."
  unzip "${ctakes_tmp}" -d "${CATALINA_HOME}/webapps/nlp/WEB-INF/classes"
  log.info ">>> Removing ctakes resource archive..."
  rm -fv "${ctakes_tmp}"

  log.info ">> Installed ctakes resources."
fi

if [[ -e "${APP_BASE_DIR}/.docker.metadata" ]]; then
  log.info ">>> Exposing version metadata..."
  mkdir "${CATALINA_HOME}/webapps/ROOT"
  cp "${APP_BASE_DIR}/.docker.metadata" "${CATALINA_HOME}/webapps/ROOT/version"
else
  log.info ">>> Version metadata file not found..."
fi

log.info ">>> Blanking default landing page..."
echo "" > "${CATALINA_HOME}/webapps/ROOT/index.html"

log.info ">>> Chown'ing ${CATALINA_HOME}/webapps ..."
chown -R 5001.5001 "${CATALINA_HOME}/webapps"
log.info ">>> Done Chown'ing ${CATALINA_HOME}/webapps ..."

log.info "> ${APP_NAME} installation complete."
