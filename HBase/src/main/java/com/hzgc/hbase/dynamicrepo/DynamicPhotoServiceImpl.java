package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceImpl implements DynamicPhotoService {
    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceImpl.class);

    /**
     * 将rowKey、特征值插入人脸/车辆库 （内）（刘思阳）
     * 表名：person/car
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertPictureFeature(PictureType type, String rowKey, float[] feature) {

        if (null != rowKey && type == PictureType.PERSON) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                person.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_person failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(person);
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                car.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_car failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(car);
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.insertePictureFeature param is empty.");
        }
        return false;
    }

    /**
     * 根据小图rowKey获取小图特征值 （内）（刘思阳）
     * 表名：person/car
     *
     * @param imageId 小图rowKey
     * @param type    人/车
     * @return byte[] 小图特征值
     */
    @Override
    public byte[] getFeature(String imageId, PictureType type) {
        byte[] feature = null;
        if (null != imageId) {
            Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            Get get = new Get(Bytes.toBytes(imageId));
            if (type == PictureType.PERSON) {
                try {
                    Result result = personTable.get(get);
                    feature = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_person failed! used method FilterByRowkey.getSmallImage");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            } else if (type == PictureType.CAR) {
                try {
                    Result result = carTable.get(get);
                    feature = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_car failed! used method FilterByRowkey.getSmallImage");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            }
        } else {
            LOG.error("method FilterByRowkey.getFeature param is empty");
        }
        return feature;
    }

    /**
     * 将上传的图片、rowKey、特征值插入人脸/车辆特征库 （内）
     * 表名：upFea
     *
     * @param type    人/车
     * @param rowKey  上传图片ID（rowKey）
     * @param feature 特征值
     * @param image   图片
     * @return boolean 是否插入成功
     */
    @Override
    public boolean upPictureInsert(PictureType type, String rowKey, float[] feature, byte[] image) {
        Table table = HBaseHelper.getTable(DynamicTable.TABLE_UPFEA);
        if (null != rowKey && type == PictureType.PERSON) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                byte[] imageData = image;
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_SMALLIMAGE, Bytes.toBytes(image.toString()));
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_person failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(table);
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                byte[] imageData = image;
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_SMALLIMAGE, Bytes.toBytes(image.toString()));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_car failed! used method DynamicPhotoServiceImpl.upPictureInsert.");
            } finally {
                HBaseUtil.closTable(table);
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.upPictureInsert param is empty.");
        }
        return false;
    }

    /**
     * 将查询ID、查询相关信息插入查询结果库 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchId     查询ID（rowKey）
     * @param queryImageId 查询图片ID
     * @param resList      查询信息（返回图片ID、相识度）
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertSearchRes(String searchId, String queryImageId, Map<String, Float> resList) {
        Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        try {
            Put put = new Put(Bytes.toBytes(searchId));
            put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHIMAGEID, Bytes.toBytes(queryImageId));
            byte[] searchMessage = ObjectUtil.objectToByte((Object) resList);
            put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE, searchMessage);
            searchRes.put(put);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("inserte data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.insertSearchRes.");
        } finally {
            HBaseUtil.closTable(searchRes);
        }
        return false;
    }

    /**
     * 根据动态库查询ID获取查询结果 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchID 查询ID（rowKey）
     * @return List<Map<String,Float>> search结果数据列表
     */
    @Override
    public Map<String, Float> getSearchRes(String searchID) {
        Map<String, Float> searchMessageMap = new HashMap<>();
        Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Get get = new Get(Bytes.toBytes(searchID));
        try {
            Result result = searchRes.get(get);
            byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
            searchMessageMap = (Map<String, Float>) ObjectUtil.byteToObject(searchMessage);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("get data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.getSearchRes.");
        } finally {
            HBaseUtil.closTable(searchRes);
        }
        return searchMessageMap;
    }

}
