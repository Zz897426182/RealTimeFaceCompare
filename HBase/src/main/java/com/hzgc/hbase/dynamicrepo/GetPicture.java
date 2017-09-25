package com.hzgc.hbase.dynamicrepo;


import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.Serializable;


public class GetPicture implements Serializable {

    private static Logger LOG = Logger.getLogger(GetPicture.class);
    public byte[] getCapture(String imageId) {
        if (null != imageId) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            try {
                Get get = new Get(Bytes.toBytes(imageId));
                Result result = person.get(get);
                if (result != null) {
                    byte[] smallImage = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    return smallImage;
                } else {
                    LOG.error("get Result form table_person is null!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("get getCapture by rowkey from table_person failed!");
            } finally {
                HBaseUtil.closTable(person);
            }
        }
        return null;
    }

}
