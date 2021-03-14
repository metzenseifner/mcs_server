#!/bin/sh

echo "==> Running as $(id -u):$(id -g)"
stat /var/lib/mcs
/setpermissions $(id -u) $(id -g) /usr/share/mcs
/setpermissions $(id -u) $(id -g) /var/lib/mcs
/setpermissions $(id -u) $(id -g) /var/log/mcs
/setpermissions $(id -u) $(id -g) /srv/mcs
/usr/share/mcs/bin/start-mcs server