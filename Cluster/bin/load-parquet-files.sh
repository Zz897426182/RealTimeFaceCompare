#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    load-parquet-files.sh
## Description: 加载分区数据到peroson_table
## Version:     1.0
## Author:      qiaokaifeng
## Created:     2017-11-18
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉


cd `dirname $0`
BIN_DIR=`pwd`                                                                ## cluster/bin目录
cd ..
DEPLOY_DIR=`pwd`                                                             ## cluster模块根目录
cd ..
PROJECT_DIR=`pwd`                                                            ## 项目根目录
CONF_DIR=$DEPLOY_DIR/conf                                                    ## cluster模块conf目录
LIB_DIR=$DEPLOY_DIR/lib                                                      ## cluster模块jar目录
COMMON_LIB=$PROJECT_DIR/common/lib                                           ## common模块lib目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar 包位置以及第三方依赖jar包，绝对路径
sleep 2s
COM_LIB_JARS=`ls $COMMON_LIB|grep .jar|awk '{print "'${COMMON_LIB}'/"$0}'|tr "\n" ":"`
LOG_DIR=${DEPLOY_DIR}/logs                                                   ## log 日记目录
LOG_FILE=${LOG_DIR}/load-parquet-files.log                                   ## log 日记文件

if [ ! -d $LOG_DIR ]; then
    mkdir -p $LOG_DIR;
fi


SPARK_STREAMING_KAFKA=1.6.2
RELEASE_VERSION=1.5.0
KAFKA_VERSION=1.0.0
SCALA_VERSION=2.11

hdfsClusterName=$(sed -n '1p' ${CONF_DIR}/load-parquet-files.properties)
hdfsPath=$(sed -n '2p' ${CONF_DIR}/load-parquet-files.properties)
tableName=$(sed -n '3p' ${CONF_DIR}/load-parquet-files.properties)
#####################################################################
# 函数名: start_load_data
# 描述: 加载数据
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function load_parquet()
{
    source /etc/profile
    spark-submit --class com.hzgc.cluster.loaddata.LoadParquetFile \
    --master local[*] \
    --driver-memory 4g \
    --jars ${COMMON_LIB}/service-${RELEASE_VERSION}.jar, \
    ${COMMON_LIB}/cluster-${RELEASE_VERSION}.jar ${hdfsClusterName} ${hdfsPath} ${tableName}
    if [ "$?" -eq "0" ] ; then
        echo "******************** 更新元数据完成 ***************** "
    else
        echo "******************** 更新元数据失败 ***************** "
    fi

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
    echo ""  | tee  -a  $LOG_FILE
    echo ""  | tee  -a  $LOG_FILE
    echo "==================================================="  | tee -a $LOG_FILE
    echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
    load_parquet
}
## 脚本主要业务入口
# 主程序入口
echo "" | tee  -a  $LOG_FILE
echo "*******************************************************************"  | tee  -a  $LOG_FILE
main


set -x