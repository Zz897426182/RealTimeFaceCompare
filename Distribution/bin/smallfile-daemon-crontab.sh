
#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-merge-parquet-file.sh
## Description: 1,配置合并小文件服务，并且启动服务
##              2,配置监听大数据ftp 和dubbo 服务 的定时任务
## Version:     1.0
## Author:      lidiliang
## Created:     2017-11-17
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉


#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
source /etc/profile
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 模块根目录
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录


cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r CLUSTER_DIR=${BIGDATA_SERVICE_DIR}/cluster

if [ -f $LOG_DIR/config-parquet ];then
    echo "已经配置过，请检查/etc/crontab,    exit with 0"
    exit 0
fi
mkdir -p ${LOG_DIR}
echo "0,15,30,45 * * * * root  ${CLUSTER_DIR}/bin/schema-merge-parquet-file.sh"  >> /etc/crontab
echo "5 */1 * * * root  ${CLUSTER_DIR}/bin/schema-merge-final-table.sh"   >> /etc/crontab
echo "10 3 * * * root ${CLUSTER_DIR}/bin/schema-merge-final-table-crash.sh"  >> /etc/crontab
echo "*/5  * * * * root  ${COMMMON_DIR}/bin/facecomp-daemon.sh"  >> /etc/crontab
echo config-parquet > $LOG_DIR/config-parquet
echo "restart crond service"
service crond restart
echo "config done." 
