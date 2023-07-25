# Use a reasonable FROM image
FROM tomcat:9-jdk13-openjdk-oracle

# Create a list of build arguments
ARG APP_GIT_SHA
ARG APM_AGENT_VERSION

# Set environment variables
# UNIVERSE_IS_VERBOSE enables log level INFO.
ENV UNIVERSE_IS_VERBOSE=true

### Application metadata
ENV APP_GIT_SHA="${APP_GIT_SHA:-unset}"
ENV APP_NAME="ctakes"
ENV APP_BASE_DIR="/app"
ENV PATH="${APP_BASE_DIR}/bin/:${PATH}"

# Download Ctakes.zip
ENV INCLUDE_RES=true

ENV CTAKES_RESOURCE_ARCHIVE="s3://biomed-data/ctakes/ctakes-res.zip"

# Set some container options
WORKDIR "${APP_BASE_DIR}"
EXPOSE 8080

# Copy the application code.
COPY . "${APP_BASE_DIR}"

# Copy the bash.utils script.
COPY "./build-tools/bin/bash.utils" "${APP_BASE_DIR}/bin/"

# Run the scripts together so they end up as a single layer.
RUN mv ${APP_BASE_DIR}/build-resources/bin/* "${APP_BASE_DIR}/bin/" && \
    "${APP_BASE_DIR}/bin/docker-pre-build.sh" && \
    "${APP_BASE_DIR}/bin/docker-build.sh" && \
    "${APP_BASE_DIR}/bin/docker-post-build.sh"

USER 5001

# dumb-init is used to assist with proper signal handling, without
# it we will not kill the other processes
ENTRYPOINT ["/usr/local/bin/dumb-init","--"]

# This command is what launches the service by default.
CMD ["/bin/bash", "-c", "${APP_BASE_DIR}/bin/startup.sh"]
