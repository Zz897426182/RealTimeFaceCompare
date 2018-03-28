#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-dubbo-all
## Description: 在配置节点下启动dubbo
## Version:     1.0
## Author:      caodabao
## Created:     2017-11-29 
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
DEPLOY_DIR=`pwd`                                      ### service模块部署目录
CONF_SERVICE_DIR=$DEPLOY_DIR/conf                     ### 配置文件目录
LOG_DIR=$DEPLOY_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/config-service.log                  ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
CONF_DIR=$OBJECT_DIR/project-conf.properties          ### 项目配置文件
cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                   ### 集群配置文件目录

#####################################################################
# 函数名: start_all_dubbo
# 描述: 启动dubbo
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_all_dubbo()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    ## 获取dubbo节点IP
    cd ${OBJECT_DIR}
    DUBBO_HOSTS=$(grep dubbo_servicenode project-conf.properties|cut -d '=' -f2)
    dubbo_arr=(${DUBBO_HOSTS//;/ }) 
    for dubbo_host in ${dubbo_arr[@]}
    do
        echo "${dubbo_host}节点下启动dubbo................."  | tee  -a  $LOG_FILE
        ssh root@${dubbo_host}  "source /etc/profile;cd ${BIN_DIR};sh start-dubbo.sh" 
        if [ $? -eq 0 ];then
            echo  -e '${dubbo_host}节点下启动dubbo成功\n'
        else
            echo  -e '${dubbo_host}节点下启动dubbo失败 \n'
        fi
    done 
    echo "启动dubbo完毕......"  | tee  -a  $LOG_FILE
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
    start_all_dubbo
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
main

set +x
