#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    change-data-featuretype.sh
## Description: 根据pkey改变特征值类型，并更新到HBase和ES中
## Version:     1.0
## Author:      caodabao
## Created:     2017-1-3
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
#set -x
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf                                                                        ### 项目根目录
LIB_DIR=$DEPLOY_DIR/lib                                                                          ### Jar 包目录
cd ..
REALTIMEFACECOMPARE_DIR=`pwd`
COMMON_LIB=${REALTIMEFACECOMPARE_DIR}/common/lib
LIB_JARS=`ls $LIB_DIR|grep .jar| grep -v avro-ipc-1.7.7-tests.jar | grep -v avro-ipc-1.7.7.jar | grep -v .bak \
| grep -v spark-network-common_2.10-1.5.1.jar| grep -v service \
 |awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`                                                     ### jar包位置以及第三方依赖jar包，绝对路径
COMMON_LIB_JARS=`ls $COMMON_LIB |grep .jar |grep -v .bak | awk '{print "'$COMMON_LIB'/"$0}'|tr "\n" ":"`
LOG_DIR=${DEPLOY_DIR}/logs                                                                       ### log 日记目录
if [ $# != 2 ]; then
    echo "sh ***.sh pkey  ***.log";
    exit 0;
fi
PKEY=$1
LOG_FILE_NAME=$2

LOG_FILE=${LOG_DIR}/${LOG_FILE_NAME}      ##  log 日记文件


#####################################################################
# 函数名: change_feature
# 描述: 根据pkey改变特征值类型，并更新到HBase和ES中
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function change_feature()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    java -classpath $CONF_DIR:$LIB_JARS:$COMMON_LIB_JARS com.hzgc.service.staticrepo.ChangeFeatureByPkey  $PKEY  | tee -a  ${LOG_FILE}
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
    change_feature
}

## 脚本主要业务入口
main
