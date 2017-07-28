package com.hzgc.hbase.dynamicrepo;


import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 以图搜图接口实现类，内含四个方法（外）（彭聪）
 */
public class CapturePictureSearchServiceImpl implements CapturePictureSearchService {
    private static Logger LOG = Logger.getLogger(CapturePictureSearchServiceImpl.class);

    @Override
    public SearchResult search(SearchOption option) {
        SearchResult searchResult = new SearchResult();
        Scan scan = new Scan();

        if (null != option) {
            if (!option.getDeviceIds().isEmpty() && null != option.getSearchType()) {
                List<String> rowKeyList = new FilterByRowkey().filterByDeviceId(option, scan);

            }
        }

        return null;
    }

    @Override
    public SearchResult getSearchResult(String searchId, int offset, int count) {
        return null;
    }

    @Override
    public Map<String, String> getSearchFilterParams(int type) {
        return null;
    }

    /**
     * 根据id（rowkey）获取原图 （刘思阳）
     *
     * @param imageId rowkey
     * @param type    图片类型，人/车
     * @return 以二进制数组的形式返回图片
     */
    @Override
    public byte[] getPicture(String imageId, PictureType type) {
        byte[] picture = null;
        if (null != imageId && type == PictureType.PERSON) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
            try {
                Get get = new Get(Bytes.toBytes(imageId));
                Result result = person.get(get);
                picture = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(person);
                LOG.error("get picture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getPicture.");
            }
        } else if (null != imageId && type == PictureType.CAR) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                Get get = new Get(Bytes.toBytes(imageId));
                Result result = car.get(get);
                picture = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("get picture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getPicture.");
            } finally {
                HBaseUtil.closTable(car);
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getPicture param is empty.");
        }
        return picture;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowkey）
     * @param type    图片类型，人/车
     * @return DynamicObject    动态库对象
     */
    @Override
    public DynamicObject getCaptureMessage(String imageId, int type) {
        DynamicObject dynamicObject = new DynamicObject();
        dynamicObject.setImageId(imageId);

        Map<String, String> map = FtpUtil.getRowKeyMessage(imageId);
        String ipcID = map.get("ipcID");
        String timeStampStr = map.get("time");

        dynamicObject.setIpc(ipcID);
        dynamicObject.setTimeStamp(Long.valueOf(timeStampStr));

        if (null != imageId && PictureType.PERSON.equals(type) || PictureType.CAR.equals(type)) {
            if (PictureType.PERSON.equals(type)) {
                Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);

                    byte[] image = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    dynamicObject.setImage(image);

                    String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
                    dynamicObject.setDes(des);

                    String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
                    dynamicObject.setEx(ex);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get DynamicObject by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.");
                } finally {
                    HBaseUtil.closTable(person);
                }
            } else if (PictureType.CAR.equals(type)) {
                Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = car.get(get);

                    byte[] image = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
                    dynamicObject.setImage(image);

                    String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
                    dynamicObject.setDes(des);

                    String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
                    dynamicObject.setEx(ex);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get DynamicObject by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.");
                } finally {
                    HBaseUtil.closTable(car);
                }
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage param is empty.");
        }
        return dynamicObject;
    }
}
