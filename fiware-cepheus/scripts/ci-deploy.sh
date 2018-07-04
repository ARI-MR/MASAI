#!/bin/bash
# This script is triggered by Travis-CI when a new commit is added to master branch

set -e
set -x

# Deploy JAR to Sonatype Mave Repository
mvn deploy -q -settings=settings.xml -DskipTests=true

# Trigger Docker Automated build
curl -s -H "Content-Type: application/json" --data "build=true" -X POST "https://registry.hub.docker.com/u/$DOCKER_USER/fiware-cepheus/trigger/$DOCKER_KEY/"
