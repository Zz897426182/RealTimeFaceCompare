#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-device-and-dynamic.sh
## Description: 创建动态库中的'upFea'、'searchRes'表；创建'device'设备表
## Version:     1.0
## Author:      mashencai
## Created:     2017-12-16
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#

cd `dirname $0`
## bin目录
BIN_DIR=`pwd`
cd ..
SERVICE_DEPLOY_DIR=`pwd`
## 配置文件目录
SERVICE_CONF_DIR=${SERVICE_DEPLOY_DIR}/conf
## sql文件目录
SQL_DIR=${SERVICE_DEPLOY_DIR}/sql
## 日志路径
LOG_DIR=${SERVICE_DEPLOY_DIR}/logs
## 日志文件
LOG_FILE=${LOG_DIR}/create-device-and-dynamic.log

cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
## common 模块根目录
COMMON_HOME_DIR=${OBJECT_DIR}/common
## common 模块conf 目录
COMMON_CONF_DIR=${COMMON_HOME_DIR}/conf
## project_conf.properties
PROJECT_CONF_FILE=$COMMON_CONF_DIR/project_conf.properties ### 集群配置文件
## 最终安装的根目录，所有bigdata 相关的根目录
INSTALL_HOME=$(grep Install_HomeDir ${PROJECT_CONF_FILE}|cut -d '=' -f2)
## HBASE安装目录
HBASE_INSTALL_HOME=${INSTALL_HOME}/HBase
## HBASE组件的根目录
HBASE_HOME=${HBASE_INSTALL_HOME}/hbase



if [ ! -d $LOG_DIR ];then
    mkdir -p $LOG_DIR;
fi

echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE

echo ""  | tee -a $LOG_FILE
echo "**********************************************" | tee -a $LOG_FILE
echo "please waitinng, 一键创建动态库中的'upFea'、'searchRes'表；创建'device'设备表........"  | tee -a $LOG_FILE

sh ${HBASE_HOME}/bin/hbase shell ${SQL_DIR}/deviceAndDynamic.sql
if [ $? = 0 ];then
	echo "创建成功..."
else
	echo "创建失败..."
fi
