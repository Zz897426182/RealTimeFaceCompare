package com.hzgc.hbase.dynamicrepo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.DateUtil;
import com.hzgc.util.ListSplitUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.hzgc.util.ObjectUtil.byteToObject;
import static com.hzgc.util.ObjectUtil.objectToByte;

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
            Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.setDurability(Durability.SKIP_WAL);
                put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                personTable.put(put);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_person failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(personTable);
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.setDurability(Durability.SKIP_WAL);
                put.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                car.put(put);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_car failed! used method DynamicPhotoServiceImpl.insertPictureFeature.");
            } finally {
                HBaseUtil.closTable(car);
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.insertPictureFeature param is empty.");
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
            Get get = new Get(Bytes.toBytes(imageId));
            if (type == PictureType.SMALL_PERSON) {
                Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                try {
                    Result result = personTable.get(get);
                    if (result != null) {
                        feature = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA);
                    } else {
                        LOG.error("get Result from table_person is null! used method DynamicPhotoServiceImpl.getFeature.");
                    }
                } catch (IOException e) {
                    LOG.error("get feature by imageId from table_person failed! used method DynamicPhotoServiceImpl.getFeature");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            } else if (type == PictureType.SMALL_CAR) {
                Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                try {
                    Result result = carTable.get(get);
                    if (result != null) {
                        feature = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA);
                    } else {
                        LOG.error("get Result from table_car is null! used method DynamicPhotoServiceImpl.getFeature.");
                    }
                } catch (IOException e) {
                    LOG.error("get feature by imageId from table_car failed! used method DynamicPhotoServiceImpl.getFeature");
                } finally {
                    HBaseUtil.closTable(carTable);
                }
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.getFeature param is empty");
        }
        return feature;
    }

    /**
     * 批量获取特征值（彭聪）
     *
     * @param imageIdList 图片ID列表
     * @param type        查询类型
     * @return 特征值列表
     */
    @Override
    public List<float[]> getBatchFeature(List<String> imageIdList, PictureType type) {
        List<float[]> feaFloatList = new ArrayList<>();
        if (null != imageIdList && imageIdList.size() > 0) {
            List<Get> gets = new ArrayList<>();
            for (int i = 0, len = imageIdList.size(); i < len; i++) {
                if (imageIdList.get(i) != null) {
                    Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                    get.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"));
                    gets.add(get);
                }
            }
            if (type == PictureType.SMALL_PERSON) {
                Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                try {
                    Result[] results = personTable.get(gets);
                    if (results != null) {
                        for (Result result : results) {
                            if (result != null) {
                                float[] featureFloat = FaceFunction.byteArr2floatArr(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA));
                                feaFloatList.add(featureFloat);
                            } else {
                                feaFloatList.add(null);
                                LOG.error("get Result from table_car is null! used method DynamicPhotoServiceImpl.getBatchFeature.");
                            }
                        }
                    } else {
                        LOG.error("get Result[] from table_person is null! used method DynamicPhotoServiceImpl.getBatchFeature.");
                    }
                } catch (IOException e) {
                    LOG.info("get feature by imageId from table_person failed! used method DynamicPhotoServiceImpl.getBatchFeature");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            } else if (type == PictureType.SMALL_CAR) {
                Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                try {
                    Result[] results = carTable.get(gets);
                    if (results != null) {
                        for (Result result : results) {
                            if (result != null) {
                                float[] featureFloat = FaceFunction.byteArr2floatArr(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA));
                                feaFloatList.add(featureFloat);
                            } else {
                                feaFloatList.add(null);
                                LOG.info("get Result from table_car is null! used method DynamicPhotoServiceImpl.getBatchFeature.");
                            }
                        }
                    } else {
                        LOG.info("get Result[] from table_car is null! used method DynamicPhotoServiceImpl.getBatchFeature.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.info("get feature by imageId from table_car failed! used method DynamicPhotoServiceImpl.getBatchFeature");
                } finally {
                    HBaseUtil.closTable(carTable);
                }
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.getBatchFeature param is empty");
        }
        return feaFloatList;
    }

    /**
     * 批量多线程获取特征值（彭聪）
     *
     * @param imageIdList 图片id列表
     * @param type        图片类型
     * @return 特征值列表
     */
    @Override
    public List<float[]> getMultiBatchFeature(List<String> imageIdList, PictureType type) {
        //一般线程数设置为 （cpu（核数）+1）*线程处理时间，四核cpu （4+1）*2 = 10 （线程池数量）
        int parallel = (Runtime.getRuntime().availableProcessors() + 1) * 2;
        LOG.info("当前线程数：" + parallel);
        List<List<String>> lstBatchImageId;
        if (imageIdList.size() < parallel) {
            lstBatchImageId = new ArrayList<>(1);
            lstBatchImageId.add(imageIdList);
        } else {
            lstBatchImageId = new ArrayList<>(parallel);
            List<String> lst;
            for (int i = 0; i < parallel; i++) {
                lst = new ArrayList<>();
                lstBatchImageId.add(lst);
            }
            lstBatchImageId = ListSplitUtil.averageAssign(imageIdList, parallel);
        }
        List<Future<List<float[]>>> futures = new ArrayList<>(parallel);
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("ParallelBatchFeature");
        ThreadFactory factory = builder.build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(lstBatchImageId.size(), factory);

        for (List<String> keys : lstBatchImageId) {
            BatchFeaCallable callable = new BatchFeaCallable(keys, type);
            FutureTask<List<float[]>> future = (FutureTask<List<float[]>>) executor.submit(callable);
            futures.add(future);
        }
        executor.shutdown();

        // Wait for all the tasks to finish
        try {
            boolean stillRunning = !executor.awaitTermination(
                    10000, TimeUnit.MILLISECONDS);
            if (stillRunning) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        List<float[]> feaList = new ArrayList<>();
        // Look for any exception
        try {
            for (Future f : futures) {

                if (f.get() != null) {
                    feaList.addAll((List<float[]>) f.get());
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return feaList;
    }


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
    public boolean upPictureInsert(PictureType type, String rowKey, float[] feature, byte[] image) {
        Table table = HBaseHelper.getTable(DynamicTable.TABLE_UPFEA);
        if (null != rowKey && type == PictureType.SMALL_PERSON) {
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
        } else if (null != rowKey && type == PictureType.SMALL_CAR) {
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

    /**
     * 根据动态库查询ID获取查询结果 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchId 查询ID（rowKey）
     * @return 查询信息列表
     */
    @Override
    public Map<String, Float> getSearchRes(String searchId) {
        Map<String, Float> searchMessageMap = new HashMap<>();
        if (searchId != null) {
            Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
            Get get = new Get(Bytes.toBytes(searchId));
            try {
                Result result = searchRes.get(get);
                if (result != null) {
                    byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
                    searchMessageMap = (Map<String, Float>) byteToObject(searchMessage);
                } else {
                    LOG.error("get Result from table_searchRes is null! used method DynamicPhotoServiceImpl.getSearchRes.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("get data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.getSearchRes.");
            } finally {
                HBaseUtil.closTable(searchRes);
            }
        }
        return searchMessageMap;
    }

    /**
     * 根据id（rowKey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowKey）
     * @param type    图片类型，人/车
     * @return CapturedPicture
     */
    @Override
    public CapturedPicture getCaptureMessage(String imageId, int type) {
        CapturedPicture capturedPicture = new CapturedPicture();
        if (null != imageId) {
            capturedPicture.setId(imageId);
            Map<String, Object> mapEx = new HashMap<>();
            if (type == PictureType.PERSON.getType()) {
                Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);
                    setCapturedPicture_person(capturedPicture, result, mapEx);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    LOG.error("get CapturedPicture by rowkey from table_person failed! used method DynamicPhotoServiceImpl.getCaptureMessage.case 6");
                } finally {
                    HBaseUtil.closTable(person);
                }
            } else if (type == PictureType.CAR.getType()) {
                Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = car.get(get);
                    setCapturedPicture_car(capturedPicture, result, mapEx);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    LOG.error("get CapturedPicture by rowkey from table_car failed! used method DynamicPhotoServiceImpl.getCaptureMessage.case 7");
                } finally {
                    HBaseUtil.closTable(car);
                }
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.getCaptureMessage param is empty.");
        }
        return capturedPicture;
    }

    /**
     * 批量查询图片对象（彭聪）
     *
     * @param imageIdList 图片ID列表
     * @param type        搜索类型
     * @return List<CapturedPicture> 图片对象列表
     */
    @Override
    public List<CapturedPicture> getBatchCaptureMessage(List<String> imageIdList, int type) {
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        if (imageIdList != null) {
            List<Get> gets = new ArrayList<>();
            Map<String, Object> mapEx = new HashMap<>();
            CapturedPicture capturedPicture;

            if (type == PictureType.SMALL_PERSON.getType()) {
                Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                for (int i = 0, len = imageIdList.size(); i < len; i++) {
                    if (imageIdList.get(i) != null) {
                        Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                        get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID);
                        get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP);
                        get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE);
                        get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA);
                        gets.add(get);
                    }
                }
                Result[] results = null;
                try {
                    results = person.get(gets);
                    HBaseUtil.closTable(person);
                } catch (IOException e) {
                    LOG.info(e.getMessage());
                }
                if (results != null) {
                    for (int i = 0; i < results.length; i++) {
                        capturedPicture = new CapturedPicture();
                        if (results[i] != null) {
                            String rowKey = Bytes.toString(results[i].getRow());
                            capturedPicture.setId(rowKey);
                            capturedPicture.setPictureType(PictureType.SMALL_PERSON);
                            try {
                                setCapturedPicture_person(capturedPicture, results[i], mapEx);
                            } catch (ParseException e) {
                                LOG.info("Parse mapEx failed by setCapturedPicture_person");
                            }
                            capturedPictureList.add(capturedPicture);
                        } else {
                            LOG.error("get Result from table_person is null! used method DynamicPhotoServiceImpl.getBatchCaptureMessage.");
                        }
                    }
                } else {
                    LOG.error("get Result[] from table_person is null! used method DynamicPhotoServiceImpl.getBatchCaptureMessage.");
                }
            } else if (type == PictureType.SMALL_CAR.getType()) {
                Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                for (int i = 0, len = imageIdList.size(); i < len; i++) {
                    if (imageIdList.get(i) != null) {
                        Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IPCID);
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE);
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_TIMESTAMP);
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM);
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA);
                        gets.add(get);
                    }
                }
                Result[] results = null;
                try {
                    results = car.get(gets);
                    HBaseUtil.closTable(car);
                } catch (IOException e) {
                    LOG.info(e.getMessage());
                }

                if (results != null) {
                    for (Result result : results) {
                        capturedPicture = new CapturedPicture();
                        if (result != null) {
                            String rowKey = Bytes.toString(result.getRow());
                            capturedPicture.setId(rowKey);
                            capturedPicture.setPictureType(PictureType.SMALL_CAR);
                            try {
                                setCapturedPicture_car(capturedPicture, result, mapEx);
                            } catch (ParseException e) {
                                LOG.info(e.getMessage());
                            }
                            capturedPictureList.add(capturedPicture);
                        } else {
                            LOG.error("get Result from table_car is null! used method DynamicPhotoServiceImpl.getBatchCaptureMessage.");
                        }
                    }
                } else {
                    LOG.error("get Result[] from table_car is null! used method DynamicPhotoServiceImpl.getBatchCaptureMessage.");
                }
            }
        }
        return capturedPictureList;
    }

    /**
     * 多线程批量获取图片信息（彭聪）
     *
     * @param imageIdList 图片Id列表
     * @param type        图片类型
     * @return 图片对象列表
     */
    @Override
    public List<CapturedPicture> getMultiBatchCaptureMessage(List<String> imageIdList, int type) {
        //一般线程数设置为 （cpu（核数）+1）*线程处理时间，四核cpu （4+1）*5 = 20 （线程池数量）
        int parallel = (Runtime.getRuntime().availableProcessors() + 1) * 2;
        LOG.info("当前线程数：" + parallel);
        List<List<String>> lstBatchImageId;
        if (imageIdList.size() < parallel) {
            lstBatchImageId = new ArrayList<>(1);
            lstBatchImageId.add(imageIdList);
        } else {
            lstBatchImageId = new ArrayList<>(parallel);
            List<String> lst;
            for (int i = 0; i < parallel; i++) {
                lst = new ArrayList<>();
                lstBatchImageId.add(lst);
            }
            //将rowKey list 平均分成多个sublist
            lstBatchImageId = ListSplitUtil.averageAssign(imageIdList, parallel);
        }
        List<Future<List<CapturedPicture>>> futures = new ArrayList<>(parallel);
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("ParallelBatchCapture");
        ThreadFactory factory = builder.build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(lstBatchImageId.size(), factory);

        for (List<String> keys : lstBatchImageId) {
            Callable<List<CapturedPicture>> callable = new BatchCapturedPictureCallable(keys, type);
            FutureTask<List<CapturedPicture>> future = (FutureTask<List<CapturedPicture>>) executor.submit(callable);
            futures.add(future);
        }
        executor.shutdown();
        // Wait for all the tasks to finish
        try {
            boolean stillRunning = !executor.awaitTermination(
                    10000, TimeUnit.MILLISECONDS);
            if (stillRunning) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        try {
            for (Future f : futures) {
                if (null != f.get()) {
                    capturedPictureList.addAll((List<CapturedPicture>) f.get());
                } else {
                    LOG.info("capturePicture get by futureTask is null");
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return capturedPictureList;
    }

    private void setCapturedPicture_person(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) throws ParseException {
        if (result != null) {
            String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
            capturedPicture.setDescription(des);

            String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
            mapEx.put("ex", ex);
            capturedPicture.setExtend(mapEx);

            String ipcId = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID));
            capturedPicture.setIpcId(ipcId);

            String time = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP));
            long timeStamp = DateUtil.dateToTimeStamp(time);
            capturedPicture.setTimeStamp(timeStamp);
        } else {
            LOG.error("get Result from table_person is null! used method DynamicPhotoServiceImpl.setCapturedPicture_person.");
        }
    }

    private void setCapturedPicture_car(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) throws ParseException {
        if (result != null) {
            String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
            capturedPicture.setDescription(des);

            String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
            mapEx.put("ex", ex);
            capturedPicture.setExtend(mapEx);

            String ipcId = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IPCID));
            capturedPicture.setIpcId(ipcId);

            String time = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_TIMESTAMP));
            long timeStamp = DateUtil.dateToTimeStamp(time);
            capturedPicture.setTimeStamp(timeStamp);

            String plateNumber = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM));
            capturedPicture.setPlateNumber(plateNumber);
        }
    }

    /**
     * 批量获取图片对象数据（彭聪）
     *
     * @param capturedPictures 图片对象（不含图片数据）
     * @param type             图片类型
     * @return 返回包含图片数据的图片对象
     */
    @Override
    public List<CapturedPicture> getFullImageData(List<CapturedPicture> capturedPictures, int type) {
        if (null != capturedPictures && capturedPictures.size() > 0) {
            Table table;
            byte[] family;
            byte[] qualifier;
            if (type == PictureType.SMALL_PERSON.getType()) {
                table = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                family = DynamicTable.PERSON_COLUMNFAMILY;
                qualifier = DynamicTable.PERSON_COLUMN_IMGE;
            } else {
                table = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                family = DynamicTable.PERSON_COLUMNFAMILY;
                qualifier = DynamicTable.PERSON_COLUMN_IMGE;
            }
            capturedPictures = getBatchImageData(capturedPictures, table, family, qualifier);
            HBaseUtil.closTable(table);
        } else {
            LOG.info("the capturedPictures is null");
        }
        return capturedPictures;
    }

    /**
     * @param capturedPictures 图片对象（无图片数据）（彭聪）
     * @param table            表明
     * @param family           列簇
     * @param qualifier        列
     * @return 返回包含图片数据的图片对象
     */
    private List<CapturedPicture> getBatchImageData(List<CapturedPicture> capturedPictures, Table table, byte[] family, byte[] qualifier) {
        List<String> CurrentImageIdList = new ArrayList<>();
        for (CapturedPicture capturedPicture : capturedPictures) {
            CurrentImageIdList.add(capturedPicture.getId());
        }
        if (CurrentImageIdList.size() > 0) {
            List<Get> gets = new ArrayList<>();
            for (String anImageIdList : CurrentImageIdList) {
                Get get = new Get(Bytes.toBytes(anImageIdList));
                get.addColumn(family, qualifier);
                gets.add(get);
            }
            Result[] results = new Result[CurrentImageIdList.size()];
            try {
                results = table.get(gets);
                HBaseUtil.closTable(table);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (results != null) {
                CapturedPicture capturedPictureTemp;
                for (int i = 0; i < results.length; i++) {
                    capturedPictureTemp = capturedPictures.get(i);
                    if (results[i] != null) {
                        byte[] imageData = results[i].getValue(family, qualifier);
                        capturedPictureTemp.setSmallImage(imageData);
                    } else {
                        LOG.error("get Result from table is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                    }
                }
            } else {
                LOG.error("get Result[] from table is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
            }
        } else {
            LOG.info("imageIdList is null");
        }
        return capturedPictures;
    }

    /**
     * 获取单张图片数据（彭聪）
     *
     * @param capturedPicture 图片对象，无图片数据
     * @return 有图片数据的图片对象
     */
    @Override
    public CapturedPicture getImageData(CapturedPicture capturedPicture) {
        Table table;
        byte[] family;
        byte[] qualifier;
        int type = capturedPicture.getPictureType().getType();
        if (type == PictureType.SMALL_PERSON.getType()) {
            table = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            family = DynamicTable.PERSON_COLUMNFAMILY;
            qualifier = DynamicTable.PERSON_COLUMN_IMGE;
        } else {
            table = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            family = DynamicTable.PERSON_COLUMNFAMILY;
            qualifier = DynamicTable.PERSON_COLUMN_IMGE;
        }
        String imageId = capturedPicture.getId();
        Get get = new Get(Bytes.toBytes(imageId));
        try {
            Result result = table.get(get);
            if (null != result) {
                byte[] imageData = result.getValue(family, qualifier);
                capturedPicture.setSmallImage(imageData);
            } else {
                LOG.info("the results is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtil.closTable(table);
        }
        return capturedPicture;
    }

    @Override
    public List<CapturedPicture> getImageData(List<CapturedPicture> capturedPictures, int type) {
        return null;
    }
}

/**
 * 调用接口类，实现Callable接口（彭聪）
 */
class BatchCapturedPictureCallable implements Callable<List<CapturedPicture>>, Serializable {
    private List<String> keys;
    private int type;

    BatchCapturedPictureCallable(List<String> lstKeys, int picType) {
        this.keys = lstKeys;
        this.type = picType;
    }

    @Override
    public List<CapturedPicture> call() throws Exception {
        DynamicPhotoService dynamicPhotoService = new DynamicPhotoServiceImpl();
        return dynamicPhotoService.getBatchCaptureMessage(keys, type);
    }


    public List<CapturedPicture> getImageData(List<CapturedPicture> capturedPictures, int type) {
        return null;
    }

}

/**
 * 调用接口类，实现Callable接口（彭聪）
 */
class BatchFeaCallable implements Callable<List<float[]>>, Serializable {
    private List<String> keys;
    private PictureType type;

    BatchFeaCallable(List<String> lstKeys, PictureType pictureType) {
        this.keys = lstKeys;
        this.type = pictureType;
    }

    @Override
    public List<float[]> call() throws Exception {
        DynamicPhotoService dynamicPhotoService = new DynamicPhotoServiceImpl();
        return dynamicPhotoService.getBatchFeature(keys, type);
    }
}

