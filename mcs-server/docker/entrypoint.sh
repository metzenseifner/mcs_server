#!/bin/sh

/setpermissions /usr/share/mcs $(id -u) $(id -g)
/usr/share/mcs/bin/start-mcs server