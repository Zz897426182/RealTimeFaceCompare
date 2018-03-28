package com.hzgc.service.staticrepo.tablecreate;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.util.common.FileUtil;
import com.hzgc.util.common.IOUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CreateStaticRepoTable {
    private static Logger LOG = Logger.getLogger(CreateStaticRepoTable.class);


    public static void main(String[] args) {
        Properties tableProper = new Properties();
        InputStream inputStream = null;
        try {
            File file = FileUtil.loadResourceFile("static-table.properties");
            if (file == null){
                return;
            }
            inputStream = new FileInputStream(file);
            tableProper.load(inputStream);
            String tableName = tableProper.getProperty("table.name");
            String colfamsString = tableProper.getProperty("table.colfams");
            String maxVersion = tableProper.getProperty("table.maxversion");
            String timetToLive = tableProper.getProperty("table.timetolive");
            String[] colfams = colfamsString.split("-");
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                LOG.error("表格:" + tableName + "已经存在，请进行确认是否删除表格，需要手动到HBase 客户端删除表格。");
                HBaseHelper.closeInnerHbaseConn();
                return;
            }
            if (timetToLive != null) {
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
            } else {
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), colfams);
            }

            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                HBaseHelper.closeInnerHbaseConn();
                LOG.info("create table " + tableName + "success..");
            }

            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
                Put put = new Put(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS),
                        Bytes.toBytes(Long.MIN_VALUE));
                table.put(put);
                table.close();
                HBaseHelper.closeInnerHbaseConn();
                LOG.info("====================== add a column for " + tableName + " success.. ==================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(inputStream);
        }
    }
}
