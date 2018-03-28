package com.hzgc.service.util;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HBaseUtil {
    private static Logger LOG = Logger.getLogger(HBaseUtil.class);

    public static void closTable(Table table) {
        if (null != table) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ObjectSearchResult dealWithPaging(ObjectSearchResult objectSearchResult, int start, int pageSize){
        // 分页处理
        //处理搜索的数据,根据是否需要分页进行返回
        List<Map<String, Object>> objectResult;
        List<Map<String, Object>> objectMap = objectSearchResult.getResults();
        if (objectMap != null && start != -1 && pageSize != -1 && start <= objectMap.size()){
            if ((start + pageSize - 1) > objectMap.size()){
                objectResult = objectMap.subList(start - 1, objectMap.size());
            }else {
                objectResult = objectMap.subList(start - 1, pageSize + start - 1);
            }
            objectSearchResult.setResults(objectResult);
        }
        return objectSearchResult;
    }
}
