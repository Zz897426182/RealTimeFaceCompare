package com.hzgc.service.staticrepo.tablecreate;

import com.hzgc.service.util.HBaseHelper;
import com.hzgc.util.common.FileUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CreateSrecordTable {
    private static Logger LOG = Logger.getLogger(CreateSrecordTable.class);

    public static void main(String[] args) {
        // 如果传入的参数不为空
        InputStream inputStream = null;
        File srecPro = FileUtil.loadResourceFile("srecord-table.properties");

        // 如果表已经存在，直接返回
        try {
            Properties prop = new Properties();
            if (srecPro == null){
                return;
            }
            inputStream = new FileInputStream(srecPro);
            prop.load(inputStream);
            String tableName =  prop.getProperty("table.srecord.name");
            String colfamsString =  prop.getProperty("table.srecord.colfams");
            String maxVersion = prop.getProperty("table.srecord.maxversion");
            String timetToLive = prop.getProperty("table.sercord.timetolive");
            String[] colfams = colfamsString.split("-");
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))){
                LOG.error("表格:" + tableName + "已经存在，请进行确认是否删除表格，需要手动到HBase 客户端删除表格。");
                HBaseHelper.closeInnerHbaseConn();
                return;
            }
            if (timetToLive != null){
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
                HBaseHelper.closeInnerHbaseConn();
            }else {
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), colfams);
                HBaseHelper.closeInnerHbaseConn();
            }
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                HBaseHelper.closeInnerHbaseConn();
                LOG.info("create table " + tableName + " success..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
