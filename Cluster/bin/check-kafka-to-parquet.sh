#!bin/bash
###########################################################
##Copyright:       HZGOSUN Tech. Co. BigData
##Filename:        restart-kafka-to-parquet.sh
##Description:     to restart kafkaToParquet
##Verson:          1.5.0
##Author:          chenke
##Created:         2017-11-16
###########################################################
#opt目录
#set -x
cd /opt
CKTOTALINFOR="/opt/cktot.txt"
CKAPPINFOR="/opt/ckapp.txt"
chmod 777 /opt/RealTimeFaceCompare/cluster/bin/check-kafka-to-parquet.sh
#判断信息文件是否存在
if [ ! -f "$CKTOTALINFOR" ];then
    touch "$CKTOTALINFOR"
fi
#判断application文件是否存在
if [ ! -f "$CKAPPINFOR" ];then
    touch "$CKAPPINFOR"
fi
#获取kafkatoparquet的application编号
yarn application -list | grep 'FaceObject*' > $CKTOTALINFOR
cut -b 1-30 $CKTOTALINFOR > $CKAPPINFOR
APP=`cat $CKAPPINFOR`
echo ${APP}
if [ ! -s $CKAPPINFOR ];then
   sh /opt/RealTimeFaceCompare/cluster/bin/start-kafka-to-parquet.sh
fi