package com.hzgc.hbase.dynamicrepo;


import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.util.HBaseHelper;
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
    private static Logger LOGGER = Logger.getLogger(CapturePictureSearchServiceImpl.class);

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
     * 根据id（rowkey）获取原图
     *
     * @param imageId rowkey
     * @param type    图片类型，人还是车
     * @return 以二进制数组的形式返回图片
     */
    @Override
    public byte[] getPicture(String imageId, PictureType type) {
        byte[] picture = null;

        try {
            if (null != imageId && type.getType() == 0) {
                Table person = HBaseHelper.getTable("person");
                Get get = new Get(Bytes.toBytes(imageId));
                Result result = person.get(get);
                picture = result.getValue(Bytes.toBytes("i"), Bytes.toBytes("p"));
            } else if (null != imageId && type.getType() == 1) {
                Table person = HBaseHelper.getTable("car");
                Get get = new Get(Bytes.toBytes(imageId));
                Result result = person.get(get);
                picture = result.getValue(Bytes.toBytes("i"), Bytes.toBytes("p"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return picture;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（PersonPhoto对象）
     *
     * @param imageId rowkey
     * @param type    图片类型，人还是车
     * @return DynamicObject对象
     */
    @Override
    public DynamicObject getCaptureMessage(String imageId, int type) {
        DynamicObject dynamicObject  = new DynamicObject();
        dynamicObject.setImageId(imageId);

        Map<String, String> map = FtpUtil.getRowKeyMessage(imageId);
        String ipcID = map.get("ipcID");
        String timeStampStr = map.get("time");

        dynamicObject.setIpc(ipcID);
        dynamicObject.setTimeStamp(Long.valueOf(timeStampStr));

        if (null != imageId && type == 0 || type == 1) {
            try {
                if (type == 0) {
                    Table person = HBaseHelper.getTable("person");
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);

                    byte[] image = result.getValue(Bytes.toBytes("i"), Bytes.toBytes("p"));
                    dynamicObject.setImage(image);

                    String des = Bytes.toString(result.getValue(Bytes.toBytes("i"), Bytes.toBytes("d")));
                    dynamicObject.setDes(des);

                    String ex = Bytes.toString(result.getValue(Bytes.toBytes("i"), Bytes.toBytes("e")));
                    dynamicObject.setEx(ex);

                } else if (type == 1) {
                    Table person = HBaseHelper.getTable("car");
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);

                    byte[] image = result.getValue(Bytes.toBytes("i"), Bytes.toBytes("p"));
                    dynamicObject.setImage(image);

                    String des = Bytes.toString(result.getValue(Bytes.toBytes("i"), Bytes.toBytes("d")));
                    dynamicObject.setDes(des);

                    String ex = Bytes.toString(result.getValue(Bytes.toBytes("i"), Bytes.toBytes("e")));
                    dynamicObject.setEx(ex);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dynamicObject;
    }
}
