#!/usr/bin/env bash

echo "> Running 'docker-pre-build.sh'"

set -e

# Source the bash.utils
source "${APP_BASE_DIR}/bin/bash.utils"

log.info "> Starting ${APP_NAME} build..."

log.info ">> Installing ${APP_NAME} dependencies..."

# install epel repo
yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm

yum install -y \
  build-essential \
  ca-certificates \
  git \
  pigz \
  pkgconfig \
  python3 \
  rsync \
  unzip \
  wget \
  which 

log.info ">> Creating application user..."
useradd -u 5001 -U -c "Text2phenotype App User" -m -d /app -s /bin/bash mdluser

### Python Sanity Check
echo "> Using Python..."
which python3
python3 --version

echo "> Upgrading PIP..."
python3 -m pip install -U pip

echo "> Using PIP version:"
pip3 --version
### Python Sanity Check

pip3 install --no-cache-dir -r "${APP_BASE_DIR}/build-resources/config/python-requirements.txt"

log.info ">> Installing maven from apache..."
wget --no-verbose https://www.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -P /tmp
tar xf /tmp/apache-maven-*.tar.gz -C /opt
ln -s /opt/apache-maven-3.6.3 /opt/maven

log.info ">> ${APP_NAME} dependency installation complete."
