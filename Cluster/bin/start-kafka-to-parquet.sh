#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-kafka-to-parquet.sh
## Description: to start kafkaToParquet
## Version:     1.5.0
## Author:      chenke
## Created:     2017-11-09
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
## bin目录
BIN_DIR=`pwd`
cd ..
CLUSTER_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
######## cluster目录 ########
CLUSTER_CONF_DIR=${CLUSTER_DIR}/conf
CLUSTER_LIB_DIR=${CLUSTER_DIR}/lib
CLUSTER_LOG_DIR=${CLUSTER_DIR}/logs
LOG_FILE=${CLUSTER_LOG_DIR}/KafkaToParquet.log
######## common目录 ########
COMMON_CONF_DIR=${DEPLOY_DIR}/common/conf
COMMON_LIB_DIR=${DEPLOY_DIR}/common/lib
######## ftp目录 #########
FTP_CONF_DIR=${DEPLOY_DIR}/ftp/conf
FTP_LIB_DIR=${DEPLOY_DIR}/ftp/lib
######## service目录 ########
SERVICE_CONF_DIR=${DEPLOY_DIR}/service/conf
SERVICE_LIB_DIR=${DEPLOY_DIR}/service/lib
## bigdata_env
BIGDATA_ENV=/opt/hzgc/env_bigdata.sh
## spark class
SPARK_CLASS_PARAM=com.hzgc.cluster.consumer.KafkaToParquet
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
##deploy-mode
DEPLOY_MODE=client
#---------------------------------------------------------------------#
#                              jar版本控制                            #
#---------------------------------------------------------------------#
## module version(模块)
BIGDATA_API_VERSION=bigdata-api-1.5.0.jar
CLUSTER_VERSION=cluster-1.5.0.jar
FTP_VERSION=ftp-1.5.0.jar
JNI_VERSION=jni-1.5.0.jar
SERVICE_VERSION=service-1.5.0.jar
UTIL_VERSION=util-1.5.0.jar

## quote version(引用)
GSON_VERSION=gson-2.8.0.jar
JACKSON_CORE_VERSION=jackson-core-2.8.6.jar
SPARK_STREAMING_KAFKA_VERSION=spark-streaming-kafka-0-8_2.11-2.2.0.jar
HBASE_SERVER_VERSION=hbase-server-1.2.6.jar
HBASE_CLIENT_VERSION=hbase-client-1.2.6.jar
HBASE_COMMON_VERSION=hbase-common-1.2.6.jar
HBASE_PROTOCOL_VERSION=hbase-protocol-1.2.6.jar
KAFKA_VERSION=kafka_2.11-0.8.2.1.jar
ELASTICSEARCH_VERSION=elasticsearch-1.0.jar
ROCKETMQ_CLIENT_VERSION=rocketmq-client-4.2.0.jar
ROCKETMQ_COMMON_VERSION=rocketmq-common-4.2.0.jar
ROCKETMQ_REMOTING_VERSION=rocketmq-remoting-4.2.0.jar
FASTJSON_VERSION=fastjson-1.2.29.jar
KAFKA_CLIENTS_VERSION=kafka-clients-1.0.0.jar
METRICS_CORE_VERSION=metrics-core-2.2.0.jar
ZKCLIENT_VERSION=zkclient-0.3.jar


############ 创建log目录 ###############
if [ ! -d ${CLUSTER_LOG_DIR} ];then
   mkdir ${CLUSTER_LOG_DIR}
fi

############ 判断是否存在大数据集群 ###################
if [ ! -d ${BIGDATA_CLUSTER_PATH} ];then
   echo "${BIGDATA_CLUSTER_PATH} does not exit,please go to the node of the existing bigdata cluster !"
   exit 0
fi

############### 判断是否存在配置文件 ##################
if [ ! -e ${SERVICE_CONF_DIR}/es-config.properties ];then
    echo "${SERVICE_CONF_DIR}/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_CONF_DIR}/sparkJob.properties ];then
    echo "${CLUSTER_CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_CONF_DIR}/log4j.properties ];then
    echo "${CLUSTER_CONF_DIR}/log4j.properties does not exit!"
    exit 0
else
    echo "===================开始配置log4j.properties===================="
    sed -i "s#^log4j.appender.FILE.File=.*#log4j.appender.FILE.File=${LOG_FILE}#g" ${CLUSTER_CONF_DIR}/log4j.properties
fi


################# 判断是否存在jar ###################
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${GSON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${GSON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${SERVICE_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${SERVICE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${JNI_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${JNI_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${KAFKA_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${KAFKA_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} ];then
    echo "${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${FTP_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${FTP_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${FASTJSON_VERSION} ];then
    echo "${FTP_LIB_DIR}/${FASTJSON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${UTIL_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${UTIL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${CLUSTER_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${CLUSTER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} does not exit!"
    exit 0
fi

################## 存放数据至parquet任务 ###################
source /etc/profile
source ${BIGDATA_ENV}
nohup spark-submit \
--master yarn \
--deploy-mode ${DEPLOY_MODE} \
--executor-memory 4g \
--executor-cores 2 \
--num-executors 4 \
--class ${SPARK_CLASS_PARAM} \
--jars ${CLUSTER_LIB_DIR}/${GSON_VERSION},\
${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION},\
${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION},\
${COMMON_LIB_DIR}/${SERVICE_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION},\
${CLUSTER_LIB_DIR}/${ZKCLIENT_VERSION},\
${COMMON_LIB_DIR}/${JNI_VERSION},\
${CLUSTER_LIB_DIR}/${KAFKA_VERSION},\
${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION},\
${COMMON_LIB_DIR}/${FTP_VERSION},\
${COMMON_LIB_DIR}/${BIGDATA_API_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION},\
${FTP_LIB_DIR}/${FASTJSON_VERSION},\
${COMMON_LIB_DIR}/${UTIL_VERSION},\
${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION},\
${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} \
--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:${CLUSTER_CONF_DIR}/log4j.properties" \
--files ${SERVICE_CONF_DIR}/es-config.properties,\
${SERVICE_CONF_DIR}/hbase-site.xml,\
${FTP_CONF_DIR}/ftpAddress.properties,\
${CLUSTER_CONF_DIR}/sparkJob.properties,\
${FTP_CONF_DIR}/rocketmq.properties \
${COMMON_LIB_DIR}/${CLUSTER_VERSION} &
