#!/usr/bin/env bash
if [ -f $TRAVIS_BUILD_DIR/target/sovietBot-master.jar ]
then
    echo Build File Exists
else
    echo Build File Missing!
    exit
fi

echo Uploading Jar
sftp -o "IdentityFile=deploy_rsa" root@$SERVER << !
    rm /root/sovietBot/sovietBot-update.jar
    put $TRAVIS_BUILD_DIR/target/sovietBot-master.jar /root/sovietBot/sovietBot-update.jar
    bye
!
echo Sending Update POST

curl -X POST --data "{\"command\": \"restart\",\"secret\": \"$SECRET\",\"name\": \"`md5sum target/sovietBot-master.jar | awk '{ print $1 }'`\"}" --header "Content-Type:application/json" https://www.sovietbot.xyz:2053/command

echo ""