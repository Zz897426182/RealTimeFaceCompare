#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-projectconf
## Description: 一键配置脚本：执行common、cluster、ftp、service的一键配置脚本
## Version:     1.0
## Author:      mashencai
## Created:     2017-11-30
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
COMMON_DIR=`pwd`                                      ### common模块部署目录
CONF_COMMON_DIR=$COMMON_DIR/conf                      ### 配置文件目录
CONF_FILE=$CONF_COMMON_DIR/project-conf.properties    ### 项目配置文件

LOG_DIR=$COMMON_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/config-project.log                  ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
CLUSTER_DIR=$OBJECT_DIR/cluster                       ### cluster模块部署目录
FTP_DIR=$OBJECT_DIR/ftp                               ### ftp模块部署目录
SERVICE_DIR=$OBJECT_DIR/service                       ### service模块部署目录

CLUSTER_SCRPIT=$CLUSTER_DIR/bin/config-clusterconf.sh ### 一键配置cluster脚本
FTP_SCRPIT=$FTP_DIR/bin/config-ftpconf.sh             ### 一键配置ftp脚本
SERVICE_SCRPIT=$SERVICE_DIR/bin/config-serviceconf.sh ### 一键配置service脚本

CLUSTER_LOG_DIR=$CLUSTER_DIR/logs                     ### cluster的log日志目录
FTP_LOG_DIR=$FTP_DIR/logs                             ### ftp的log日志目录
SERVICE_LOG_DIR=$SERVICE_DIR/logs                     ### service的log日志目录

# 创建日志目录
mkdir -p $LOG_DIR
mkdir -p $CLUSTER_LOG_DIR
mkdir -p $FTP_LOG_DIR
mkdir -p $SERVICE_LOG_DIR


#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#

#####################################################################
# 函数名: config_common_dubbo
# 描述: 配置文件：common/conf/dubbo-hostnames.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_common_dubbo()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置common/conf/dubbo-hostnames.properties......"  | tee  -a  $LOG_FILE
    
    # 删除原本dubbo-hostnames.properties内容（从第一行开始的行）
    sed -i '1,$d' ${CONF_COMMON_DIR}/dubbo-hostnames.properties
	
    ### 从project-conf.properties读取dubbo所需配置IP
    # 根据字段dubbo_servicenode，查找配置文件中，dubbo的服务节点所在IP，这些值以分号分割
    cd ${OBJECT_DIR}
    DUBBO_HOSTS=$(grep dubbo_servicenode ${CONF_FILE}|cut -d '=' -f2)
    # 将这些分号分割的ip用放入数组，将数组每行添加到dubbo-hostnames.properties文件末尾
    dubbo_arr=(${DUBBO_HOSTS//;/ })
    dubbopro=''    
    for dubbo_host in ${dubbo_arr[@]}
    do
        echo "${dubbo_host}" >> ${CONF_COMMON_DIR}/dubbo-hostnames.properties
    done

    echo "配置完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: config_common_ftp
# 描述: 配置common/conf/ftp-hostnames.properties的地址
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_common_ftp()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置common/conf/ftp-hostnames.properties......"  | tee  -a  $LOG_FILE
	
	# 删除原本dubbo-hostnames.properties内容（从第一行开始的行）
	sed -i '1,$d' ${CONF_COMMON_DIR}/ftp-hostnames.properties
	
    ### 从project-conf.properties读取ftp所需配置IP
    # 根据字段ftp_servicenode，查找配置文件中，ftp的服务节点主机名，这些值以分号分割
    cd ${OBJECT_DIR}
    FTP_HOSTS=$(grep ftp_servicenode ${CONF_FILE}|cut -d '=' -f2)
    # 将这些分号分割的ip用放入数组，将数组每行添加到dubbo-hostnames.properties文件末尾
    ftp_arr=(${FTP_HOSTS//;/ })
    ftppro=''    
    for ftp_host in ${ftp_arr[@]}
    do
        echo "${ftp_host}" >> ${CONF_COMMON_DIR}/ftp-hostnames.properties
    done
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: distribute_common
# 描述: 将common文件夹分发到所有节点
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function distribute_common()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "分发common......"  | tee  -a  $LOG_FILE
    
	CLUSTER_HOSTNAME_LISTS=$(grep Cluster_HostName ${CONF_FILE}|cut -d '=' -f2)
	CLUSTER_HOSTNAME_ARRY=(${CLUSTER_HOSTNAME_LISTS//;/ })
    for hostname in ${CLUSTER_HOSTNAME_ARRY[@]}
    do
        ssh root@${hostname} "if [ ! -x "${OBJECT_DIR}" ]; then mkdir "${OBJECT_DIR}"; fi"
        rsync -rvl ${OBJECT_DIR}/common   root@${hostname}:${OBJECT_DIR}  >/dev/null
        ssh root@${hostname}  "chmod -R 755   ${OBJECT_DIR}/common"
        echo "${hostname}上分发common完毕......"  | tee  -a  $LOG_FILE
    done 
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: sh_cluster
# 描述: 执行config-clusterconf.sh脚本
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function sh_cluster()
{
	# 判断脚本是否存在，存在才执行
	if [ -f "${CLUSTER_SCRPIT}" ]; then 
		sh ${CLUSTER_SCRPIT}
	else
		echo "config-clusterconf.sh脚本不存在...." 
	fi
}

#####################################################################
# 函数名: sh_ftp
# 描述: 执行config-ftpconf.sh脚本
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function sh_ftp()
{
	if [ -f "${FTP_SCRPIT}" ]; then 
		sh ${FTP_SCRPIT}
	else
		echo "config-ftpconf.sh脚本不存在...." 
	fi
}

#####################################################################
# 函数名: sh_service
# 描述: 执行config-serviceconf.sh脚本
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function sh_service()
{
	if [ -f "${SERVICE_SCRPIT}" ]; then 
		sh ${SERVICE_SCRPIT}
	else
		echo "config-serviceconf.sh脚本不存在...." 
	fi
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
    config_common_dubbo
	config_common_ftp
	distribute_common
    sh_cluster
    sh_ftp
    sh_service
}

#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  
echo ""  
echo "==================================================="
echo "$(date "+%Y-%m-%d  %H:%M:%S")"

main


set +x
