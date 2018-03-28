package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.ftpserver.util.DownloadUtils;
import com.hzgc.ftpserver.util.FtpUtils;
import com.hzgc.service.util.JDBCUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.sort.ListUtils;
import com.hzgc.util.sort.SortParam;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 通过parkSQL以图搜图
 */
class RealTimeFaceCompareBySparkSQL {
    private Logger LOG = Logger.getLogger(RealTimeFaceCompareBySparkSQL.class);
    private DynamicPhotoService dynamicPhotoService;

    RealTimeFaceCompareBySparkSQL() {
        dynamicPhotoService = new DynamicPhotoServiceImpl();
    }

    SearchResult pictureSearchBySparkSQL(SearchOption option, String searchId) {
        //搜索类型 是人还是车
        SearchType searchType = option.getSearchType();
        //设置查询Id
        if (null != searchType) {
            //查询的对象库是人
            if (searchType == SearchType.PERSON) {
                //根据上传的图片查询
                return compareByImageBySparkSQL(searchType, option, searchId);
                //查询的对象库是车
            } else if (searchType == SearchType.CAR) {
                //平台上传的参数中有图片
                return compareByImageBySparkSQL(searchType, option, searchId);
            }
        }
        return new SearchResult();
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param type 图片类型（人、车）SearchOption 过滤条件
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImageBySparkSQL(SearchType type, SearchOption option, String searchId) {
        String typeStr = null;
        Connection conn = null;
        Statement statement1 = null;
        Statement statement2 = null;
        ResultSet resultSet = null;
        SearchResult searchResult = null;
        //提取上传图片的特征值
        float[] searchFea;
        byte[] image;
        if (type == SearchType.PERSON) {
            typeStr = DynamicTable.PERSON_TYPE;
        } else if (type == SearchType.CAR) {
            typeStr = DynamicTable.CAR_TYPE;
        }
        if (option.getImage() != null) {
            image = option.getImage();
            searchFea = FaceFunction.featureExtract(option.getImage()).getFeature();
        } else {
            image = DownloadUtils.downloadftpFile2Bytes(option.getImageId());
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
            String selectBySparkSQL = ParseByOption.getFinalSQLwithOption(searchFeaStr, option);
            if (selectBySparkSQL.length() == 0) {
                LOG.warn("the threshold is null");
                return new SearchResult();
            }
            LOG.info("query sql:" + ParseByOption.getFinalSQLwithOption("", option));
            //特征值比对，根据条件过滤
            try {
                long start = System.currentTimeMillis();
                conn = JDBCUtil.getConnection();
                statement1 = conn.createStatement();
                statement1.execute("REFRESH TABLE " + DynamicTable.MID_TABLE +
                        ";REFRESH TABLE " + DynamicTable.PERSON_TABLE + ";");
                statement2 = conn.createStatement();
                resultSet = statement2.executeQuery(selectBySparkSQL);
                long mid = System.currentTimeMillis();
                LOG.info("executeQuery total time is:" + (mid - start));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (resultSet != null) {
                    List<CapturedPicture> capturedPictureList = new ArrayList<>();
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
                        String burl = FtpUtils.surlToBurl(surl);
                        //图片对象
                        CapturedPicture capturedPicture = new CapturedPicture();
                        capturedPicture.setSurl(FtpUtils.getFtpUrl(surl));
                        capturedPicture.setBurl(FtpUtils.getFtpUrl(burl));
                        capturedPicture.setIpcId(ipcid);
                        capturedPicture.setTimeStamp(format.format(timestamp));
                        capturedPicture.setSimilarity(similaritys);
                        capturedPictureList.add(capturedPicture);
                    }
                    searchResult = saveResults(capturedPictureList,
                            option.getOffset(),
                            option.getCount(),
                            searchId,
                            typeStr);
                    LOG.info("saveResult time is:" + (System.currentTimeMillis() - mid));
                } else {
                    LOG.info("result set is null");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (statement1 != null && !statement1.isClosed()) {
                        statement1.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (statement2 != null && !statement2.isClosed()) {
                        statement2.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return searchResult;
        } else {
            LOG.error("extract the feature is faild");
            return new SearchResult();
        }
    }

    /**
     * 经过阈值过滤以及根据排序参数重新生成的结果
     *
     * @param capturedPictures 抓拍图片信息的封装对象
     * @param offset           分页偏移量
     * @param count            分页读取的数量
     * @return 返回结果集
     */
    private SearchResult saveResults(List<CapturedPicture> capturedPictures, int offset, int count, String searchId, String typeStr) {
        SearchResult searchResultTemp = new SearchResult();
        if (null != capturedPictures && capturedPictures.size() > 0) {
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, capturedPictures, typeStr);
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPictures, offset, count);
            searchResultTemp.setPictures(subCapturedPictures);
            searchResultTemp.setSearchId(searchId);
            searchResultTemp.setTotal(capturedPictures.size());
        } else {
            LOG.error("Find no image by deviceIds or timeStamp");
        }
        return searchResultTemp;
    }


    /**
     * 根据阈值过滤后的imageIdList批量查询数据对象分组排序
     *
     * @param capturedPictures 根据阈值过滤之后的对象列表
     * @return 最终查询结果
     */
    private SearchResult sortAndSplit(List<CapturedPicture> capturedPictures,
                                      String sortParams,
                                      int offset,
                                      int count,
                                      String searchId,
                                      String typeStr) {
        SearchResult searchResultTemp = new SearchResult();
        List<CapturedPicture> capturedPicturesSorted;
        if (null != capturedPictures && capturedPictures.size() > 0) {
            capturedPicturesSorted = sortByParams(capturedPictures, sortParams);
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, capturedPicturesSorted, typeStr);
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
