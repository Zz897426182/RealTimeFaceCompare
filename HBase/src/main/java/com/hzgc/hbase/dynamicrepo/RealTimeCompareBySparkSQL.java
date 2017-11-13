package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.Download;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.util.JDBCUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * 通过parkSQL以图搜图
 */
class RealTimeCompareBySparkSQL {

    private Logger LOG = Logger.getLogger(RealTimeCompareBySparkSQL.class);
    /**
     * 获取jdbc连接
     */
    private JDBCUtil jdbcUtil = JDBCUtil.getInstance();
    /**
     * 查询Id 由UUID生成
     */
    private String searchId;
    /**
     * 查询结果，最终的返回值
     */
    private static SearchResult searchResult = new SearchResult();
    /**
     * 图片对象列表
     */
    private List<CapturedPicture> capturedPictureList = new ArrayList<>();
    /**
     * SQL语句生成器
     */
    private ParseByOption parseByOption = new ParseByOption();
    private String insertType;
    private DynamicPhotoService dynamicPhotoService;
    private CapturePictureSearchServiceImpl capturePictureSearchService;

    RealTimeCompareBySparkSQL() {

        dynamicPhotoService = new DynamicPhotoServiceImpl();
        capturePictureSearchService = new CapturePictureSearchServiceImpl();
    }

    SearchResult pictureSearchBySparkSQL(SearchOption option) {
        if (null != option) {
            //搜索类型 是人还是车
            SearchType searchType = option.getSearchType();
            //设置查询Id
            searchId = UuidUtil.setUuid();
            if (null != searchType) {
                //查询的对象库是人
                if (searchType == SearchType.PERSON) {
                    insertType = DynamicTable.PERSON_TYPE;
                    if (option.getImage() != null || option.getImageId() != null) {
                        //根据上传的图片查询
                        searchResult = compareByImageBySparkSQL(searchType, option);
                    } else {
                        //无图无imageId,通过其他参数查询
                        searchResult = capturePictureSearchService.getCaptureHistory(option);
                    }
                }
                //查询的对象库是车
                else if (searchType == SearchType.CAR) {
                    insertType = DynamicTable.CAR_TYPE;
                    //平台上传的参数中有图片
                    if (option.getImage() != null || option.getImageId() != null) {
                        searchResult = compareByImageBySparkSQL(searchType, option);
                    } else {
                        //无图无imageId,通过其他参数查询
                        searchResult = capturePictureSearchService.getCaptureHistory(option);
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
     * 以图搜图，图片不为空的查询方法
     *
     * @param type 图片类型（人、车）SearchOption 过滤条件
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImageBySparkSQL(SearchType type, SearchOption option) {
        //提取上传图片的特征值
        float[] searchFea;
        //根据imageId查找到的图片
        byte[] image;
        if (option.getImage() != null) {
            image = option.getImage();
            searchFea = FaceFunction.featureExtract(option.getImage()).getFeature();
        } else {
            image = Download.downloadftpFile2Bytes(option.getImageId());
            if (image == null) {
                return new SearchResult();
            }
            searchFea = FaceFunction.featureExtract(image).getFeature();
        }
        //将图片特征插入到特征库
        boolean insertStatus = dynamicPhotoService.upPictureInsert(type, searchId, searchFea, image);
        if (insertStatus) {
            LOG.info("feature[" + searchId + "]insert into HBase successful");
        } else {
            LOG.error("feature[" + searchId + "] insert into HBase failed");
        }
        //判断特征值是否符合
        if (null != searchFea && searchFea.length == 512) {
            //将float[]特征值转为String特征值
            String searchFeaStr = FaceFunction.floatArray2string(searchFea);
            String selectBySparkSQL = parseByOption.getFinalSQLwithOption(searchFeaStr, option);
            if (selectBySparkSQL.length() == 0) {
                LOG.warn("the threshold is null");
                return searchResult;
            }
            //特征值比对，根据条件过滤
            ResultSet resultSet = jdbcUtil.executeQuery(selectBySparkSQL);
            try {
                while (resultSet.next()) {
                    //小图ftpurl
                    String surl = resultSet.getString(DynamicTable.FTPURL);
                    //设备id
                    String ipcid = resultSet.getString(DynamicTable.IPCID);
                    //相似度
                    Float similaritys = resultSet.getFloat(DynamicTable.SIMILARITY);
                    //时间戳
                    Timestamp timestamp = resultSet.getTimestamp(DynamicTable.TIMESTAMP);
                    //大图ftpurl
                    String burl = FtpUtil.surlToBurl(surl);
                    CapturedPicture capturedPicture = new CapturedPicture();
                    capturedPicture.setSurl(surl);
                    capturedPicture.setBurl(burl);
                    capturedPicture.setIpcId(ipcid);
                    capturedPicture.setTimeStamp(timestamp.toString());
                    capturedPicture.setSimilarity(similaritys);
                    capturedPictureList.add(capturedPicture);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                jdbcUtil.close();
            }
            searchResult = sortAndSplit(capturedPictureList,
                    option.getSortParams(),
                    option.getOffset(),
                    option.getCount());
        } else {
            LOG.error("extract the feature is faild");
        }
        return searchResult;

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
            LOG.error("Find no image by deviceIds or timeStamp");
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
            LOG.error("sortParams is null!");
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
