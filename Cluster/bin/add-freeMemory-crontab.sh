#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    add-freeMemory-crontab.sh
## Description: free memory each host(将清理集群内存添加到定时器)
## Version:     1.5
## Author:      liushanbin
## Created:     2017-11-30
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`   
cd ..
cd ..
DEPLOY_DIR=`pwd`
cd ..
MODEL_DIR=`pwd`
IS_EXIT_CRONTAB=`rpm -qa|grep cron`
STR="crontabs"
STATUS=`service crond status`
RUNNING_STR="running"

##定时执行脚本是否存在判断
if [ ! -e $BIN_DIR/free-memory.sh ];then
    echo "$BIN_DIR/free-memory.sh  does not exit!"
    exit 0
fi

#---------------------------------------------------------------------#
#                           主机添加定时任务                          #
#---------------------------------------------------------------------#

for hostName in "$@"
do
	scp -r ${DEPLOY_DIR} root@${hostName}:${MODEL_DIR}/
	ssh root@${hostName} "echo '0 * * * * ${BIN_DIR}/free-memory.sh' >> /var/spool/cron/root"

done
