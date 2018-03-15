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
}
