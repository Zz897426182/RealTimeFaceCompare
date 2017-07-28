package com.hzgc.hbase.dynamicrepo;


import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class FilterByRowkey {
    private static Logger LOG = Logger.getLogger(FilterByRowkey.class);

    public List<String> filterByDeviceId(SearchOption option, Scan scan) {
        SearchType searchType = option.getSearchType();
        List<String> deviceIdList = option.getDeviceIds();
        List<String> rowKeyList = null;

        if (searchType.equals(SearchType.PERSON)) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            for (String device : deviceIdList) {
                Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(".*" + device));
                scan.setFilter(filter);
                try {
                    ResultScanner scanner = person.getScanner(scan);
                    for (Result result : scanner) {
                        byte[] bytes = result.getRow();
                        String string = Bytes.toString(bytes);
                        rowKeyList.add(string);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("filter rowkey by deciceId from table_person failed! used method FilterByRowkey.filterByDeviceId.");
                } finally {
                    HBaseUtil.closTable(person);
                }
            }
        } else if (searchType.equals(SearchType.CAR)) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            for (String device : deviceIdList) {
                Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(".*" + device));
                scan.setFilter(filter);
                try {
                    ResultScanner scanner = car.getScanner(scan);
                    for (Result result : scanner) {
                        byte[] bytes = result.getRow();
                        String string = Bytes.toString(bytes);
                        rowKeyList.add(string);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("filter rowkey by deciceId from table_car failed! used method FilterByRowkey.filterByDeviceId.");
                } finally {
                    HBaseUtil.closTable(car);
                }
            }
        }
        return rowKeyList;
    }
}
