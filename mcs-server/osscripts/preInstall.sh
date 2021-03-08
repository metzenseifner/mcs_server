#!/bin/sh

username=mcs
uid=555

groupname=$username
gid=$uid
HOMEDIR=/srv/mcs

# Create user and group if nonexistent
# Try using a common numeric uid/gid if possible
if [ ! $(getent group $groupname) ]; then
   if [ ! $(getent group $gid) ]; then
      groupadd -r -g $gid $groupname > /dev/null 2>&1 || :
   else
      groupadd -r $groupname > /dev/null 2>&1 || :
   fi
fi
if [ ! $(getent passwd $username) ]; then
   if [ ! $(getent passwd $uid) ]; then
      useradd -M -r -u $uid -d "$HOMEDIR" -g $groupname $username > /dev/null 2>&1 || :
   else
      useradd -M -r -d "$HOMEDIR" -g $groupname $username > /dev/null 2>&1 || :
   fi
fi