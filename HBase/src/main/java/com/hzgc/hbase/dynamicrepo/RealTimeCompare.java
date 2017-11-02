package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.Attribute.Attribute;
import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * 多线程
 */
public class RealTimeCompare implements Serializable {
    private Logger LOG = Logger.getLogger(RealTimeCompare.class);
    /**
     * 图片的二进制数据
     */
    private byte[] image;
    /**
     * 图片 id ,优先使用图片流数组
     */
    private String imageId;
    /**
     * 阈值
     */
    private float threshold;
    /**
     * 排序参数
     */
    private String sortParams;
    /**
     * 分页查询开始行
     */
    private int offset;
    /**
     * 分页查询条数
     */
    private int count;
    /**
     * 查询Id 由UUID生成
     */
    private String searchId;
    /**
     * 用于保存筛选出来的一组一个图片的id
     */
    private List<String> imageIdList;
    /**
     * 过滤大图后的图片Id列表
     */
    private List<String> imageIdFilterList;
    /**
     * 查询结果，最终的返回值
     */
    private SearchResult searchResult;
    /**
     * 特征列表，根据rowKeyList批量查询到的特征
     */
    private List<float[]> feaFloatList;
    /**
     * 相似度列表，保存比对后的相似度
     */
    private List<Float> simList;
    /**
     * 图片对象列表
     */
    private List<CapturedPicture> capturedPictureList;
    private DynamicPhotoService dynamicPhotoService;
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
            Attribute attribute = option.getAttribute();
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
            }
        } else {
            LOG.error("search parameter option is null");
            searchResult.setSearchId(null);
            searchResult.setPictures(null);
            searchResult.setTotal(0);
        }
        return searchResult;
    }

    /**
     *抓拍历史查询
     */
    private SearchResult getCaptureHistory(SearchOption option) {
        PictureType pictureType = PictureType.SMALL_PERSON;
        option.setSearchType(SearchType.PERSON);
        long esStartTime = System.currentTimeMillis();
        searchResult = new SearchResult();
        searchResult = getImageIdListFromEs_Tmp(option);
        long esEndTime = System.currentTimeMillis();
        LOG.info("search" + searchResult.getTotal() + " history image from es takes:" + (esEndTime - esStartTime) + "ms");
        List<CapturedPicture> capturedPicturesTemp = searchResult.getPictures();
        List<String> rowKeyList = new ArrayList<>(capturedPicturesTemp.size());
        for (CapturedPicture capturedPicture : capturedPicturesTemp) {
            String rowKey = capturedPicture.getId();
            rowKeyList.add(rowKey);
        }
        List<CapturedPicture> capturedPicturesNew = dynamicPhotoService.getMultiBatchCaptureMessage(rowKeyList, pictureType.getType());
        long startTime = System.currentTimeMillis();
        List<CapturedPicture> capturedPicturesPerson = dynamicPhotoService.getImageData(capturedPicturesNew, pictureType.getType());
        long endTime = System.currentTimeMillis();
        LOG.info("search " + option.getCount() + "history image from es takes:" + (endTime - startTime) + "ms");
        searchResult.setPictures(capturedPicturesPerson);
        return searchResult;
    }

    private SearchResult getImageIdListFromEs_Tmp(SearchOption option) {
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        return filterByRowkey.getRowKey_tmp(option);
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
        float[] searchFea = FaceFunction.featureExtract(image).getFeature();
        if (null != searchFea && searchFea.length == 512) {
            //将图片特征插入到特征库
            boolean insertStatus = dynamicPhotoService.upPictureInsert(pictureType, searchId, searchFea, image);
            if (insertStatus) {
                LOG.info("feature[" + searchId + "]insert into HBase successful");
            } else {
                LOG.error("feature[" + searchId + "] insert into HBase failed");
            }
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            imageIdList = getImageIdListFromEs(option);
            if (null != imageIdList && imageIdList.size() > 0) {
                try {
                    feaFloatList = getFeaByImageId(imageIdList, pictureType);
                } catch (Exception e) {
                    LOG.error("get float[] feature failed by getFeaByImageId method");
                }
                if (null != feaFloatList && feaFloatList.size() > 0) {
                    simList = featureCompare(searchFea, feaFloatList);
                } else {
                    LOG.info("feaFloatList is null");
                }
                //根据阈值对计算结果进行过滤，并进行排序分页等操作
                searchResult = lastResult(imageIdList, simList, threshold, pictureType.getType(), sortParams);
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
        List<String> imageIdFilterList = new ArrayList<>();
        List<Float> simFilterList = new ArrayList<>();
        for (int i = 0, len = imageIdList.size(); i < len; i++) {
            if (simList.get(i) > threshold) {
                imageIdFilterList.add(imageIdList.get(i));
                simFilterList.add(simList.get(i));
            }
        }
        //多线程批量读取
        capturedPictureList = dynamicPhotoService.getMultiBatchCaptureMessage(imageIdFilterList, type);
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
            capturedPicturesSorted = sortByParams(capturedPictures, sortParams);
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, capturedPicturesSorted, insertType);
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPicturesSorted, offset, count);
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

