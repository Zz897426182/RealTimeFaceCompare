#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    upDataToFtp.sh
## Description: to Up Data To Ftp
## Version:     1.0
## Author:      mashencai
## Created:     2018-03-05
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录
SERVER_NAME=`sed '/ftpserver.application.name/!d;s/.*=//' conf/cluster-over-ftp.properties | tr -d '\r'` #获取服务名称
SERVER_PORT=`sed '/listener-port/!d;s/.*=//' conf/cluster-over-ftp.properties | tr -d '\r'` #获取服务端口号

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r CLUSTER_DIR=${BIGDATA_SERVICE_DIR}/cluster

cd -
COMMON_JARS=`ls ${COMMMON_DIR}/lib | grep .jar | awk '{print "'${COMMMON_DIR}/lib'/"$0}'|tr "\n" ":"`

if [ -z "$SERVER_NAME" ]; then
    SERVER_NAME=`hostname`
fi


if [ -n "$SERVER_PORT" ]; then
    SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
    if [ $SERVER_PORT_COUNT -gt 0 ]; then
        echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
        exit 1
    fi
fi

LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`    ## jar 包位置以及第三方依赖jar包，绝对路径
LIB_JARS=${LIB_JARS}${COMMON_JARS}
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/upDataToFtp.log        ##  log 日记文件

#####################################################################
# 函数名: upDataToFtp
# 描述: 把consumer 消费组启动起来
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function upDataToFtp()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    echo "up data to ftp start..."
    nohup java -server -Xms2g -Xmx4g  -XX:PermSize=512m -XX:MaxPermSize=512m  -classpath $CONF_DIR:$LIB_JARS com.hzgc.collect.UpDataToFtp > ${LOG_FILE} 2>&1 &
}

#####################################################################
# 函数名: main
# 描述: 脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    upDataToFtp
}

## 脚本主要业务入口
main
