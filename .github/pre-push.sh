#!/bin/sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# To clean Docker artifacts execute: `sudo docker system prune -af`
#

set -e

./gradlew clean installDist -x test

docker-compose -f docker-compose.yml -f hexagon_benchmark/docker-compose.yml rm -sf
docker-compose -f docker-compose.yml -f hexagon_benchmark/docker-compose.yml up -d

./gradlew all
./gradlew dokkaMd checkSite

me="$(whoami)"
user="$(id -u "$me"):$(id -g "$me")"
docker run --rm -v "$PWD/hexagon_site:/docs" -u "$user" "squidfunk/mkdocs-material:4.6.0" build
docker volume prune -f
