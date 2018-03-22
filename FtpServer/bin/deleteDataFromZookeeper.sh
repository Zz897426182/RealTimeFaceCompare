#!/bin/bash
#############################################################################
## Copyright:      HZGOSUN Tech. Co, BigData
## Filename:       deleteDataFromZookeeper.sh
## Description:    删除Zookeeper中保存的订阅及演示功能的数据
## Version:        1.0
## Author:         liusiyang
## Created:        2018-03-16
#############################################################################
#set -x  ##用于调试，不用时可以注释

#--------------------------------------------------------------------#
#                              定义变量                              #
#--------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                ### bin目录
cd ..
FTP_DIR=`pwd`
CONF_DIR=$FTP_DIR/conf       ##service根目录
LIB_DIR=$FTP_DIR/lib         ##Jar包目录
LIB_JARS=`ls $LIB_DIR|grep .jar | grep -v avro-ipc-1.7.7-tests.jar \
| grep -v avro-ipc-1.7.7.jar | grep -v spark-network-common_2.10-1.5.1.jar | \
awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`      ## jar包位置以及第三方依赖jar包，绝对路径
cd ../common
COMMON_DIR=`pwd`
COMMON_LIB_DIR=$COMMON_DIR/lib
COMMON_JARS=`ls $COMMON_LIB_DIR | grep .jar | awk '{print "'${COMMON_LIB_DIR}'/"$0}'|tr "\n" ":"`
LOG_DIR=${FTP_DIR}/logs                         ##log日记目录
LOG_FILE=${LOG_DIR}/deleteDataFromZookeeper.log

##########################################################################
# 函数名：delete_data
# 描  述：删除Zookeeper中保存的订阅及演示功能的数据（删除ZK中“/mq_subscribe”与“mq_show”节点数据）
# 参  数：N/A
# 返回值：N/A
# 其  他：N/A
##########################################################################
function delete_data()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi

    java -classpath $CONF_DIR:$LIB_JARS:$COMMON_JARS com.hzgc.collect.expand.subscribe.DeleteDataFromZookeeper | tee -a  ${LOG_FILE}
}
#########################################################################
# 函数名：main
# 描  述：脚本主要入口
# 参  数：N/A
# 返回值：N/A
# 其  他：N/A
#########################################################################
function main()
{
    delete_data
}

## 脚本的主要入口
main
