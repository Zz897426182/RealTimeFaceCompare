#!/bin/bash
##  动态抓拍总数映射
curl -XDELETE 's103:9200/dynamicshow?pretty'  -H 'Content-Type: application/json'
curl -XPUT 's103:9200/dynamicshow?pretty' -H 'Content-Type: application/json' -d'
{
    "settings": {
	    "number_of_shards":5,
        "number_of_replicas":1,
        "analysis": {
            "filter": {
                "trigrams_filter": {
                    "type":     "ngram",
                    "min_gram": 2,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "trigrams": {
                    "type":      "custom",
                    "tokenizer": "standard",
                    "filter":   [
                        "lowercase",
                        "trigrams_filter"
                    ]
                },
                "ik": {
                    "tokenizer" : "ik_max_word"
                }
            }
        }
    },
    "mappings": {
         "person": {
              "properties": {
                    "ipcid" : {
                                    "type" : "text"
                              },
                              "time" : {
                                    "type" : "keyword"
                               },
                              "count" : {
                                    "type" : "long"
                              }
                    }
              }
        }
    }
'

curl -XPUT 's103:9200/dynamicshow/_settings' -d '{
    "index": {
        "max_result_window": 1000000000
    }
}'

