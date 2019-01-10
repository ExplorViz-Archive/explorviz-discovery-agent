#!/bin/bash
echo "Building Docker image"

DIRECTORY="$(basename "$PWD")"
../gradlew assemble

echo $DOCKER_PW | docker login -u $DOCKER_LOGIN --password-stdin

if [[ "$TRAVIS_BRANCH" == 'dev-1' ]]; then
  docker build -t explorviz/explorviz-discovery-agent:dev .
  docker push explorviz/explorviz-discovery-agent:dev
elif [[ "$TRAVIS_BRANCH" == 'master' ]]; then
  docker build -t explorviz/explorviz-discovery-agent:latest .
  docker push explorviz/explorviz-discovery-agent:latest
else
  echo "Unknown branch for Docker image."
fi
