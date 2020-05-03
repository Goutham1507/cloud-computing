#!/usr/bin/env bash
cd /home/ubuntu
sudo systemctl enable amazon-cloudwatch-agent
sudo systemctl start amazon-cloudwatch-agent
cd /home/ubuntu/app
source /etc/profile.d/envars.sh
printenv
sudo rm -rf /home/ubuntu/applogs/*.log
nohup java -jar -Dspring.profiles.active=$springprofilesactive -Ddb.url=$dburl -Ddb.username=$springdatasourceusername -Ddb.password=$springdatasourcepassword -Dbucket.name=$bucketname clouddemo-0.0.1-SNAPSHOT.war 1> /home/ubuntu/applogs/webapp.out 2>&1 </dev/null &
echo "Application succesfully created"



