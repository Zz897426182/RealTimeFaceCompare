package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.ftpserver.util.FtpUtils;
import com.hzgc.service.util.JDBCUtil;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * 通过parkSQL以图搜图
 */
class RealTimeFaceCompareBySparkSQL {
    private Logger LOG = Logger.getLogger(RealTimeFaceCompareBySparkSQL.class);

    RealTimeFaceCompareBySparkSQL() {
    }

    SearchResult pictureSearchBySparkSQL(SearchOption option, String searchId) {
        //搜索类型 是人还是车
        SearchType searchType = option.getSearchType();
        //设置查询Id
        if (null != searchType) {
            //查询的对象库是人
            if (searchType == SearchType.PERSON) {
                //根据上传的图片查询
                return compareByImageBySparkSQL(option, searchId);
                //查询的对象库是车
            } else if (searchType == SearchType.CAR) {
                //平台上传的参数中有图片
                return compareByImageBySparkSQL(option, searchId);
            }
        }
        return new SearchResult();
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImageBySparkSQL(SearchOption option, String searchId) {
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet;
        SearchResult searchResult = null;

        String selectBySparkSQL = null;
        try {
            selectBySparkSQL = ParseByOption.getFinalSQLwithOption(option, false);
            LOG.info("Query sql:" + ParseByOption.getFinalSQLwithOption(option, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //特征值比对，根据条件过滤
        try {
            //开始通过spark执行查询
            long start = System.currentTimeMillis();
            conn = JDBCUtil.getConnection();
            LOG.info("Get jdbc connection time is:" + (System.currentTimeMillis() - start));
            statement = conn.createStatement();
            resultSet = statement.executeQuery(selectBySparkSQL);
            LOG.info("Execute query total time is:" + (System.currentTimeMillis() - start));
            //查询结束
            //整理查询数据
            long processDataStart = System.currentTimeMillis();
            if (resultSet != null) {
                if (option.isOnePerson() || option.getImages().size() == 1) {
                    searchResult = parseResultOnePerson(resultSet, option, searchId);
                } else {
                    searchResult = parseResultNotOnePerson(resultSet, option, searchId);
                }
                long processDataEnd = System.currentTimeMillis();
                LOG.info("Process query data time is:" + (processDataEnd - processDataStart));
                searchResult = saveResults(searchResult,
                        option.getOffset(),
                        option.getCount(),
                        searchId);
                LOG.info("SaveResult time is:" + (System.currentTimeMillis() - processDataEnd));
            } else {
                LOG.info("Query result set is null");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
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
        searchResult.setSearchType(DynamicTable.PERSON_TYPE);
        return searchResult;
    }

    private SearchResult parseResultOnePerson(ResultSet resultSet, SearchOption option, String searchId) {
        SingleResult singleResult = new SingleResult();
        SearchResult searchResult = new SearchResult();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
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
            singleResult.
                    setBinPicture(option.getImages().stream().map(PictureData::getBinImage).collect(toList()));
            singleResult.setId(searchId + "-0");
            singleResult.setPictures(capturedPictureList);
            singleResult.setTotal(capturedPictureList.size());
            searchResult.setSearchId(searchId);
            List<SingleResult> singleList = new ArrayList<>();
            singleList.add(singleResult);
            searchResult.setResults(singleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    private SearchResult parseResultNotOnePerson(ResultSet resultSet, SearchOption option, String searchId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, List<CapturedPicture>> mapSet = new HashMap<>();
        SearchResult searchResult = new SearchResult();
        List<SingleResult> singleResultList = new ArrayList<>();
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
                //group id
                String id = resultSet.getString(DynamicTable.GROUP_FIELD);
                //大图ftpurl
                String burl = FtpUtils.surlToBurl(surl);
                //图片对象
                CapturedPicture capturedPicture = new CapturedPicture();
                capturedPicture.setSurl(FtpUtils.getFtpUrl(surl));
                capturedPicture.setBurl(FtpUtils.getFtpUrl(burl));
                capturedPicture.setIpcId(ipcid);
                capturedPicture.setTimeStamp(format.format(timestamp));
                capturedPicture.setSimilarity(similaritys);
                if (mapSet.containsKey(id)) {
                    mapSet.get(id).add(capturedPicture);
                } else {
                    List<CapturedPicture> pictureList = new ArrayList<>();
                    pictureList.add(capturedPicture);
                    mapSet.put(id, pictureList);
                }
            }
            searchResult.setSearchId(searchId);
            for (int i = 0; i < option.getImages().size(); i++) {
                SingleResult singleResult = new SingleResult();
                String temp = i + "";
                if (mapSet.containsKey(temp)) {
                    singleResult.setPictures(mapSet.get(temp));
                    singleResult.setTotal(mapSet.get(temp).size());
                    List<byte[]> list = new ArrayList<>();
                    list.add(option.getImages().get(i).getBinImage());
                    singleResult.setBinPicture(list);
                    singleResult.setId(searchId + "-" + i);
                    singleResultList.add(singleResult);
                } else {
                    List<byte[]> list = new ArrayList<>();
                    list.add(option.getImages().get(i).getBinImage());
                    singleResult.setBinPicture(list);
                    singleResult.setTotal(0);
                    singleResult.setPictures(new ArrayList<>());
                    singleResult.setId(searchId + i);
                    singleResultList.add(singleResult);
                }

            }
            searchResult.setResults(singleResultList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        searchResult.setSearchType(DynamicTable.PERSON_TYPE);
        return searchResult;
    }

    /**
     * 经过阈值过滤以及根据排序参数重新生成的结果
     *
     * @param result 查询结果
     * @param offset 分页偏移量
     * @param count  分页读取的数量
     * @return 返回结果集
     */
    private SearchResult saveResults(SearchResult result, int offset, int count, String searchId) {
        if (result.getResults().size() > 0) {
            boolean flag = DynamicPhotoServiceHelper.insertSearchRes(result);
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
            for (SingleResult singleResult : result.getResults()) {
                singleResult.setPictures(DynamicPhotoServiceHelper.pageSplit(singleResult.getPictures(), offset, count));
            }
        } else {
            LOG.error("Find no image by deviceIds or timeStamp");
        }
        return result;
    }
}
