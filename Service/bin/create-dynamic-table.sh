#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-dynamic-table.sh
## Description:  创建动态库表的所有表格，自定义函数，索引
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
## HBase_home
HBASE_HOME=${BIGDATA_CLUSTER_PATH}/HBase/hbase
## sql 文件目录
SQL_DIR=${DEPLOY_DIR}/sql
## bigdata hadoop path
HADOOP_PATH=${BIGDATA_CLUSTER_PATH}/Hadoop/hadoop
## bigdata hive path
HIVE_PATH=${BIGDATA_CLUSTER_PATH}/Hive/hive
## udf function name
UDF_FUNCTION_NAME=compare
## udf class path
UDF_CLASS_PATH=com.hzgc.udf.spark.UDFArrayCompare
## hdfs udf  path
HDFS_UDF_PATH=/user/hive/udf
## udf jar version
UDF_VERSION=udf-2.1.0.jar
## hdfs udf Absolute path
HDFS_UDF_ABSOLUTE_PATH=hdfs://hzgc/${HDFS_UDF_PATH}/${UDF_VERSION}
cd ..
## 大数据项目根目录
BIGDATA_HOME=`pwd`
## common 模块根目录
COMMON_DIR=${BIGDATA_HOME}/common
## COMMON lib 目录
COMMON_LIB=${COMMON_DIR}/lib
## 项目配置文件
CONF_FILE=$COMMON_DIR/conf/project-conf.properties     ### 项目配置文件

##集群配置文件目录
CONF_HZGC_DIR=/opt/hzgc/conf



#####################################################################
# 函数名: add_hive_UDF
# 描述: 添加Hive 自定义函数
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function add_hive_UDF(){
    ## 判断hdfs上/user/hive/udf目录是否存在
    ${HADOOP_PATH}/bin/hdfs dfs -test -e ${HDFS_UDF_PATH}
    if [ $? -eq 0 ] ;then
        echo "=================================="
        echo "${HDFS_UDF_PATH}已经存在"
        echo "=================================="
    else
        echo "=================================="
        echo "${HDFS_UDF_PATH}不存在,正在创建"
        echo "=================================="
        ${HADOOP_PATH}/bin/hdfs dfs -mkdir -p ${HDFS_UDF_PATH}
        if [ $? == 0 ];then
            echo "=================================="
            echo "创建${HDFS_UDF_PATH}目录成功......"
            echo "=================================="
        else
            echo "====================================================="
            echo "创建${HDFS_UDF_PATH}目录失败,请检查服务是否启动......"
            echo "====================================================="
        fi
    fi

    ## 上传udf到hdfs指定目录
    ${HADOOP_PATH}/bin/hdfs dfs -test -e ${HDFS_UDF_PATH}/${UDF_VERSION}
    if [ $? -eq 0 ] ;then
        echo "=================================="
        echo "${HDFS_UDF_PATH}/${UDF_VERSION}已经存在"
        echo "=================================="
    else
        echo "=================================="
        echo "${HDFS_UDF_PATH}/${UDF_VERSION}不存在,正在上传"
        echo "=================================="
        ${HADOOP_PATH}/bin/hdfs dfs -put ${COMMON_LIB}/${UDF_VERSION} ${HDFS_UDF_PATH}
        if [ $? == 0 ];then
            echo "===================================="
            echo "上传udf函数成功......"
            echo "===================================="
        else
            echo "===================================="
            echo "上传udf函数失败,请查找失败原因......"
            echo "===================================="
        fi
    fi

    ## 在hive中添加并注册udf函数
    ${HIVE_PATH}/bin/hive -e "create function ${UDF_FUNCTION_NAME} as '${UDF_CLASS_PATH}' using jar '${HDFS_UDF_ABSOLUTE_PATH}';show functions"

    echo "================================================================================="
    echo "Please see if there is a UDF function with the function called default.${UDF_FUNCTION_NAME}!!!"
    echo "================================================================================="
}

#####################################################################
# 函数名: create_person_table_mid_table
# 描述: 创建person表， mid_table 表格
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_person_table_mid_table() {
    ${SPARK_PATH}/bin/spark-sql -e "CREATE EXTERNAL TABLE IF NOT EXISTS default.person_table( \
                                    ftpurl        string, \
                                    ipcid         string, \
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
                                    sharpness     int) \
                                    partitioned by (date string) \
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
                                    ipcid         string, \
                                    sharpness     int) \
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
}


