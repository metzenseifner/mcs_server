#!/bin/sh
IMG=URL:VERSION
docker pull $IMG
docker service create \
  --mode global \
  --user $(id -u dkrmcs):$(id -g dkrmcs) \
  --name mcs \
  --hostname mcs \
  --network 'host' \
  --publish 'mode=host,target=1099,published=1099' \
  --publish 'mode=host,target=8101,published=8101' \
  --publish 'mode=host,target=8181,published=8181' \
  --publish 'mode=host,target=44444,published=44444' \
  --publish 'mode=host,target=5005,published=5005' \
  --env "TZ=Europe/Zurich" \
  --mount type=bind,source=/dkr/containers.d/mcs/log,target=/var/lib/mcs/log \
  --mount type=bind,source=/dkr/containers.d/mcs/etc,target=/usr/share/mcs/etc \
  $IMG