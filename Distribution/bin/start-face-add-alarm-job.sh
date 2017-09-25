#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-face-add-alarm-job.sh
## Description: to start faceAddAlarmJob
## Version:     1.0
## Author:      liushanbin
## Created:     2017-09-08
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 配置文件目录
LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/sparkFaceAddAlarmJob.log       ##  log 日记文件

if [ ! -d $LOG_DIR ];then
   mkdir $LOG_DIR
fi

#判断是否存在FI客户端
if [ ! -d /opt/client ];then
   echo "$/opt/client does not exit,please go to the node of the existing FI client !"
   exit 0
fi

#判断是否存在配置文件
if [ ! -e $CONF_DIR/es-config.properties ];then
    echo "$CONF_DIR/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e $CONF_DIR/rocketmq.properties ];then
    echo "$CONF_DIR/rocketmq.properties does not exit!"
    exit 0
fi
if [ ! -e $CONF_DIR/sparkJob.properties ];then
    echo "$CONF_DIR/sparkJob.properties does not exit!"
    exit 0
fi

#更新FI客户端Spark/spark/conf 下运行任务所需要的配置文件
rm -rf /opt/client/Spark/spark/conf/es-config.properties
rm -rf /opt/client/Spark/spark/conf/rocketmq.properties
rm -rf /opt/client/Spark/spark/conf/sparkJob.properties
cp $CONF_DIR/es-config.properties /opt/client/Spark/spark/conf
cp $CONF_DIR/rocketmq.properties  /opt/client/Spark/spark/conf
cp $CONF_DIR/sparkJob.properties  /opt/client/Spark/spark/conf

#判断是否存在jar
if [ ! -e $LIB_DIR/hbase-client-1.0.2.jar ];then
    echo "$LIB_DIR/hbase-client-1.0.2.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/gson-2.8.0.jar ];then
    echo "$LIB_DIR/gson-2.8.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/jackson-core-2.8.6.jar ];then
    echo "$LIB_DIR/jackson-core-2.8.6.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/spark-streaming-kafka_2.10-1.5.1.jar ];then
    echo "$LIB_DIR/spark-streaming-kafka_2.10-1.5.1.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/hbase-1.0.jar ];then
    echo "$LIB_DIR/hbase-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/hbase-server-1.0.2.jar ];then
    echo "$LIB_DIR/hbase-server-1.0.2.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/jni-1.0.jar ];then
    echo "$LIB_DIR/jni-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/kafka_2.10-0.8.2.1.jar ];then
    echo "$LIB_DIR/kafka_2.10-0.8.2.1.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/elasticsearch-1.0.jar ];then
    echo "$LIB_DIR/elasticsearch-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/ftp-1.0.jar ];then
    echo "$LIB_DIR/ftp-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/rocketmq-1.0.jar ];then
    echo "$LIB_DIR/rocketmq-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/rocketmq-client-4.1.0-incubating.jar ];then
    echo "$LIB_DIR/rocketmq-client-4.1.0-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/rocketmq-common-4.1.0-incubating.jar ];then
    echo "$LIB_DIR/rocketmq-common-4.1.0-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/rocketmq-remoting-4.1.0-incubating.jar ];then
    echo "$LIB_DIR/rocketmq-remoting-4.1.0-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/fastjson-1.2.29.jar ];then
    echo "$LIB_DIR/fastjson-1.2.29.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/util-1.0.jar ];then
    echo "$LIB_DIR/util-1.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/kafka-clients-0.10.0.0.jar ];then
    echo "$LIB_DIR/kafka-clients-0.10.0.0.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/streaming-1.0.jar ];then
    echo "$LIB_DIR/streaming-1.0.jar does not exit!"
    exit 0
fi

## 新增告警任务
source /etc/profile
source /opt/client/bigdata_env
nohup spark-submit \
--master yarn-cluster \
--executor-memory 5g \
--executor-cores 4 \
--class com.hzgc.streaming.job.FaceAddAlarmJob \
--jars $LIB_DIR/gson-2.8.0.jar,\
$LIB_DIR/hbase-client-1.0.2.jar,\
$LIB_DIR/jackson-core-2.8.6.jar,\
$LIB_DIR/spark-streaming-kafka_2.10-1.5.1.jar,\
$LIB_DIR/hbase-1.0.jar,\
$LIB_DIR/hbase-server-1.0.2.jar,\
$LIB_DIR/jni-1.0.jar,\
$LIB_DIR/kafka_2.10-0.8.2.1.jar,\
$LIB_DIR/elasticsearch-1.0.jar,\
$LIB_DIR/ftp-1.0.jar,\
$LIB_DIR/rocketmq-1.0.jar,\
$LIB_DIR/rocketmq-client-4.1.0-incubating.jar,\
$LIB_DIR/rocketmq-common-4.1.0-incubating.jar,\
$LIB_DIR/rocketmq-remoting-4.1.0-incubating.jar,\
$LIB_DIR/fastjson-1.2.29.jar,\
$LIB_DIR/util-1.0.jar,\
$LIB_DIR/kafka-clients-0.10.0.0.jar \
--files $CONF_DIR/es-config.properties,\
$CONF_DIR/sparkJob.properties,\
$CONF_DIR/rocketmq.properties \
$LIB_DIR/streaming-1.0.jar > $LOG_FILE 2>&1 &