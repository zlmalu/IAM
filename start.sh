#!/bin/sh
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-server-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-gateway-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx1024m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/api-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-console-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-portal-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-sso-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-auth-5.0.0.jar &
sleep 3
nohup java -jar -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Dspring.profiles.filedir=/app/iam-apps40 /app/iam-apps40/iam-jop-5.0.0.jar &
sleep 3