#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-consumer.sh
## Description: to start consumer
## Version:     1.0
## Author:      lidiliang
## Created:     2017-11-18
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
declare -r  CONF_DIR=${DEPLOY_DIR}/conf
declare -r  LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
declare -r  LOG_FILE=${LOG_DIR}/make_sure_bidata_service_alive.log        ##  log 日记文件
flag_ftp=0   #标志ftp 进程是否在线
flag_consumer=0  # 标志consumer进程是否存活
flag_dubbo=0    # 标志dubbo 进程是否存活

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r FTP_HOSTS_FILE=${CONF_DIR}/ftp-hostnames.properties

#####################################################################
# 函数名: make_sure_the_ftp_service_alive
# 描述: 把脚本定时执行，每个小时监控一下ftp 服务是否挂掉，如果挂掉则重启。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function make_sure_the_ftp_service_alive()
{
    echo ""  | tee -a $LOG_FILE
    echo "****************************************************"  | tee -a $LOG_FILE
    echo "in $1: monitoring ftp service ing......................." | tee  -a $LOG_FILE
    ftp_pid=$(ssh root@$1 "source /etc/profile;jps | grep LocalOverFtpServer")
    echo "ftp's pid is: ${ftp_pid}"  | tee -a $LOG_FILE
    if [ -n "${ftp_pid}" ];then
        echo "ftp process is exit,do not need to do anything. exit with 0 " | tee -a $LOG_FILE  
    else 
	echo "ftp process is not exit, just to restart ftp."   | tee -a $LOG_FILE
        ssh root@$1 "source /etc/profile;service ftp start" 
        echo "starting, please wait........" | tee -a $LOG_FILE
        sleep 10s
        ftp_pid_restart=$(ssh root@$1 "source /etc/profile;jps | grep LocalOverFtpServer")
        if [ -z "${ftp_pid_restart}" ];then
            echo "first trying start ftp failed.....,retrying to start it second time"  | tee -a $LOG_FILE
            ssh root@$1 "source /etc/profile;service ftp start"
            echo "second try starting, please wait........" | tee -a $LOG_FILE
            sleep 10s
            ftp_pid_retry=$(ssh root@$1 "source /etc/profile;jps | grep LocalOverFtpServer")
            if [ -z  "${ftp_pid_retry}" ];then
                echo "retry start ftp failed, please check the config......exit with 1"  | tee -a  $LOG_FILE
                flag_ftp=1
            else
                echo "secondary try start ftp sucess. exit with 0."  | tee -a  $LOG_FILE
            fi
        else
            echo "trying to start ftp sucess. exit with 0."  | tee -a  $LOG_FILE
        fi
    fi
}


#####################################################################
# 函数名: make_sure_the_dubbo_service_alive
# 描述: 把脚本定时执行，每个小时监控一下ftp 服务是否挂掉，如果挂掉则重启。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function make_sure_the_dubbo_service_alive()
{
    echo ""  | tee  -a  $LOG_FILE
    echo "****************************************************"  | tee -a $LOG_FILE
    echo "dubbo procceding......................."  | tee  -a  $LOG_FILE
    dubbo_pid=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
    echo "dubbo's pid is: ${dubbo_pid}"  | tee  -a  $LOG_FILE
    if [ -n "${dubbo_pid}" ];then
        echo "dubbo process is exit,do not need to do anything. exit with 0 " | tee -a $LOG_FILE
    else
        echo "dubbo process is not exit, just to restart dubbo."  | tee -a $LOG_FILE
        sh ${BIN_DIR}/start-dubbo.sh
        echo "starting, please wait........" | tee -a $LOG_FILE
        sleep 1m
        dubbo_pid_restart=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
        if [ -z "${dubbo_pid_restart}" ];then
            echo "start dubbo failed.....,retrying to start it second time" | tee -a $LOG_FILE
            sh  ${BIN_DIR}/start-dubbo.sh
            echo "second try starting, please wait........" | tee -a $LOG_FILE
            sleep 1m
            dubbo_pid_retry=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
            if [ -z  "${dubbo_pid_retry}" ];then
                echo "retry start dubbo failed, please check the config......exit with 1"  | tee -a $LOG_FILE
                flag_dubbo=1
            else
                echo "secondary try start ftp sucess. exit with 0." | tee -a $LOG_FILE
            fi
        else
            echo "trying to restart dubbo sucess. exit with 0."  | tee -a $LOG_FILE
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
    echo ""  | tee  -a  $LOG_FILE
    echo ""  | tee  -a  $LOG_FILE
    echo "==================================================="  | tee -a $LOG_FILE
    echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
    ## 监听ftp 进程情况 
    for host in $(cat ${FTP_HOSTS_FILE});do
        make_sure_the_ftp_service_alive  $host
    done
    ## 监听 dubbo 服务情况
    make_sure_the_dubbo_service_alive
}


# 主程序入口
main
echo "" | tee  -a  $LOG_FILE
echo "*******************************************************************"  | tee  -a  $LOG_FILE
if [[ ($flag_ftp -eq 0) && ($flag_consumer -eq 0) && ($flag_dubbo -eq 0) ]];then
    echo "the bigdata service is bling bling health!!!!!!!!!!!!!!!!!!!" | tee  -a  $LOG_FILE
fi


set +x

