package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import com.hzgc.service.util.ListUtils;
import com.hzgc.util.common.ObjectUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hzgc.util.common.ObjectUtil.objectToByte;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceHelper {
    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceHelper.class);

    /**
     * 存储查询结果
     *
     * @param result 查询结果
     * @return 返回是否插入成功
     */
    static boolean insertSearchRes(SearchResult result) {
        Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        try {
            Put put = new Put(Bytes.toBytes(result.getSearchId()));
            put.setDurability(Durability.SYNC_WAL);
            byte[] searchMessage = objectToByte(result);
            put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE, searchMessage);
            searchRes.put(put);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Insert data by searchId from table_searchRes failed! used method DynamicPhotoServiceHelper.insertSearchRes.");
        } finally {
            HBaseUtil.closTable(searchRes);
        }
        return false;
    }

    /**
     * 通过查询ID获取之前的查询结果
     *
     * @param searchId 查询ID
     * @return 返回查询结果对象
     */
    static SearchResult getSearchRes(String searchId) {
        Result result;
        SearchResult searchResult = null;
        Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Get get = new Get(Bytes.toBytes(searchId));
        try {
            result = searchResTable.get(get);
            if (result != null) {
                byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
                searchResult = ((SearchResult) ObjectUtil.byteToObject(searchMessage));
                searchResult.setSearchId(searchId);
                return searchResult;
            } else {
                LOG.info("Get searchResult null from table_searchRes, search id is:" + searchId);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("No result get by searchId[" + searchId + "]");
        } finally {
            HBaseUtil.closTable(searchResTable);
        }
        return null;
    }

    /**
     * 通过排序参数进行排序
     *
     * @param result 查询结果
     * @param option 查询结果的查询参数
     */
    static void sortByParamsAndPageSplit(SearchResult result, SearchResultOption option) {
        List<SortParam> paramList = option.getSortParam();
        boolean[] isAscArr = new boolean[paramList.size()];
        String[] sortNameArr = new String[paramList.size()];
        for (int i = 0; i < paramList.size(); i++) {
            switch (paramList.get(i)) {
                case TIMEASC:
                    isAscArr[i] = true;
                    sortNameArr[i] = "timeStamp";
                    break;
                case TIMEDESC:
                    isAscArr[i] = false;
                    sortNameArr[i] = "timeStamp";
                    break;
                case SIMDESC:
                    isAscArr[i] = false;
                    sortNameArr[i] = "similarity";
                    break;
                case SIMDASC:
                    isAscArr[i] = true;
                    sortNameArr[i] = "similarity";
                    break;
            }
        }
        if (paramList.contains(SortParam.IPC)) {
            groupByIpc(result);
            for (SingleResult singleResult : result.getResults()) {
                for (GroupByIpc groupByIpc : singleResult.getPicturesByIpc()) {
                    ListUtils.sort(groupByIpc.getPictures(), sortNameArr, isAscArr);
                    groupByIpc.setPictures(pageSplit(groupByIpc.getPictures(), option));
                }
            }
        } else {
            for (SingleResult singleResult : result.getResults()) {
                ListUtils.sort(singleResult.getPictures(), sortNameArr, isAscArr);
                singleResult.setPictures(pageSplit(singleResult.getPictures(), option));
            }
        }
    }

    /**
     * 根据设备ID进行归类
     *
     * @param result 历史查询结果
     */
    private static void groupByIpc(SearchResult result) {
        for (SingleResult singleResult : result.getResults()) {
            List<GroupByIpc> list = new ArrayList<>();
            Map<String, List<CapturedPicture>> map =
                    singleResult.getPictures().stream().collect(Collectors.groupingBy(CapturedPicture::getIpcId));
            for (String key : map.keySet()) {
                GroupByIpc groupByIpc = new GroupByIpc();
                groupByIpc.setIpc(key);
                groupByIpc.setPictures(map.get(key));
                groupByIpc.setTotal(map.get(key).size());
                list.add(groupByIpc);
            }
            singleResult.setPicturesByIpc(list);
        }
    }

    /**
     * 对图片对象列表进行分页返回
     *
     * @param capturedPictures 待分页的图片对象列表
     * @param option 查询结果的查询参数
     * @return 返回分页查询结果
     */
    static List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, SearchResultOption option) {
        int offset = option.getStart();
        int count = option.getLimit();
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

    static List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset >= 0 && totalPicture > (offset + count - 1) && count > 0) {
            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }
}

