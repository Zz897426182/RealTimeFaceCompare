#!/bin/bash
########################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    schema-merge-parquet-file.sh
## Description: 将定时任务生成job文件并打包成zip包
## Author:      chenke
## Created:     2018-03-27
#########################################################################
#set -x ##用于调试使用，不用的时候可以注释掉

#----------------------------------------------------------------------#
#                              定义变量                                #
#----------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`      ###cluster模块bin目录
cd ..
DEPLOY_DIR=`pwd`   ###cluster模块
cd ..
PROJECT_DIR=`pwd`  ###项目根目录
SERVICE_DIR=$PROJECT_DIR/service  ###项目service模块
SERIVICEBIN_DIR=$SERVICE_DIR/bin  ###service模块bin目录

CONF_DIR=$DEPLOY_DIR/logs  ###集群log日志目录
LOG_FILE=${LOG_DIR}/create-schedule-job-to-zip.log  ##log日志文件
SCHEMA_FILE="schema-merge-parquet-file.sh"
OFFLINE_FILE="start-face-offline-alarm-job.sh"
KEEP_FACECONSUMER="keep-faceconsumer-running.sh"
DYNAMICSHOW_TABLE="get-dynamicshow-table-run.sh"

cd ${BIN_DIR}  ##进入cluster的bin目录
mkdir -p midTableAndPersonTableNow
if [ ! -f "$SCHEMA_FILE" ]; then
   echo "The schema-merge-parquet-file.sh is not exist!!!"
else
   touch midtable.job     ##创建midtable.job文件
   echo "type=command" >> midtable.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> midtable.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh mid_table" >> midtable.job

   touch person_table_now.job  ##创建person_table_now.job文件
   echo "type=command" >> person_table_now.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> person_table_now.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh person_table now" >> person_table_now.job
   echo "dependencies=midtable" >> person_table_now.job

   touch person_table_before.job  ##创建person_table_before.job文件
   echo "type=command" >> person_table_before.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> person_table_before.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh person_table before" >> person_table_before.job

fi
if [ ! -f "$OFFLINE_FILE" ]; then
   echo "The start-face-offline-alarm-job.sh is not exist!!!"
else
   touch start-face-offline-alarm-job.job  ##创建离线告警的job文件
   echo "type=command" >> start-face-offline-alarm-job.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> start-face-offline-alarm-job.job
   echo "command=sh \${cluster_home}/start-face-offline-alarm-job.sh" >> start-face-offline-alarm-job.job
fi

if [ ! -f "$KEEP_FACECONSUMER" ]; then
   echo "The keep-faceconsumer-running.sh is not exist!!!"
else
   touch keep-faceconsumer-running.job  ##创建保证Spark任务Faceconsumer不离线的job文件
   echo "type=command" >> keep-faceconsumer-running.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> keep-faceconsumer-running.job
   echo "command=sh \${cluster_home}/keep-faceconsumer-running.sh" >> keep-faceconsumer-running.job
fi

mv midtable.job person_table_now.job midTableAndPersonTableNow

### zip midTableAndPersonTableNow.zip midTableAndPersonTableNow
zip person_table_before_oneday.job.zip person_table_before.job
zip start-face-offline-alarm-job_oneday.job.zip start-face-offline-alarm-job.job
zip keep-faceconsumer-running_fiveminute.job.zip keep-faceconsumer-running.job

mv person_table_before_oneday.job.zip start-face-offline-alarm-job_oneday.job.zip keep-faceconsumer-running_fiveminute.job.zip midTableAndPersonTableNow

rm -rf  person_table_before.job start-face-offline-alarm-job.job get-dynamicshow-table-run.job keep-faceconsumer-running.job

cd ${SERIVICEBIN_DIR}  ##进入service的bin目录

if [ ! -f "$DYNAMICSHOW_TABLE" ]; then
   echo "The get-dynamicshow-table-run.sh is not exist!!!"
else
   touch get-dynamicshow-table-run.job  ##创建get-dynamicshow-table-run.job文件
   echo "type=command" >> get-dynamicshow-table-run.job
   echo "service_home=/opt/RealTimeFaceCompare/service/bin" >> get-dynamicshow-table-run.job
   echo "commad=sh \${service_home}/get-dynamicshow-table-run.sh" >> get-dynamicshow-table-run.job
fi

zip get-dynamicshow-table-run_onehour.job.zip get-dynamicshow-table-run.job
mv get-dynamicshow-table-run_onehour.job.zip $BIN_DIR/midTableAndPersonTableNow
rm -rf get-dynamicshow-table-run.job