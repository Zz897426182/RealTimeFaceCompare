#!/bin/bash
#############################################################################
## Copyright:      HZGOSUN Tech. Co, BigData
## Filename:       get-dynamicshow-table-run.sh
## Description:    将动态库的数据统计插入到dynamicshow表中
## Author:         chenke
## Created:        2018-02-01
#############################################################################
#set -x  ##用于调试，不用时可以注释

#--------------------------------------------------------------------#
#                              定义变量                              #
#--------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
SERVICE_DIR=`pwd`
CONF_DIR=$SERVICE_DIR/conf       ##service根目录
LIB_DIR=$SERVICE_DIR/lib         ##Jar包目录
LIB_JARS=`ls $LIB_DIR|grep .jar | grep -v avro-ipc-1.7.7-tests.jar \
| grep -v avro-ipc-1.7.7.jar | grep -v spark-network-common_2.10-1.5.1.jar | \
awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar包位置以及第三方依赖jar包，绝对路径
cd ../common
COMMON_DIR=`pwd`
COMMON_LIB_DIR=$COMMON_DIR/lib
COMMON_JARS=`ls $COMMON_LIB_DIR | grep .jar | awk '{print "'${COMMON_LIB_DIR}'/"$0}'|tr "\n" ":"`
LOG_DIR=${SERVICE_DIR}/logs                  ##log日记目录
LOG_FILE=${LOG_DIR}/dynamicshow-table.log

##########################################################################
# 函数名：start_consumer
# 描述：把consumer消费组启动起来
# 参数：N/A
# 返回值：N/A
# 其他：N/A
##########################################################################
function start_consumer()
{
    if [ ! -d $LOG_DIR ]; then
            mkdir $LOG_DIR;
    fi

    java -classpath $CONF_DIR:$LIB_JARS:$COMMON_JARS com.hzgc.service.dynamicrepo.CaptureNumberImplTimer | tee -a  ${LOG_FILE}
}
#########################################################################
# 函数名：main
# 描述：脚本主要入口
# 参数：N/A
# 返回值：N/A
# 其他：N/A
#########################################################################
function main()
{
    start_consumer
}

## 脚本的主要入口
main













