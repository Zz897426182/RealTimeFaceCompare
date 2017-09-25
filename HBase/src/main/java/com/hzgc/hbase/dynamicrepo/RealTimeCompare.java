package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import java.io.Serializable;
import java.util.*;

/**
 * 多线程
 */
public class RealTimeCompare implements Serializable {
    private Logger LOG = Logger.getLogger(RealTimeCompare.class);
    private byte[] image;// 图片的二进制数据
    private String imageId;//图片 id ,优先使用图片流数组
    private float threshold;//阈值
    private String sortParams;//排序参数
    private int offset;//分页查询开始行
    private int count;//分页查询条数
    private String searchId;//查询Id 由UUID生成
    private DynamicPhotoService dynamicPhotoService;
    private List<String> imageIdList;//用于保存筛选出来的一组一个图片的id
    private SearchResult searchResult;//查询结果，最终的返回值
    private List<float[]> feaFloatList;//特征列表，根据rowKeyList批量查询到的特征
    private List<Float> simList;//相似度列表，保存比对后的相似度
    private List<CapturedPicture> capturedPictureList;//图片对象列表
    private String insertType;

    public RealTimeCompare() {
        dynamicPhotoService = new DynamicPhotoServiceImpl();
    }

    SearchResult pictureSearch(SearchOption option) throws Exception {
        if (null != option) {
            SearchType searchType = option.getSearchType();
            imageId = option.getImageId();
            image = option.getImage();
            String plateNumber = option.getPlateNumber();
            threshold = option.getThreshold();
            List<String> deviceIds = option.getDeviceIds();
            String platformId = option.getPlatformId();
            Date startDate = option.getStartDate();
            Date endDate = option.getEndDate();
            List<TimeInterval> intervals = option.getIntervals();
            sortParams = option.getSortParams();
            List<SearchFilter> filters = option.getFilters();
            offset = option.getOffset();
            count = option.getCount();
            //设置查询Id
            searchId = UuidUtil.setUuid();
            if (null != searchType) {
                //查询的对象库是人
                if (searchType == SearchType.PERSON) {
                    insertType = DynamicTable.PERSON_TYPE;
                    PictureType pictureType = PictureType.SMALL_PERSON;
                    //上传的参数中有图
                    if (null != image && image.length > 0) {
                        searchResult = compareByImage(pictureType, option);
                    } else {
                        //无图，有imageId
                        if (null != imageId) {
                            searchResult = compareByImageId(pictureType, option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            searchResult = compareByOthers(pictureType, option);
                        }
                    }
                }
                //查询的对象库是车
                else if (searchType == SearchType.CAR) {
                    insertType = DynamicTable.CAR_TYPE;
                    PictureType pictureType = PictureType.SMALL_CAR;
                    //上传的参数中有图
                    if (null != image && image.length > 0) {
                        searchResult = compareByImage(pictureType, option);
                    } else {
                        //无图，有imageId
                        if (null != imageId) {
                            searchResult = compareByImageId(pictureType, option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            searchResult = compareByOthers(pictureType, option);
                        }
                    }
                }
            } else {
                //searchType 为空，则同时返回人、车
                insertType = DynamicTable.MIX_TYPE;
                PictureType pictureType;
                List<String> personImageIdList;
                option.setSearchType(SearchType.PERSON);
                personImageIdList = getImageIdListFromEs(option);

                //通过es查询到的车辆图片id列表
                List<String> carImageIdList = null;
               /* option.setSearchType(SearchType.CAR);
                carImageIdList = getImageIdListFromEs(option);*/
                List<String> personAddCarList = new ArrayList<>();//用于同时保存人车图片id
                capturedPictureList = new ArrayList<>();//用于同时保存人车图片对象
                if (null != personImageIdList && personImageIdList.size() > 0) {
                    personAddCarList.addAll(personImageIdList);
                    pictureType = PictureType.SMALL_PERSON;
                    List<CapturedPicture> capturedPicturesPerson = dynamicPhotoService.getMultiBatchCaptureMessage(personImageIdList, pictureType.getType());
                    if (null != capturedPicturesPerson) {
                        capturedPictureList.addAll(capturedPicturesPerson);
                    } else {
                        LOG.info("get person capturedPicture null");
                    }
                } else {
                    LOG.info("no person image get from es");
                }
                if (null != carImageIdList && carImageIdList.size() > 0) {
                    personAddCarList.addAll(carImageIdList);
                    pictureType = PictureType.SMALL_CAR;
                    List<CapturedPicture> capturedPicturesCar = dynamicPhotoService.getMultiBatchCaptureMessage(carImageIdList, pictureType.getType());
                    if (null != capturedPicturesCar) {
                        capturedPictureList.addAll(capturedPicturesCar);
                    } else {
                        LOG.info("get car capturedPicture null");
                    }
                } else {
                    LOG.info("no image of car get from es");
                }
                searchResult = sortAndSplit(capturedPictureList, sortParams, offset, count);
            }
        } else {
            LOG.error("search parameter option is null");
            searchResult = null;
        }
        return searchResult;
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImage(PictureType pictureType, SearchOption option) {
        //对上传的图片提取特征
        long feaExtrTime = System.currentTimeMillis();
        float[] searchFea = FaceFunction.featureExtract(image);
        System.out.println("特征提取的时间消耗：" + (System.currentTimeMillis() - feaExtrTime));
        if (null != searchFea && searchFea.length == 512) {
            //将图片特征插入到特征库
            long featureSaveTime = System.currentTimeMillis();
            boolean insertStatus = dynamicPhotoService.upPictureInsert(pictureType, searchId, searchFea, image);
            LOG.info("特征插入到HBase时间消耗：" + (System.currentTimeMillis() - featureSaveTime));
            if (insertStatus) {
                LOG.info("feature[" + searchId + "]insert into HBase successful");
            } else {
                LOG.error("feature[" + searchId + "] insert into HBase failed");
            }
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            long esTime = System.currentTimeMillis();
            imageIdList = getImageIdListFromEs(option);
            LOG.info("从es中筛选图片Id的数量" + imageIdList.size() + " ,时间消耗：" + (System.currentTimeMillis() - esTime));
            if (null != imageIdList && imageIdList.size() > 0) {
                //根据imageId找出对应特征加入组成二元组并加入到列表
                try {
                    long getFeaTime = System.currentTimeMillis();
                    feaFloatList = getFeaByImageId(imageIdList, pictureType);
                    LOG.info("从HBase中获取特征的数量：" + feaFloatList.size() + ",时间消耗：" + (System.currentTimeMillis() - getFeaTime));
                } catch (Exception e) {
                    LOG.error("get float[] feature failed by getFeaByImageId method");
                }
                long compareTime = System.currentTimeMillis();
                if (null != feaFloatList && feaFloatList.size() > 0) {
                    simList = featureCompare(searchFea, feaFloatList);
                } else {
                    LOG.info("feaFloatList is null");
                }
                LOG.info("特征比对数量：" + feaFloatList.size() + " ,时间消耗：" + (System.currentTimeMillis() - compareTime));
                //根据阈值对计算结果进行过滤，并进行排序分页等操作
                searchResult = lastResult(imageIdList, simList, threshold, pictureType.getType(), sortParams);
                // TODO: 2017-9-20  
                List<CapturedPicture> capturedPictureRes = searchResult.getPictures();
                //读取imageData并返回结果
                List<CapturedPicture> FullCapturePictureList = dynamicPhotoService.getFullImageData(capturedPictureRes, pictureType.getType());
                searchResult.setPictures(FullCapturePictureList);
            } else {
                LOG.info("the imageIdList is null");
            }
        } else {
            LOG.info("search feature is null or short than 512");
        }
        return searchResult;
    }

    /**
     * 根据图片id进行搜图的方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片rowkey
     */
    private SearchResult compareByImageId(PictureType pictureType, SearchOption option) {
        //根据imageId从动态库中获取特征值
        byte[] fea = dynamicPhotoService.getFeature(imageId, pictureType);
        if (null != fea && fea.length == 2048) {
            float[] searchFea = FaceFunction.byteArr2floatArr(fea);
            if (null != searchFea && searchFea.length == 512) {
                //从es中获取数据
                imageIdList = getImageIdListFromEs(option);
                if (null != imageIdList && imageIdList.size() > 0) {
                    try {
                        //根据imageId找出对应特征加入组成二元组并加入到列表
                        feaFloatList = getFeaByImageId(imageIdList, pictureType);
                    } catch (Exception e) {
                        LOG.error("Failed to get feature by imageId", e);
                    }
                    if (null != feaFloatList && feaFloatList.size() > 0) {
                        //对特征进行比对
                        simList = featureCompare(searchFea, feaFloatList);
                        if (null != simList && simList.size() > 0) {
                            searchResult = lastResult(imageIdList, simList, threshold, pictureType.getType(), sortParams);
                            List<CapturedPicture> capturedPictureRes = searchResult.getPictures();
                            // TODO: 2017-9-20  
                            //读取imageData并返回结果
                            List<CapturedPicture> FullCapturePictureList = dynamicPhotoService.getFullImageData(capturedPictureRes, pictureType.getType());
                            searchResult.setPictures(FullCapturePictureList);
                        } else {
                            LOG.info("simList is null");
                        }
                    } else {
                        LOG.error("all the feature of imageIdList is null");
                    }
                } else {
                    LOG.info("the imageIdList is null");
                }
            }
        } else {
            LOG.error("the feature read from HBase is null or short than 2048");
        }
        return searchResult;
    }

    /**
     * 无图/图片id，仅通过设备、时间等参数进行搜图
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回满足所有查询条件的图片
     */
    private SearchResult compareByOthers(PictureType pictureType, SearchOption option) {
        //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
        imageIdList = getImageIdListFromEs(option);
        if (null != imageIdList && imageIdList.size() > 0) {
            capturedPictureList = dynamicPhotoService.getMultiBatchCaptureMessage(imageIdList, pictureType.getType());
            searchResult = sortAndSplit(capturedPictureList, sortParams, offset, count);
            // TODO: 2017-9-20  
            List<CapturedPicture> capturedPictureRes = searchResult.getPictures();
            //读取imageData并返回结果
            List<CapturedPicture> FullCapturePictureList = dynamicPhotoService.getFullImageData(capturedPictureRes, pictureType.getType());
            searchResult.setPictures(FullCapturePictureList);
        } else {
            LOG.info("no image find in es by method getImageIdListFromEs");
        }
        return searchResult;
    }

    /**
     * 通过elasticSearch根据查询参数对HBase数据库进行过滤
     *
     * @param option 查询参数
     * @return 返回满足所有查询条件的结果
     */
    private List<String> getImageIdListFromEs(SearchOption option) {
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        return filterByRowkey.getRowKey(option);
    }

    /**
     * 通过图片id从对应库中查询其特征值
     *
     * @param imageIdList 图片id列表
     * @param pictureType 图片类型（人、车）
     * @return 图片id及其特征所组成的二元组列表
     */
    private List<float[]> getFeaByImageId(List<String> imageIdList, PictureType pictureType) {
        return dynamicPhotoService.getMultiBatchFeature(imageIdList, pictureType);
    }

    /**
     * 进行特征比对的方法
     *
     * @param searchFea    查询图片的特征
     * @param feaFloatList 查询到的图片的id以及其特征二元组列表
     * @return 查询到的图片的id以及其与查询图片的相似度二元组列表
     */
    private List<Float> featureCompare(final float[] searchFea, List<float[]> feaFloatList) {
        simList = new ArrayList<>();
        float similarity;
        //对两个特征进行比对
        for (int i = 0, len = feaFloatList.size(); i < len; i++) {
            float[] feaFloat = feaFloatList.get(i);
            if (null != feaFloat && feaFloat.length == 512) {
                similarity = FaceFunction.featureCompare(searchFea, feaFloat);
                simList.add(similarity);
            } else {
                simList.add(0.00f);
            }
        }
        return simList;
    }

    /**
     * 对计算结果根据阈值过滤，根据排序参数排序，分页
     *
     * @param simList    计算得到图片id以及其与查询图片的相似度二元组列表
     * @param threshold  相似度阈值
     * @param type       图片类型
     * @param sortParams 排序参数
     * @return 阈值过滤、排序、分页后最终返回结果
     */
    private SearchResult lastResult(List<String> imageIdList, List<Float> simList, final float threshold, final int type, String sortParams) {
        long thresholdTime = System.currentTimeMillis();
        List<String> imageIdFilterList = new ArrayList<>();
        List<Float> simFilterList = new ArrayList<>();
        for (int i = 0, len = imageIdList.size(); i < len; i++) {
            if (simList.get(i) > threshold) {
                imageIdFilterList.add(imageIdList.get(i));
                simFilterList.add(simList.get(i));
            }
        }
        LOG.info("根据相似度过滤imageId的数量：" + simFilterList.size() + " ,时间消耗：" + (System.currentTimeMillis() - thresholdTime));
        long getMultiBatchCaptureMessageTime = System.currentTimeMillis();
        //多线程批量读取
        capturedPictureList = dynamicPhotoService.getMultiBatchCaptureMessage(imageIdFilterList, type);
        Log.info("多线程批量获取图片对象的时间消耗：" + (System.currentTimeMillis() - getMultiBatchCaptureMessageTime));
        capturedPictureList = setSimilaritys(capturedPictureList, imageIdFilterList, simFilterList);
        searchResult = sortAndSplit(capturedPictureList, sortParams, offset, count);
        return searchResult;
    }

    /**
     * 设置默认相似度为0.00f
     *
     * @param imageIdList 图片ID列表
     * @return 图片和相似度map
     */
    private Map<String, Float> setDefaultSimilarity(List<String> imageIdList) {
        Map<String, Float> imgSimMap = new HashMap<>();
        if (null != imageIdList) {
            for (int i = 0, len = imageIdList.size(); i < len; i++) {
                imgSimMap.put(imageIdList.get(i), 0.00f);
            }
        } else {
            LOG.info("imageIdList is null");
        }
        return imgSimMap;
    }

    /**
     * 为图片对象设置相似度
     *
     * @param capturedPictureList 图片对象
     * @param imageIdFilterList   图片列表
     * @param simFilterList       相似度列表
     * @return 图片对象
     */
    private List<CapturedPicture> setSimilaritys(List<CapturedPicture> capturedPictureList, List<String> imageIdFilterList, List<Float> simFilterList) {
        List<CapturedPicture> capturedPictureListTemp = new ArrayList<>();
        CapturedPicture capturedPicture;
        for (int i = 0, len = imageIdFilterList.size(); i < len; i++) {
            capturedPicture = capturedPictureList.get(i);
            capturedPicture.setSimilarity(simFilterList.get(i));
            capturedPictureListTemp.add(capturedPicture);
        }
        return capturedPictureListTemp;
    }

    /**
     * 根据阈值过滤后的imageIdList批量查询数据对象分组排序
     *
     * @param capturedPictures 根据阈值过滤之后的对象列表
     * @return 最终查询结果
     */
    private SearchResult sortAndSplit(List<CapturedPicture> capturedPictures, String sortParams, int offset, int count) {
        SearchResult searchResultTemp = new SearchResult();
        List<CapturedPicture> capturedPicturesSorted;
        if (null != capturedPictures && capturedPictures.size() > 0) {
            long sortTime = System.currentTimeMillis();
            capturedPicturesSorted = sortByParams(capturedPictures, sortParams);
            LOG.info("对" + capturedPictures.size() + "张图片对象进行排序时间消耗：" + (System.currentTimeMillis() - sortTime));
            //保存到Hbase
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, capturedPicturesSorted, insertType);
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
            long splitTime = System.currentTimeMillis();
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPicturesSorted, offset, count);
            LOG.info("分页返回" + subCapturedPictures.size() + "时间消耗：" + (System.currentTimeMillis() - splitTime));
            searchResultTemp = new SearchResult();
            searchResultTemp.setPictures(subCapturedPictures);
            searchResultTemp.setSearchId(searchId);
            searchResultTemp.setTotal(capturedPictures.size());
        } else {
            LOG.info("Find no image by deviceIds or timeStamp");
        }
        return searchResultTemp;
    }

    /**
     * 根据排序参数对图片对象列表进行排序，支持多字段
     *
     * @param capturedPictures 待排序的图片对象列表
     * @param sortParams       排序参数
     * @return 排序后的图片对象列表
     */
    private List<CapturedPicture> sortByParams(List<CapturedPicture> capturedPictures, String sortParams) {
        //对排序参数进行读取和预处理
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        if (null != sortParams && sortParams.length() > 0) {
            //根据自定义的排序方法进行排序
            ListUtils.sort(capturedPictures, sortParam.getSortNameArr(), sortParam.getIsAscArr());
        } else {
            LOG.info("sortParams is null!");
        }
        return capturedPictures;
    }

    /**
     * 对图片对象列表进行分页返回
     *
     * @param capturedPictures 待分页的图片对象列表
     * @param offset           起始行
     * @param count            条数
     * @return 返回分页查询结果
     */
    private List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset > -1 && totalPicture > (offset + count - 1)) {
            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }
}

