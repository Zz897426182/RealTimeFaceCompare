#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    put-data-to-hbase.sh
## Description: to put data to hbase
## Version:     1.0
## Author:      chenke
## Created:     2017-11-28
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录
LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar| grep -v avro-ipc-1.7.7-tests.jar | grep -v avro-ipc-1.7.7.jar \
| grep -v spark-network-common_2.10-1.5.1.jar \
 |awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar包位置以及第三方依赖jar包，绝对路径
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
if [ $# != 3 ]; then
    echo "sh ***.sh photoPath jsonFile ***.log";
    exit 0;
fi
PHOTO_PATH=$1
LOG_FILE_NAME=$3
JSON_DIR=${DEPLOY_DIR}/json
if [ ! -d $JSON_DIR ]; then
        mkdir $JSON_DIR;
fi
JSON_FILE=$JSON_DIR/$2

LOG_FILE=${LOG_DIR}/${LOG_FILE_NAME}      ##  log 日记文件


#####################################################################
# 函数名: start_consumer
# 描述: 把consumer 消费组启动起来
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_consumer()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    java -classpath $CONF_DIR:$LIB_JARS com.hzgc.service.Putdata.PutDataToHBase  $PHOTO_PATH  $JSON_FILE | tee -a  ${LOG_FILE}
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
    start_consumer
}

## 脚本主要业务入口
main