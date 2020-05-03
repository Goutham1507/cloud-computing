#!/usr/bin/env bash
echo "started the next deployment"
PID=`ps -C java -o pid=`
kill -9 $PID
echo "Stopped the  application"
mkdir -p /home/ubuntu/app
sudo rm -rf /home/ubuntu/app/
cd /opt
sudo mkdir cloudwatch
sudo rm -rf /opt/cloudwatch/cloudwatch-config.json