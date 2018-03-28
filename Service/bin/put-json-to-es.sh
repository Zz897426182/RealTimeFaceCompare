#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    put-json-to-es.sh
## Description: to spilt json to put data to es
## Version:     1.0
## Author:      chenke
## Created:     2017-09-01
################################################################################

cd `dirname $0`
declare -r BIN_DIR=`pwd`    ### bin目录


function ensure_correct_use()
{
    if [ "$#" != "2" ];then
        echo "$(date "+%Y-%m-%d %H:%M:%S ") ########################################################################################"
        echo "$(date "+%Y-%m-%d %H:%M:%S ") Usage: sh bulk-data-to-es.sh json_fiel_parent_path log_file(ex: sh  bulk-data-to-es.sh /opt/json bulk-data-to-es.log)"
        exit 0
   fi
}

ensure_correct_use $@
declare -r JSON_FILE_PARENT_PATH=$1
declare -r JSON_SPILIT_PATH=$BIN_DIR/json_spilit
if [ ! -d $JSON_SPILIT_PATH ];then
    echo  "$(date "+%Y-%m-%d %H:%M:%S ") mkdir of $JSON_SPILIT_PATH "
    mkdir $JSON_SPILIT_PATH
fi

##对文件进行切分，切分成1万条数据每个文件
for file in $(ls $JSON_FILE_PARENT_PATH | egrep *.json);do
    echo "$(date "+%Y-%m-%d %H:%M:%S ") -------------------------------------------------------------"
    num_of_count=$(cat ${JSON_FILE_PARENT_PATH}${file}|wc -l)
    echo "$(date "+%Y-%m-%d %H:%M:%S ") total count of ${JSON_FILE_PARENT_PATH}$file is $num_of_count"
    num_of_pages=$(($num_of_count/20000))
    echo "$(date "+%Y-%m-%d %H:%M:%S ") saparate into $num_of_pages part, 多出部分特殊处理。"
    for((i=1;i<=$num_of_pages;i++));do
      start=$((($i-1)*20000+1))
      end=$(($i*20000))
      sed -n "${start},${end}p" ${JSON_FILE_PARENT_PATH}${file} > ${JSON_SPILIT_PATH}/park_${i}_${file}
    done
    if [ $num_of_count -gt $((num_of_pages*20000)) ];then
      start=$((num_of_pages*20000+1))
      end=$num_of_count
      part_count=$((num_of_pages + 1))
      sed -n "${start},${end}p" ${JSON_FILE_PARENT_PATH}${file} > ${JSON_SPILIT_PATH}/park_${part_count}_${file}
    fi
done

##循环遍历文件，把数据导入到Es 中
echo "$(date "+%Y-%m-%d %H:%M:%S ") -------------------------------------------------------------"
echo "$(date "+%Y-%m-%d %H:%M:%S ") bulk data to the es.........................................."
for file in $(ls $JSON_SPILIT_PATH);do
    curl 172.18.18.100:9200/_bulk?pretty --data-binary @${JSON_SPILIT_PATH}/${file}
done


