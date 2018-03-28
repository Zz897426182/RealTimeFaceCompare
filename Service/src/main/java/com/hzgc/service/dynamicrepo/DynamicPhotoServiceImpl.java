package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.util.List;

import static com.hzgc.util.common.ObjectUtil.objectToByte;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceImpl implements DynamicPhotoService {
    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceImpl.class);

    /**
     * 将上传的图片、rowKey、特征值插入upFea特征库 （彭聪）
     * 表名：upFea
     *
     * @param type    人/车
     * @param rowKey  上传图片ID（rowKey）
     * @param feature 特征值
     * @param image   图片
     * @return boolean 是否插入成功
     */
    @Override
    public boolean upPictureInsert(SearchType type, String rowKey, float[] feature, byte[] image) {
        Table table = HBaseHelper.getTable(DynamicTable.TABLE_UPFEA);
        if (null != rowKey && type == SearchType.PERSON) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.setDurability(Durability.SKIP_WAL);
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_SMALLIMAGE, image);
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_person failed! used method DynamicPhotoServiceImpl.insertPictureFeature.");
            } finally {
                HBaseUtil.closTable(table);
            }
        } else if (null != rowKey && type == SearchType.CAR) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.setDurability(Durability.SKIP_WAL);
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_SMALLIMAGE, image);
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_car failed! used method DynamicPhotoServiceImpl.upPictureInsert.");
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
     * @param searchId            查询ID（rowKey）
     * @param capturedPictureList 查询信息（返回图片ID、相识度）
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertSearchRes(String searchId, List<CapturedPicture> capturedPictureList, String insertType) {
        if (searchId != null && !capturedPictureList.isEmpty()) {
            Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
            try {
                Put put = new Put(Bytes.toBytes(searchId));
                put.setDurability(Durability.ASYNC_WAL);
                byte[] searchMessage = objectToByte(capturedPictureList);
                put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE, searchMessage);
                put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHTYPE, Bytes.toBytes(insertType));
                searchRes.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.insertSearchRes.");
            } finally {
                HBaseUtil.closTable(searchRes);
            }
        }
        return false;
    }
}

