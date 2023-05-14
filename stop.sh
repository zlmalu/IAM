#!/bin/sh
ps -ef|grep /app/iam-apps40/|grep -v grep|cut -c 9-15|xargs kill -9
sleep 1
