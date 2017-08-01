package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceImpl implements DynamicPhotoService {
    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceImpl.class);

    /**
     * 将rowKey、特征值插入人脸/车辆特征库 （内）（刘思阳）
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertePictureFeature(PictureType type, String rowKey, float[] feature) {

        if (null != rowKey && type == PictureType.PERSON) {
            Table personFeature = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.PERFEA_COLUMNFAMILY, DynamicTable.PERFEA_COLUMN_FEA, Bytes.toBytes(featureStr));
                personFeature.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_person failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(personFeature);
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            Table carFeature = HBaseHelper.getTable(DynamicTable.TABLE_CARFEA);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.CARFEA_COLUMNFAMILY, DynamicTable.CARFEA_COLUMN_FEA, Bytes.toBytes(featureStr));
                carFeature.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("inserte feature by rowkey from table_car failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(carFeature);
            }
        }else{
            LOG.error("method DynamicPhotoServiceImpl.insertePictureFeature param is empty.");
        }
        return false;
    }
}