function create_searchRes_device_clusteringInfo_table() {
    #---------------------------------------------------------------------#
    #                 创建searchRes, device，clusteringInfo                         #
    #---------------------------------------------------------------------#
    echo "**********************************************" | tee -a $LOG_FILE
    echo "please waitinng, 一键创建动态库中'searchRes'表；创建'device'设备表， “clusteringInfo”........"  | tee -a $LOG_FILE
    sh ${HBASE_HOME}/bin/hbase shell ${SQL_DIR}/deviceAndDynamic.sql
    if [ $? = 0 ];then
        echo "创建成功..."
    else
        echo "创建失败..."
    fi
}





#####################################################################
# 函数名: index_es_dynamic
# 描述: index_es的子函数，替换index-dynamic.sh中的节点名
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es_dynamic()
{
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/index-dynamic.sh.templete" ]; then
        cp ${BIN_DIR}/index-dynamic.sh.templete ${BIN_DIR}/index-dynamic.sh

        ### 替换index-dynamic.sh中的节点名，共三处
        # 要替换的节点名，如s106
        ES_IP=$(grep es_servicenode ${CONF_FILE} | cut -d '=' -f2)
        ES_Host=$(cat /etc/hosts|grep "$ES_IP" | awk '{print $2}')

        ## 第一处
        # 要查找的目标
        a1="curl -XDELETE '"
        b1="/dynamic?pretty'  -H 'Content-Type: application/json'"
        replace1="curl -XDELETE '${ES_Host}:9200/dynamic?pretty'  -H 'Content-Type: application/json'"
        # ^表示以什么开头，.*a表示以a结尾。替换以a1开头、b1结尾匹配到的字符串为repalce1
        sed -i "s#^${a1}.*${b1}#${replace1}#g" ${BIN_DIR}/index-dynamic.sh

        ## 第二处
        a2="curl -XPUT '"
        b2="/dynamic?pretty' -H 'Content-Type: application/json' -d'"
        replace2="curl -XPUT '${ES_Host}:9200/dynamic?pretty' -H 'Content-Type: application/json' -d'"
        sed -i "s#^${a2}.*${b2}#${replace2}#g" ${BIN_DIR}/index-dynamic.sh

        ## 第三处
        a3="curl -XPUT '"
        b3="/dynamic/_settings' -d '{"
        replace3="curl -XPUT '${ES_Host}:9200/dynamic/_settings' -d '{"
        sed -i "s#^${a3}.*${b3}#${replace3}#g" ${BIN_DIR}/index-dynamic.sh

		##获取分片、副本数
        Shards=$(grep Number_Of_Shards ${CONF_FILE} | cut -d '=' -f2)
        Replicas=$(grep Number_Of_Replicas ${CONF_FILE} | cut -d '=' -f2)
        ##获取模糊查询最小、最大字符数
        Min_Gram=$(grep Min_Gram ${CONF_FILE} | cut -d '=' -f2)
        Max_Gram=$(grep Max_Gram ${CONF_FILE} | cut -d '=' -f2)
        ##获取最大分页搜素文档数
        Max_Result_Window=$(grep Max_Result_Window ${CONF_FILE} | cut -d '=' -f2)

        sed -i "s#^\"min_gram\"#\"min_gram\":${Min_Gram}#g" ${BIN_DIR}/index-dynamic.sh
        sed -i "s#^\"max_gram\"#\"max_gram\":${Max_Gram}#g" ${BIN_DIR}/index-dynamic.sh
        sed -i "s#^\"number_of_shards\"#\"number_of_shards\":${Shards}#g" ${BIN_DIR}/index-dynamic.sh
        sed -i "s#^\"number_of_replicas\"#\"number_of_replicas\":${Replicas}#g" ${BIN_DIR}/index-dynamic.sh
        sed -i "s#^\"max_result_window\"#\"max_result_window\":${Max_Result_Window}#g" ${BIN_DIR}/index-dynamic.sh


        sh ${BIN_DIR}/index-dynamic.sh
		if [ $? = 0 ];then
			echo "修改index-dynamic.sh成功并执行......"  | tee  -a  $LOG_FILE
		fi
    else
        echo "index-dynamic.sh.templete不存在...."
    fi
}


function main() {
    ## create kafka topic
    sh   ${BIN_DIR}/create-kafka-topic.sh 
    add_hive_UDF
    create_person_table_mid_table
    create_searchRes_device_clusteringInfo_table
    index_es_dynamic
}

main
