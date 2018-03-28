package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.dubbo.staticrepo.PSearchArgsModel;
import com.hzgc.dubbo.staticrepo.SearchRecordHandler;
import com.hzgc.dubbo.staticrepo.SrecordTable;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.*;

public class SearchRecordHandlerImpl implements SearchRecordHandler {
    private static Logger LOG = Logger.getLogger(SearchRecordHandlerImpl.class);

    /**
     * 处理流程，根据rowkey 取得当时的搜索条件，然后重新想数据库房一次请求
     * @param rowkey，即Hbase 数据库中的rowkey，查询记录唯一标志
     * @param from  返回的查询记录中，从哪一条开始
     * @param size  需要返回的记录数
     * @return
     */
    @Override
    public ObjectSearchResult getRocordOfObjectInfo(String rowkey,int from,int size) {
        ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
        Table table = HBaseHelper.getTable(SrecordTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        PSearchArgsModel pSearchArgsModel = null;
        Result result = null;
        try {
            result = table.get(get);
        } catch (IOException e) {
            LOG.error("get data by rowkey from srecord table failed! used method getRocordOfObjectInfo.");
            e.printStackTrace();
        }
        byte[] searchModelByte = null;
        if (result != null) {
           searchModelByte = result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PSEARCH_MODEL));
        }
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(searchModelByte));
            pSearchArgsModel = (PSearchArgsModel) ois.readObject();
            pSearchArgsModel.setStart(from);
            pSearchArgsModel.setPageSize(size);
            objectSearchResult = objectInfoHandler.getObjectInfo(pSearchArgsModel);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return objectSearchResult;
    }

    @Override
    public byte[] getSearchPhoto(String rowkey) {
        Table table = HBaseHelper.getTable("srecord");
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result = null;
        try {
            result = table.get(get);
        } catch (IOException e) {
            LOG.error("get data by rowkey from srecord table failed! used method getSearchPhoto.");
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(table);
        }
        return result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTO));
    }
}
