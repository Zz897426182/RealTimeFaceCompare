#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    add-offlinejob-crontab.sh
## Description: add offLineAlarmJob to crontab(将离线告警任务添加到定时器)
## Version:     1.0
## Author:      liushanbin
## Created:     2017-09-09
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
BIN_DIR=$DEPLOY_DIR/bin    ### 执行文件目录
IS_EXIT_CRONTAB=`rpm -qa|grep cron`
STR="crontabs"
STATUS=`service crond status`
RUNNING_STR="running"


##判断是否安装crontab
if [[ $IS_EXIT_CRONTAB != *$STR* ]]
then
   echo "Corntab does not exit,please go to install crontab"
   exit 0
fi

##判断crontab是否开启
if [[ $STATUS != *$RUNNING_STR* ]]
then
   service crond start
fi

##判断是否存在执行文件
if [ ! -e $BIN_DIR/start-face-offline-alarm-job.sh ];then
    echo "$BIN_DIR/start-face-offline-alarm-job.sh does not exit!"
    exit 0
fi

##将离线告警任务添加定时器crontab(任务每天凌晨执行一次)
echo "0 0 * * * ${BIN_DIR}/start-face-offline-alarm-job.sh" >> /var/spool/cron/root

echo "Successfully added offLineAlarmJob to crontab!!!"

