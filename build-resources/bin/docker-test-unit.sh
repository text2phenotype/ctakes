#!/usr/bin/env bash

echo "> Running 'docker-test-unit.sh'"

# Source the bash.utils
source "${APP_BASE_DIR}/bin/bash.utils"

log.info "> Running unit tests..."
log.info "> No unit tests. Skipping."
exit_code=0

log.info "> Unit tests complete. Exited ('${exit_code}')"
exit ${exit_code}
