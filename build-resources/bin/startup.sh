#!/usr/bin/env bash

echo "> Running 'startup.sh'"

# Source the bash.utils
source "${APP_BASE_DIR}/bin/bash.utils"

# Set some variables for APM
log.info "> Configuring ${APP_NAME}..."

if [[ ${APM_ENABLED,,} == "true" ]]; then
  log.info ">> Enabling APM..."

  export APM_SERVICE_NAME="${APM_SERVICE_NAME:-Text2phenotype - CTAKES}"
  export APM_SERVER_URL="${APM_SERVER_URL:-http://localhost:8200}"

  export CATALINA_OPTS="$CATALINA_OPTS -javaagent:${APM_AGENT_JAR}"
  export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.service_name='${APM_SERVICE_NAME}'"
  export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.server_urls=${APM_SERVER_URL}"

  # Downcase the APM_APPLICATION_PACKAGES variable for consistent usage in case statement
  _APM_APPLICATION_PACKAGES=$( tr '[:upper:]' '[:lower:]' <<<"${APM_APPLICATION_PACKAGES}" )
  log.info ">> APM_APPLICATION_PACKAGES == '${APM_APPLICATION_PACKAGES}'"

  case "${_APM_APPLICATION_PACKAGES}" in
    none|unset)
      log.info ">> Skip setting elastic.apm.application_packages (defaults to 'None')."
    ;;
    *)
      export APM_APPLICATION_PACKAGES="${APM_APPLICATION_PACKAGES:-org.apache.ctakes,com.text2phenotype,com.text2phenotype.ctakes}"
      export CATALINA_OPTS="${CATALINA_OPTS} -Delastic.apm.application_packages=${APM_APPLICATION_PACKAGES}"
    ;;
  esac
fi

log.info ">> CATALINA_OPTS='$CATALINA_OPTS'"

log.info "> Starting ${APP_NAME}..."

${CATALINA_HOME}/bin/catalina.sh run

log.warn "> ${APP_NAME} stopped!"
