#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    stop-dubbo.sh
## Description: stop dubbo
## Version:     1.0
## Author:      liushanbin
## Created:     2018-01-08
################################################################################

#set -x
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf
if [ -z "$SERVER_NAME" ]; then
    SERVER_NAME=`hostname`
fi

SERVER_NAME=`sed '/dubbo.application.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
SERVER_PROTOCOL=`sed '/dubbo.protocol.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
SERVER_PORT=`sed '/dubbo.protocol.port/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
LOGS_FILE=`sed '/dubbo.log4j.file/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`

LOGS_DIR=""
if [ -n "$LOGS_FILE" ]; then
    LOGS_DIR=`dirname $LOGS_FILE`
else
    LOGS_DIR=$DEPLOY_DIR/logs
fi
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
LOG_FILE=$LOGS_DIR/stop_dubbo.log
source /etc/profile
stop_dubbo=1                                                      ## 判断dubbo是否关闭成功 1->失败 0->成功 默认失败
stop_check_dubbo=1

#####################################################################
# 函数名:stopdubbo 
# 描述: 停止dubbo
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function stopdubbo ()
{
    echo ""  | tee -a $LOG_FILE
    echo "****************************************************"  | tee -a $LOG_FILE
    echo "dubbo procceding......................." | tee  -a $LOG_FILE
    dubbo_pid=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
    echo "dubbo's pid is: ${dubbo_pid}"  | tee -a $LOG_FILE
    if [ -n "${dubbo_pid}" ];then
        echo "dubbo process is exit,exit with 0,kill dubbo now " | tee -a $LOG_FILE  
        kill -9 ${dubbo_pid}
        sleep 5s
        dubbo_pid=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
        if [ -n "${dubbo_pid}" ];then
            stop_dubbo=1
            echo "stop dubbo failure, retry it again."  | tee -a  $LOG_FILE
        else
            stop_dubbo=0
            echo "stop dubbo sucessed, just to start dubbo."  | tee -a  $LOG_FILE
        fi
    else 
        echo "dubbo process is not exit, just to start dubbo."   | tee -a $LOG_FILE
        stop_dubbo=0
    fi
}
#####################################################################
# 函数名: stop_check_dubbo
# 描述:  停止check_dubbo的入口函数
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function stop_check_dubbo()
{
    echo ""  | tee -a $LOG_FILE
    echo "****************************************************"  | tee -a $LOG_FILE
    echo " start stop check_dubbo ......................." | tee  -a $LOG_FILE
    check_dubbo_pid=$(ps -ef | grep start-check-dubbo.sh |grep -v grep | awk  '{print $2}' | uniq)
    echo "check_dubbo's pid is: ${check_dubbo_pid}"  | tee -a $LOG_FILE
    if [ -n "${check_dubbo_pid}" ];then
        echo "check_dubbo is exit,exit with 0,kill check_dubbo now " | tee -a $LOG_FILE
        kill -9 ${check_dubbo_pid}
        sleep 5s
        check_dubbo_pid_restart=$(ps -ef | grep start-check-dubbo.sh |grep -v grep | awk  '{print $2}' | uniq)
        if [ -n "${check_dubbo_pid_restart}" ];then
            stop_check_dubbo=1
            echo "stop check_dubbo failure, retry it again."  | tee -a  $LOG_FILE
        else
            stop_check_dubbo=0
            echo "stop check_dubbo sucessed, just to start check_dubbo."  | tee -a  $LOG_FILE
        fi
    else
        echo "check_dubbo is not exit, just to start check_dubbo."   | tee -a $LOG_FILE
        stop_check_dubbo=0
    fi
}
#####################################################################
# 函数名: main
# 描述:  停止dubbo的入口函数
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    stopdubbo
    if [ ${stop_dubbo} -eq 0 ];then
        echo "stop dubbo sucessed" | tee -a  $LOG_FILE
    else
        stopdubbo
        if [ ${stop_dubbo} -eq 1 ];then
            echo "retry stop dubbo failed ..." | tee -a  $LOG_FILE
        fi
    fi

    stop_check_dubbo
    if [ ${stop_check_dubbo} -eq 0 ];then
        echo "stop check_dubbo sucessed" | tee -a  $LOG_FILE
    else
        stop_check_dubbo
        if [ ${stop_check_dubbo} -eq 1 ];then
            echo "retry stop check_dubbo failed ..." | tee -a  $LOG_FILE
        fi
    fi

}

echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                        | tee  -a  $LOG_FILE
main
