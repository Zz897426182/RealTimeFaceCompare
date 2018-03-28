#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-check-dubbo.sh
## Description: 大数据dubbo ftp 守护脚本
## Version:     1.0.0
## Author:      liushanbin
## Created:     2018-01-08
################################################################################

#set -x

#crontab 里面不会读取jdk环境变量的值
source /etc/profile

#set -x



#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname  $0`
declare -r BIN_DIR=`pwd`   #bin 目录
cd ..
declare -r  DEPLOY_DIR=`pwd`  #项目根目录
declare -r  LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
declare -r  CHECK_LOG_FILE=${LOG_DIR}/check_dubbo.log

#####################################################################
# 函数名: check_dubbo
# 描述: 把脚本定时执行，定时监控dubbo 服务是否挂掉，如果挂掉则重启。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function check_dubbo()
{
    echo ""  | tee  -a  $CHECK_LOG_FILE
    echo "****************************************************"  | tee -a $CHECK_LOG_FILE
    echo "dubbo procceding ing......................."  | tee  -a  $CHECK_LOG_FILE
    dubbo_pid=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
    echo "dubbo's pid is: ${dubbo_pid}"  | tee  -a  $CHECK_LOG_FILE
    if [ -n "${dubbo_pid}" ];then
        echo "dubbo process is exit,do not need to do anything. exit with 0 " | tee -a $CHECK_LOG_FILE
    else
        echo "dubbo process is not exit, just to restart dubbo."  | tee -a $CHECK_LOG_FILE
        sh ${BIN_DIR}/start-dubbo.sh
        echo "starting, please wait........" | tee -a $CHECK_LOG_FILE
        sleep 1m
        dubbo_pid_restart=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
        if [ -z "${dubbo_pid_restart}" ];then
            echo "start dubbo failed.....,retrying to start it second time" | tee -a $CHECK_LOG_FILE
            sh ${BIN_DIR}/start-dubbo.sh
            echo "second try starting, please wait........" | tee -a $CHECK_LOG_FILE
            sleep 1m
            dubbo_pid_retry=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
            if [ -z  "${dubbo_pid_retry}" ];then
                echo "retry start dubbo failed, please check the config......exit with 1"  | tee -a $CHECK_LOG_FILE
            else
                echo "secondary try start ftp sucess. exit with 0." | tee -a $CHECK_LOG_FILE
            fi
        else
            echo "trying to restart dubbo sucess. exit with 0."  | tee -a $CHECK_LOG_FILE
        fi
    fi
}

#####################################################################
# 函数名: main
# 描述: 模块功能main 入口，即程序入口, 用来监听整个大数据服务的情况。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
   while true
   do
       check_dubbo
       sleep 5m
   done
}


# 主程序入口
main


