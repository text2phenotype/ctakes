#!/bin/bash

echo "> Running 'local-test-unit.sh'"

cd $CI_PROJECT_DIR
time mvn clean test
