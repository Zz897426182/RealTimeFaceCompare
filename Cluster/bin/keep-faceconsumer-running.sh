#!/bin/bash
##############################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    keep faceconsumer running
## Description: 保证Spark任务Faceconsumer不离线
## Author:      chenke
## Created:     2018-05-02
#############################################################
set -x  ##用于调式，不用的时候可以注释

#----------------------------------------------------------#
#                         定义变量                         #
#----------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                 ##bin目录：脚本所在目录
cd /opt                       ##进入opt目录
OPT_DIR=`pwd`                 ##opt目录

#----------------------------------------------------------#
#                         定义函数                         #
#----------------------------------------------------------#


function keep_running()
{
     cd ${OPT_DIR}
     touch judgment.txt
     source /opt/hzgc/env_bigdata.sh
     yarn application -list | grep 'FaceObjectConsumer' | grep 'RUNNING' >> judgment.txt

     if [ -s judgment.txt ]; then
      echo "The spark job of faceconsumer is running!"
      rm -rf /opt/judgment.txt
     else
      echo "The spark job of faceconsumer is already down,please restart now!"
       echo "Restart the faceconsumer job now,please wait a minute..................."
       cd ${BIN_DIR}
       sh start-kafka-to-parquet.sh
       rm -rf /opt/judgment.txt
     fi
}


###########################################################
# 函数名：main
# 描述：脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他： N/A
##########################################################
function main()
{
    keep_running
}

main
