#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    schema-merge-parquet-file.sh
## Description: 定时启动合并小文件的脚本
## Version:     1.0-->1.0.1
## Author:      lidiliang
## Created:     2017-11-06
## Modified:    2017-12-01(lidiliang)
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
source /etc/profile
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录                      ## log 日记目录
LOG_DIR=$DEPLOY_DIR/logs
LOG_FILE=${LOG_DIR}/schema-merge-parquet-file.log        ##  log 日记文件

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r CLUSTER_DIR=${BIGDATA_SERVICE_DIR}/cluster

RELEASE_VERSION=1.5.0

hdfsClusterName=$(sed -n '1p' ${CONF_DIR}/merget-parquet-files.properties)
tmpTableHdfsPath=$(sed -n '2p' ${CONF_DIR}/merget-parquet-files.properties)
hisTableHdfsPath=$(sed -n '3p' ${CONF_DIR}/merget-parquet-files.properties)
tableName=$(sed -n '4p' ${CONF_DIR}/merget-parquet-files.properties)

BIGDATA_ENV_FILE=/opt/hzgc/env_bigdata.sh
source ${BIGDATA_ENV_FILE}
mkdir -p ${LOG_DIR}

echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE

#####################################################################
# 函数名: merge_parquet
# 描述: 合并动态库person_table 表中的零散文件
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function merge_parquet()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    nohup spark-submit --class com.hzgc.cluster.smallfile.MergeParquetFileV1 \
    --master local[*] \
    --driver-memory 4g \
${COMMMON_DIR}/lib/cluster-${RELEASE_VERSION}.jar ${hdfsClusterName} ${tmpTableHdfsPath} ${hisTableHdfsPath} ${tableName} >> ${LOG_FILE} 2>&1 &
}

merge_parquet

