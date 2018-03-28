#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-dynamic-table.sh
## Description:  创建动态库表(person_table,mid_table)
## Author:       qiaokaifeng
## Created:      2017-11-28
################################################################################

#set -x
#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#

cd `dirname $0`
## bin目录
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
## 配置文件目录
CONF_DIR=${DEPLOY_DIR}/conf
## Jar 包目录
LIB_DIR=${DEPLOY_DIR}/lib
## log 日记目录
LOG_DIR=${DEPLOY_DIR}/logs
##  log 日记文件
LOG_FILE=${LOG_DIR}/create-dynamic-table.log
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
## bigdata hive path
SPARK_PATH=${BIGDATA_CLUSTER_PATH}/Spark/spark

## 创建person_table
${SPARK_PATH}/bin/spark-sql -e "CREATE EXTERNAL TABLE IF NOT EXISTS default.person_table( \
                                ftpurl        string, \
                                feature       array<float>, \
                                eyeglasses    int, \
                                gender        int, \
                                haircolor     int, \
                                hairstyle     int, \
                                hat           int, \
                                huzi          int, \
                                tie           int, \
                                timeslot      int, \
                                exacttime     Timestamp, \
                                searchtype    string) \
                                partitioned by (date string,ipcid string) \
                                STORED AS PARQUET \
                                LOCATION '/user/hive/warehouse/person_table';
                                CREATE EXTERNAL TABLE IF NOT EXISTS default.mid_table( \
                                ftpurl        string, \
                                feature       array<float>, \
                                eyeglasses    int, \
                                gender        int, \
                                haircolor     int, \
                                hairstyle     int, \
                                hat           int, \
                                huzi          int, \
                                tie           int, \
                                timeslot      int, \
                                exacttime     Timestamp, \
                                searchtype    string, \
                                date          string, \
                                ipcid         string) \
                                STORED AS PARQUET \
                                LOCATION '/user/hive/warehouse/mid_table';
                                show tables"

if [ $? == 0 ];then
		echo "===================================="
		echo "创建person_table,mid_table成功"
		echo "===================================="
else
		echo "========================================================="
		echo "创建person_table,mid_table失败,请看日志查找失败原因......"
		echo "========================================================="
fi
