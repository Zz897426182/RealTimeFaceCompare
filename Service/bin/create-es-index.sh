#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-es-index.sh
## Description:  创建es动态与静态索引，执行index-dynamic.sh和index-static.sh
## Author:       mashencai
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
SERVICE_DIR=`pwd`                                      ### service模块部署目录
CONF_SERVICE_DIR=$SERVICE_DIR/conf                     ### 配置文件目录

LOG_DIR=${SERVICE_DIR}/logs                            ### log 日记目录
LOG_FILE=${LOG_DIR}/create-es-index.log                ### log 日记文件

cd ..
OBJECT_DIR=`pwd`                                       ### 项目根目录 
COMMON_DIR=$OBJECT_DIR/common                          ### service模块部署目录
CONF_COMMON_DIR=$COMMON_DIR/conf                       ### 配置文件目录
CONF_FILE=$CONF_COMMON_DIR/project-conf.properties     ### 项目配置文件

cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                    ### 集群配置文件目录


#####################################################################
# 函数名: index_es
# 描述: 创建es索引：执行index-dynamic.sh和index-static.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "创建es索引......"  | tee  -a  $LOG_FILE

        index_es_dynamic
        index_es_static
    
    echo "创建完毕......"  | tee  -a  $LOG_FILE
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

#####################################################################
# 函数名: index_es_static
# 描述: index_es的子函数，替换index-static.sh中的节点名
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es_static()
{
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/index-static.sh.templete" ]; then 
        cp ${BIN_DIR}/index-static.sh.templete ${BIN_DIR}/index-static.sh
		
        ### 替换index-static.sh中的节点名，共三处
        # 要替换的节点名，如s106
        ES_IP=$(grep es_servicenode ${CONF_FILE} | cut -d '=' -f2)
        ES_Host=$(cat /etc/hosts|grep "$ES_IP" | awk '{print $2}')
		
        ## 第一处
        # 要查找的目标
        a1="curl -XDELETE '"
        b1="/objectinfo?pretty' -H 'Content-Type: application/json'"
        replace1="curl -XDELETE '${ES_Host}:9200/objectinfo?pretty' -H 'Content-Type: application/json'"
        # ^表示以什么开头，.*a表示以a结尾。替换以a1开头、b1结尾匹配到的字符串为repalce1
        sed -i "s#^${a1}.*${b1}#${replace1}#g" ${BIN_DIR}/index-static.sh
		
        ## 第二处
        a2="curl -XPUT '"
        b2="/objectinfo?pretty' -H 'Content-Type: application/json' -d'"
        replace2="curl -XPUT '${ES_Host}:9200/objectinfo?pretty' -H 'Content-Type: application/json' -d'"
        sed -i "s#^${a2}.*${b2}#${replace2}#g" ${BIN_DIR}/index-static.sh
		
        ## 第三处
        a3="curl -XPUT '"
        b3="/objectinfo/_settings' -d '{"
        replace3="curl -XPUT '${ES_Host}:9200/objectinfo/_settings' -d '{"
        sed -i "s#^${a3}.*${b3}#${replace3}#g" ${BIN_DIR}/index-static.sh
		
        ##获取分片、副本数
        Shards=$(grep Number_Of_Shards ${CONF_FILE} | cut -d '=' -f2)
        Replicas=$(grep Number_Of_Replicas ${CONF_FILE} | cut -d '=' -f2)
        ##获取模糊查询最小、最大字符数
        Min_Gram=$(grep Min_Gram ${CONF_FILE} | cut -d '=' -f2)
        Max_Gram=$(grep Max_Gram ${CONF_FILE} | cut -d '=' -f2)
        ##获取最大分页搜素文档数
        Max_Result_Window=$(grep Max_Result_Window ${CONF_FILE} | cut -d '=' -f2)
        
        sed -i "s#^\"min_gram\"#\"min_gram\":${Min_Gram}#g" ${BIN_DIR}/index-static.sh
        sed -i "s#^\"max_gram\"#\"max_gram\":${Max_Gram}#g" ${BIN_DIR}/index-static.sh
        sed -i "s#^\"number_of_shards\"#\"number_of_shards\":${Shards}#g" ${BIN_DIR}/index-static.sh
        sed -i "s#^\"number_of_replicas\"#\"number_of_replicas\":${Replicas}#g" ${BIN_DIR}/index-static.sh
        sed -i "s#^\"max_result_window\"#\"max_result_window\":${Max_Result_Window}#g" ${BIN_DIR}/index-static.sh
		
        sh ${BIN_DIR}/index-static.sh
		if [ $? = 0 ];then
			echo "修改index-static.sh成功并执行......"  | tee  -a  $LOG_FILE
		fi
    else
        echo "index-static.sh.templete不存在...." 
    fi
}

#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

index_es