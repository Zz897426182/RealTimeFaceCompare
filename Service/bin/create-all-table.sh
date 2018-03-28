#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-all.sh
## Description: 一键创建：
##              ①hive表：动态库的person_table,mid_table表（create-dynamic-table.sh）;
##                静态库的静态库建表：objectinfo、srecord（create-static-repos.sh）;
##              ②es索引：dynamic; objectinfo（create-es-index.sh）
##              ③kafka的topic（create-kafka-topic.sh）
##              ④add-udf（add-udf.sh）
##              ⑤动态库中的'upFea'、'searchRes'表;'device'设备表（create-device-and-dynamic.sh）
##              相关参数（例如kafka分区数）在配置文件project-conf.properties中配置
## Version:     1.0
## Author:      mashencai
## Created:     2017-11-30 
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                                          ### bin目录：脚本所在目录
cd ..
SERVICE_DIR=`pwd`                                      ### service模块部署目录
CONF_SERVICE_DIR=$SERVICE_DIR/conf                     ### 配置文件目录
LOG_DIR=$SERVICE_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/create-all.log                       ### log日志文件
cd ..
OBJECT_DIR=`pwd`                                       ### 项目根目录 
COMMON_DIR=$OBJECT_DIR/common                          ### common模块部署目录
CONF_COMMON_DIR=$COMMON_DIR/conf                       ### 配置文件目录
CONF_FILE=$CONF_COMMON_DIR/project-conf.properties     ### 项目配置文件

cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                    ### 集群配置文件目录

#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#

#####################################################################
# 函数名: create_hivetable
# 描述: 创建hive表，执行create-dynamic-table.sh和create-static-repos.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_hivetable()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "创建hive表......"  | tee  -a  $LOG_FILE

	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-dynamic-table.sh" ]; then 
        sh ${BIN_DIR}/create-dynamic-table.sh
    else
        echo "create-dynamic-table.sh脚本不存在...." 
    fi
	
	if [ -f "${BIN_DIR}/create-static-repos.sh" ]; then 
        sh ${BIN_DIR}/create-static-repos.sh
    else
        echo "create-static-repos.sh脚本不存在...." 
    fi
    
    echo "创建完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: add_udf
# 描述: 执行add-udf.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function add_udf()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行add-udf.sh......"  | tee  -a  $LOG_FILE

	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/add-udf.sh" ]; then 
        sh ${BIN_DIR}/add-udf.sh
    else
        echo "add-udf.sh脚本不存在...." 
    fi
    
    echo "执行完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: index_es
# 描述: 创建es动态与静态索引，执行index-dynamic.sh和index-static.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行create-es-index.sh......"  | tee  -a  $LOG_FILE

	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-es-index.sh" ]; then 
        sh ${BIN_DIR}/create-es-index.sh
    else
        echo "create-es-index.sh脚本不存在...." 
    fi
    
    echo "执行完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: create_kafka_topic
# 描述: 创建kafka的topic
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_kafka_topic()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行create-kafka-topic.sh......"  | tee  -a  $LOG_FILE
    
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-kafka-topic.sh" ]; then 
        sh ${BIN_DIR}/create-kafka-topic.sh
    else
        echo "create-kafka-topic.sh脚本不存在...." 
    fi
	
    echo "执行完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: create_device_and_dynamic
# 描述: 创建动态库中的'upFea'、'searchRes'表；创建'device'设备表
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_device_and_dynamic()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行create-device-and-dynamic.sh......"  | tee  -a  $LOG_FILE
    
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-device-and-dynamic.sh" ]; then 
        sh ${BIN_DIR}/create-device-and-dynamic.sh
    else
        echo "create-device-and-dynamic.sh脚本不存在...." 
    fi
	
    echo "执行完毕......"  | tee  -a  $LOG_FILE
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
    create_hivetable
    add_udf
    index_es
    create_kafka_topic
	create_device_and_dynamic
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
echo "开始一键创建：hive表、es索引、kafka的topic、add-udf.." | tee  -a  $LOG_FILE
main

set +x
