IMG=URL:VERSION
docker rm -f mcs-test
docker run -d --name mcs-test -e "TZ=Europe/Zurich" $IMG
docker exec -it mcs-test bash