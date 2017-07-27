package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.jni.FaceFunction;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceImpl implements DynamicPhotoService {
    private static Logger LOGGER = Logger.getLogger(DynamicPhotoServiceImpl.class);

    /**
     * 将rowKey、特征值插入人脸/车辆特征库 （内）（刘思阳）
     *
     * @param type    图片类型（判断是车是人）
     * @param rowKey  图片id
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertePictureFeature(PictureType type, String rowKey, float[] feature) {
        try {
            if (null != rowKey && type.getType() == 0) {
                String featureStr = FaceFunction.floatArray2string(feature);
                Table personFeature = HBaseHelper.getTable("perFea");
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("fea"), Bytes.toBytes(featureStr));
                personFeature.put(put);

                return true;
            } else if (null != rowKey && type.getType() == 1) {
                String featureStr = FaceFunction.floatArray2string(feature);
                Table personFeature = HBaseHelper.getTable("carFea");
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("fea"), Bytes.toBytes(featureStr));
                personFeature.put(put);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
