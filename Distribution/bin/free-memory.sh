#! /bin/sh
#set -x
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
LOG_DIR=${DEPLOY_DIR}/logs
LOG_FILE=${LOG_DIR}/free-memory.log
if [ ! -d $LOG_DIR ];then
   mkdir $LOG_DIR
fi
if [ ! -x $LOG_FILE ]; then
 touch "$LOG_FILE"
fi

used=`free -m | awk 'NR==2' | awk '{print $3}'`
free=`free -m | awk 'NR==2' | awk '{print $4}'`
echo "===========================" >> $LOG_FILE
date >> $LOG_FILE
echo "Memory usage before | [Use：${used}MB][Free：${free}MB]" >> $LOG_FILE
if [ $free -le 20000 ] ; then
                sync && echo 1 > /proc/sys/vm/drop_caches
                sync && echo 2 > /proc/sys/vm/drop_caches
                sync && echo 3 > /proc/sys/vm/drop_caches
				used_ok=`free -m | awk 'NR==2' | awk '{print $3}'`
				free_ok=`free -m | awk 'NR==2' | awk '{print $4}'`
				echo "Memory usage after | [Use：${used_ok}MB][Free：${free_ok}MB]" >> $LOG_FILE
                echo "OK" >> $LOG_FILE
else
                echo "Not required" >> $LOG_FILE
fi
exit 1
