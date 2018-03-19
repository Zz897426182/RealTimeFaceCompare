package com.hzgc.service.clustering;

import com.hzgc.dubbo.clustering.AlarmInfo;
import com.hzgc.dubbo.clustering.ClusteringAttribute;
import com.hzgc.dubbo.clustering.ClusteringInfo;
import com.hzgc.dubbo.clustering.ClusteringSearchService;
import com.hzgc.service.dynamicrepo.DynamicTable;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.util.common.ObjectUtil;
import com.hzgc.util.common.sort.ListUtils;
import com.hzgc.util.common.sort.SortParam;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 告警聚类结果查询接口实现(彭聪)
 */
public class ClusteringSearchServiceImpl implements ClusteringSearchService {
    private static Logger LOG = Logger.getLogger(ClusteringSearchServiceImpl.class);

    /**
     * 查询聚类信息
     *
     * @param time      聚类时间
     * @param start     返回数据下标开始符号
     * @param limit     行数
     * @param sortParam 排序参数（默认按出现次数排序）
     * @return 聚类列表
     */
    @Override
    public ClusteringInfo clusteringSearch(String time, int start, int limit, String sortParam) {
        Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO);
        Get get = new Get(Bytes.toBytes(time));
        List<ClusteringAttribute> clusteringList = new ArrayList<>();
        try {
            Result result = clusteringInfoTable.get(get);
            clusteringList = (List<ClusteringAttribute>) ObjectUtil.byteToObject(result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_DATA));
            if (sortParam != null && sortParam.length() > 0) {
                SortParam sortParams = ListUtils.getOrderStringBySort(sortParam);
                ListUtils.sort(clusteringList, sortParams.getSortNameArr(), sortParams.getIsAscArr());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int total = clusteringList.size();
        ClusteringInfo clusteringInfo = new ClusteringInfo();
        clusteringInfo.setTotalClustering(total);
        if (start > -1) {
            if ((start + limit) > total - 1) {
                clusteringInfo.setClusteringAttributeList(clusteringList.subList(start, total));
            } else {
                clusteringInfo.setClusteringAttributeList(clusteringList.subList(start, start + limit));
            }
        } else {
            LOG.info("start must bigger than -1");
        }
        return clusteringInfo;
    }

    /**
     * 查询单个聚类详细信息(告警记录)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    @Override
    public List<AlarmInfo> detailClusteringSearch(String clusterId, String time, int start, int limit, String sortParam) {
        Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO);
        Get get = new Get(Bytes.toBytes(time + "-" + clusterId));
        List<AlarmInfo> alarmInfoList = new ArrayList<>();
        try {
            Result result = clusteringInfoTable.get(get);
            alarmInfoList = (List<AlarmInfo>) ObjectUtil.byteToObject(result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_DATA));
            if (sortParam != null && sortParam.length() > 0) {
                SortParam sortParams = ListUtils.getOrderStringBySort(sortParam);
                ListUtils.sort(alarmInfoList, sortParams.getSortNameArr(), sortParams.getIsAscArr());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (start > -1) {
            if ((start + limit) > alarmInfoList.size() - 1) {
                return alarmInfoList.subList(start, alarmInfoList.size());
            } else {
                return alarmInfoList.subList(start, start + limit);
            }
        } else {
            LOG.info("start must bigger than -1");
            return null;
        }
    }

    /**
     * 查询单个聚类详细信息(告警ID)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    @Override
    public List<Integer> detailClusteringSearch_v1(String clusterId, String time, int start, int limit, String sortParam) {
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        if (clusterId != null && time != null) {
            totalBQ.must(QueryBuilders.matchPhraseQuery(DynamicTable.CLUSTERING_ID, time + "-" + clusterId));
        }
        SearchRequestBuilder searchRequestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                .setQuery(totalBQ);
        SearchHit[] results = searchRequestBuilder.get().getHits().getHits();
        List<Integer> alarmIdList = new ArrayList<>();
        for (SearchHit result : results) {
            alarmIdList.add((int) result.getSource().get(DynamicTable.ALARM_ID));
        }
        return alarmIdList;
    }
}
