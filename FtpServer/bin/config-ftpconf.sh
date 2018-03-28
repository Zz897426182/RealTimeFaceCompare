#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-ftpconf
## Description: 一键配置脚本：配置项目ftp/conf中的配置文件，并将ftp分发到各个服务节点，将common分发到所有节点
## Version:     1.0
## Author:      mashencai
## Created:     2017-11-29
################################################################################

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
DEPLOY_DIR=`pwd`                                      ### ftp模块部署目录
CONF_FTP_DIR=$DEPLOY_DIR/conf                         ### 配置文件目录
LOG_DIR=$DEPLOY_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/config-ftp.log                      ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
CONF_FILE=$OBJECT_DIR/common/conf/project-conf.properties  ### 项目配置文件

cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                   ### 集群配置文件目录

FTP_DATA_PATH=$(grep FTP_DataDir $OBJECT_DIR/common/conf/project-conf.properties | cut -d '=' -f2)

#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#


#####################################################################
# 函数名: config_ftpAddress
# 描述: 配置文件：ftp/conf/ftpAddress.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_ftpAddress()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置ftp/conf/ftpAddress.properties......"  | tee  -a  $LOG_FILE
    
    ### 配置代理节点
    # 根据ftp_proxynode字段，查找配置文件中，FTP代理节点IP
    FTP_PROXYNODE=$(grep ftp_proxynode ${CONF_FILE}|cut -d '=' -f2)
    # 替换ftpAddress.properties中FTP安装节点IP值：key=value（替换key字段的值value）
    sed -i "s#^ip=.*#ip=${FTP_PROXYNODE}#g" ${CONF_FTP_DIR}/ftpAddress.properties
    
    ### 配置服务节点
    # 删除ftpAddress.properties中与服务节点相关的内容(从第7行开始的行)：
    sed -i '7,$d' ${CONF_FTP_DIR}/ftpAddress.properties
    # 根据ftp_serviceip字段，查找配置文件中，FTP服务节点主机名和IP
    FTP_SERVICEIPS=`sed '/ftp_serviceip/!d;s/.*=//' ${CONF_FILE} | tr -d '\r'`
    # 将查找到的FTP服务节点主机名和IP切分，放入数组中
    ftp_arr=(${FTP_SERVICEIPS//;/ }) 
    # 在文件末尾添加FTP服务节点hostname=ip 
    for ftp_ip in ${ftp_arr[@]}
    do
        echo "${ftp_ip//:/=}" >> ${CONF_FTP_DIR}/ftpAddress.properties
    done
    
    echo "################################################################################" >> ${CONF_FTP_DIR}/ftpAddress.properties
    
    echo "配置ftpAddress.properties完毕......"  | tee  -a  $LOG_FILE
}
#####################################################################
# 函数名: config_cluster_over_ftp
# 描述: 配置文件：ftp/conf/config-cluster-over_ftp.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_cluster_over_ftp()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置ftp/conf/config-cluster-over_ftp.properties......"  | tee  -a  $LOG_FILE

    # 从project-conf.properties中，根据Thread_number字段，读取线程数
    THREAD_NUMBER=`sed '/Thread_number/!d;s/.*=//' ${CONF_FILE} | tr -d '\r'`
    # 替换config-cluster-over_ftp.properties中线程数：key=value（替换key字段的值value）
    sed -i "s#^thread.number=.*#thread.number=${THREAD_NUMBER}#g" ${CONF_FTP_DIR}/cluster-over-ftp.properties

    echo "配置完毕......"  | tee  -a  $LOG_FILE
}
#####################################################################
# 函数名: config_rkmq
# 描述: 配置文件：ftp/conf/rocketmq.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_rkmq()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置ftp/conf/rocketmq.properties......"  | tee  -a  $LOG_FILE
    
    # 从project-conf.properties中，根据rocketmq_nameserver字段，读取RocketMQ的NameServer所在节点IP
    ROCKET_NAMESERVER=`sed '/rocketmq_nameserver/!d;s/.*=//' ${CONF_FILE} | tr -d '\r'`
    # 替换ftpAddress.properties中FTP安装节点IP值：key=value（替换key字段的值value）
    sed -i "s#^address=.*#address=${ROCKET_NAMESERVER}:9876#g" ${CONF_FTP_DIR}/rocketmq.properties
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: config_pdcrOverFtp
# 描述: 配置文件：ftp/conf/producer-over-ftp.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_pdcrOverFtp()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置ftp/conf/producer-over-ftp.properties......"  | tee  -a  $LOG_FILE
    
    ### 从project-conf.properties读取sparkJob所需配置IP
    # 根据字段kafka，查找配置文件中，Kafka的安装节点所在IP端口号的值，这些值以分号分割
    KAFKA_HOSTS=`sed '/kafka_installnode/!d;s/.*=//' ${CONF_FILE} | tr -d '\r'`
    # 将这些分号分割的ip用放入数组
    kafka_arr=(${KAFKA_HOSTS//;/ })
    kafkapro=''    
    for kafka_host in ${kafka_arr[@]}
    do
        kafkapro="$kafkapro$kafka_host:9092,"
    done
    kafkapro=${kafkapro%?}
    
    # 替换producer-over-ftp.properties中：key=value（替换key字段的值value）
    sed -i "s#^bootstrap.servers=.*#bootstrap.servers=${kafkapro}#g" ${CONF_FTP_DIR}/producer-over-ftp.properties
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: config_users
# 描述: 配置文件：ftp/conf/users.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_users()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置ftp/conf/users.properties......"  | tee  -a  $LOG_FILE
    
    # 替换producer-over-ftp.properties中：key=value（替换key字段的值value）
    sed -i "s#^com.hzgc.ftpserver.user.admin.homedirectory=.*#com.hzgc.ftpserver.user.admin.homedirectory=${FTP_DATA_PATH}#g" ${CONF_FTP_DIR}/users.properties
    sed -i "s#^com.hzgc.ftpserver.user.anonymous.homedirectory=.*#com.hzgc.ftpserver.user.anonymous.homedirectory=${FTP_DATA_PATH}#g" ${CONF_FTP_DIR}/users.properties
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: distribute_ftp
# 描述: 将ftp文件夹分发到各个服务节点
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function distribute_ftp()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "分发ftp......"  | tee  -a  $LOG_FILE
    
    ## 根据字段ftp_servicenode，从配置文件中获取ftp服务节点host
    FTP_HOSTS=`sed '/ftp_servicenode/!d;s/.*=//' ${CONF_FILE} | tr -d '\r'`
    ftp_arr=(${FTP_HOSTS//;/ }) 
    for ftp_host in ${ftp_arr[@]}
    do
        ssh root@${ftp_host} "if [ ! -x "${FTP_DATA_PATH}" ]; then mkdir -p "${FTP_DATA_PATH}"; fi" 
        ssh root@${ftp_host} "if [ ! -x "${OBJECT_DIR}" ]; then mkdir "${OBJECT_DIR}"; fi" 
        rsync -rvl ${OBJECT_DIR}/ftp   root@${ftp_host}:${OBJECT_DIR}  >/dev/null
        ssh root@${ftp_host}  "chmod -R 755   ${OBJECT_DIR}/ftp"
        echo "${ftp_host}上分发ftp完毕......"  | tee  -a  $LOG_FILE
    done 
    
    echo "配置完毕......"  | tee  -a  $LOG_FILE
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
    config_cluster_over_ftp
    config_ftpAddress
    config_rkmq
    config_pdcrOverFtp
    config_users
    distribute_ftp
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
echo "开始配置ftp中的conf文件"                       | tee  -a  $LOG_FILE
main

set +x
