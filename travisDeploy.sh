#!/usr/bin/env bash
sshpass -e sftp root@$SERVER << !
    rm /root/sovietBot/sovietBot-update.jar
    put target/sovietBot-master.jar /root/sovietBot/sovietBot-update.jar
    bye
!
curl -X POST --data "{\"command\": \"restart\",\"secret\": \"$SECRET\",\"name\": \"`md5sum target/sovietBot-master.jar | awk '{ print $1 }'`\"}" --header "Content-Type:application/json" https://www.sovietbot.xyz:2053/command
echo ""