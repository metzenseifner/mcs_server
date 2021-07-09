#!/bin/sh
IMG=URL:VERSION
docker rm -f mcs-test
docker run -d --name mcs-test --user $(id -u dkrmcs):$(id -g dkrmcs) -e "TZ=Europe/Zurich" $IMG
docker exec -it mcs-test bash