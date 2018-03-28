#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-serviceconf
## Description: 配置项目service中的conf配置文件
## Version:     1.0
## Author:      caodabao
## Created:     2017-11-28 
################################################################################

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
CONF_DIR=$OBJECT_DIR/common/conf/project-conf.properties   ### 项目配置文件
cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                   ### 集群配置文件目录

## 最终安装的根目录，所有bigdata 相关的根目录
INSTALL_HOME=$(grep Install_HomeDir $CONF_DIR |cut -d '=' -f2)
HADOOP_INSTALL_HOME=${INSTALL_HOME}/Hadoop            ### hadoop 安装目录
HADOOP_HOME=${HADOOP_INSTALL_HOME}/hadoop             ### hadoop 根目录
HBASE_INSTALL_HOME=${INSTALL_HOME}/HBase              ### hbase 安装目录
HBASE_HOME=${HBASE_INSTALL_HOME}/hbase                ### hbase 根目录


#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#

#####################################################################
# 函数名: move_xml
# 描述: 配置Hbase服务，移动所需文件到cluster/conf下
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function move_xml()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "copy 文件 hbase-site.xml core-site.xml hdfs-site.xml 到 cluster/conf......"  | tee  -a  $LOG_FILE

    cp ${HBASE_HOME}/conf/hbase-site.xml ${CONF_SERVICE_DIR}
    cp ${HADOOP_HOME}/etc/hadoop/core-site.xml ${CONF_SERVICE_DIR}
    cp ${HADOOP_HOME}/etc/hadoop/hdfs-site.xml ${CONF_SERVICE_DIR}
    
    echo "copy完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: config_es
# 描述: 配置es-config.properties的es.hosts
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_es()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置service/conf/es-config.properties......"  | tee  -a  $LOG_FILE

    # 配置es.hosts：
    # 从project-conf.properties读取es所需配置IP
    # 根据字段es，查找配置文件，这些值以分号分割
    ES_IP=$(grep es_servicenode ${CONF_DIR} | cut -d '=' -f2)
    # 将这些分号分割的ip用放入数组
    es_arr=(${ES_IP//;/ })
    espro=''    
    for es_host in ${es_arr[@]}
    do
        espro="$espro$es_host,"
    done
    espro=${espro%?}
    
    # 替换es-config.properties中：key=value（替换key字段的值value）
    sed -i "s#^es.hosts=.*#es.hosts=${espro}#g" ${DEPLOY_DIR}/conf/es-config.properties
    echo "es-config.properties配置es完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: configzk_dubbo
# 描述: 配置dubbo.properties的zk地址
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function configzk_dubbo()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置service/conf/dubbo.properties......"  | tee  -a  $LOG_FILE

    #配置dubbo.registry.address为(e.x)：
    #zookeeper://172.18.18.106:2181?backup=172.18.18.107:2181,172.18.18.108:2181
    ZK_HOSTS=$(grep zookeeper_installnode ${CONF_DIR} | cut -d '=' -f2)
    zk_arr=(${ZK_HOSTS//;/ }) 
    ZK_HOST=''
    ZK_HOST1=''
    ZK_HOST2=''
    ZKMASTER=${zk_arr[0]}
    for zk_host in ${zk_arr[@]}
    do
        if [ "$zk_host" = "$ZKMASTER" ];then
            ZK_HOST1="zookeeper://$ZK_HOST1$zk_host:2181?backup="
        else
            ZK_HOST2="$ZK_HOST2$zk_host:2181,"
        fi
    done
    ZK_HOST="$ZK_HOST$ZK_HOST1${ZK_HOST2%?}"
    sed -i "s#^dubbo.registry.address=.*#dubbo.registry.address=${ZK_HOST}#g" ${DEPLOY_DIR}/conf/dubbo.properties
    echo "dubbo.properties配置zk完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: config_ftphost
# 描述: 配置文件：service/conf/ftp-hostnames.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_ftphost()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置service/conf/ftp-hostnames.properties......"  | tee  -a  $LOG_FILE

    echo "" > ${CONF_SERVICE_DIR}/ftp-hostnames.properties
    ##FTP服务节点主机名
    FTP_HOSTS=$(grep ftp_servicenode ${CONF_DIR} | cut -d '=' -f2)
    ftph_arr=(${FTP_HOSTS//;/ }) 
    for ftp_host in ${ftph_arr[@]}
    do
        echo "${ftp_host}" >> ${CONF_SERVICE_DIR}/ftp-hostnames.properties
    done
    sed -i '1d' ${CONF_SERVICE_DIR}/ftp-hostnames.properties    
    echo "配置ftp-hostnames.properties完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: config_jdbc
# 描述: 配置service/conf/jdbc.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_jdbc()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "配置service/conf/jdbc.properties......"  | tee  -a  $LOG_FILE
    ##jdbc节点IP
    JDBC_IPS=$(grep jdbc_servicenode ${CONF_DIR} | cut -d '=' -f2)
    jdbc_arr=(${JDBC_IPS//;/ })
    jdbc_ips=''    
    for jdbc_ip in ${jdbc_arr[@]}
    do
        jdbc_ips="$jdbc_ips$jdbc_ip:2181,"
    done
    JDBC="jdbc:hive2://${jdbc_ips%?}/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=thriftserver"
    sed -i "s#^url=.*#url=${JDBC}#g"  ${CONF_SERVICE_DIR}/jdbc.properties
    
    echo "配置jdbc.properties完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: config_dubbo
# 描述: 分发并配置每个节点下的service/conf/dubbo.properties
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function config_dubbo()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "分发service................."  | tee  -a  $LOG_FILE
    ## 获取dubbo节点IP
    DUBBO_HOSTS=$(grep dubbo_servicenode ${CONF_DIR} | cut -d '=' -f2)
    dubbo_arr=(${DUBBO_HOSTS//;/ }) 
    for dubbo_host in ${dubbo_arr[@]}
    do
        ssh root@${dubbo_host}  "mkdir -p ${OBJECT_DIR}"  
        rsync -rvl ${OBJECT_DIR}/service   root@${dubbo_host}:${OBJECT_DIR}  >/dev/null
        ssh root@${dubbo_host}  "chmod -R 755   ${OBJECT_DIR}/service"
    done 
    arr=(${DUBBO_HOSTS//;/ })
    for dubbohost in ${arr[@]}
    do
        ssh root@${dubbohost} "sed -i \"s#^dubbo.protocol.host=.*#dubbo.protocol.host=${dubbohost}#g\"  ${OBJECT_DIR}/service/conf/dubbo.properties"
    done
    echo "配置dubbo.properties完毕......"  | tee  -a  $LOG_FILE
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
    move_xml
    configzk_dubbo
    config_ftphost
    config_es
    config_jdbc
    config_dubbo
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
echo "开始配置service中的conf文件"                       | tee  -a  $LOG_FILE
main

set +x
