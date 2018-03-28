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
TOTALINFOR="/opt/tot.txt"
APPINFOR="/opt/app.txt"
chmod 777 /opt/RealTimeFaceCompare/cluster/bin/restart-kafka-to-parquet.sh
#判断信息文件是否存在
if [ ! -f "$TOTALINFOR" ];then
    touch "$TOTALINFOR"
fi
#判断application文件是否存在
if [ ! -f "$APPINFOR" ];then
    touch "$APPINFOR"
fi
#获取kafkatoparquet的application编号
yarn application -list | grep 'FaceObject*' > $TOTALINFOR
cut -b 1-30 $TOTALINFOR > $APPINFOR
APP=`cat $APPINFOR`
echo ${APP}
if [ ! -s $APPINFOR ];then
   sh /opt/RealTimeFaceCompare/cluster/bin/start-kafka-to-parquet.sh
else
  yarn application -kill $APP
  sh /opt/RealTimeFaceCompare/cluster/bin/start-kafka-to-parquet.sh
fi
