package com.hzgc.service.clustering;

import com.hzgc.dubbo.clustering.AlarmInfo;
import com.hzgc.dubbo.clustering.ClusteringAttribute;
import com.hzgc.dubbo.clustering.ClusteringInfo;
import com.hzgc.dubbo.clustering.ClusteringSearchService;
import com.hzgc.service.dynamicrepo.DynamicTable;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.util.common.ObjectUtil;
import com.hzgc.util.sort.ListUtils;
import com.hzgc.util.sort.SortParam;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 告警聚类结果查询接口实现(彭聪)
 */
public class ClusteringSearchServiceImpl implements ClusteringSearchService {
    private static Logger LOG = Logger.getLogger(ClusteringSearchServiceImpl.class);
    private static final String IGNORE_FLAG_YES = "yes";
    private static final String IGNORE_FLAG_NO = "no";

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
    public ClusteringInfo clusteringSearch(String region, String time, int start, int limit, String sortParam) {
        Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO);
        Get get = new Get(Bytes.toBytes(time + "-" + region));
        List<ClusteringAttribute> clusteringList = new ArrayList<>();
        try {
            Result result = clusteringInfoTable.get(get);
            if (result != null && result.size() > 0) {
                clusteringList = (List<ClusteringAttribute>) ObjectUtil.byteToObject(result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_YES));
                if (sortParam != null && sortParam.length() > 0) {
                    SortParam sortParams = ListUtils.getOrderStringBySort(sortParam);
                    ListUtils.sort(clusteringList, sortParams.getSortNameArr(), sortParams.getIsAscArr());
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int total = clusteringList.size();
        ClusteringInfo clusteringInfo = new ClusteringInfo();
        clusteringInfo.setTotalClustering(total);
        if (start > -1 && start <= total) {
            if ((start + limit) > total) {
                clusteringInfo.setClusteringAttributeList(clusteringList.subList(start, total));
            } else {
                clusteringInfo.setClusteringAttributeList(clusteringList.subList(start, start + limit));
            }
        } else {
            LOG.info("start or limit out of index ");
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
            alarmInfoList = (List<AlarmInfo>) ObjectUtil.byteToObject(result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_YES));
            if (sortParam != null && sortParam.length() > 0) {
                SortParam sortParams = ListUtils.getOrderStringBySort(sortParam);
                ListUtils.sort(alarmInfoList, sortParams.getSortNameArr(), sortParams.getIsAscArr());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int total = alarmInfoList.size();
        if (start > -1 && start <= total) {
            if ((start + limit) > total) {
                return alarmInfoList.subList(start, total);
            } else {
                return alarmInfoList.subList(start, start + limit);
            }
        } else {
            LOG.info("start or limit out of index");
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
                .setFrom(start)
                .setSize(limit)
                .addSort(DynamicTable.ALARM_TIME, SortOrder.DESC)
                .setQuery(totalBQ);
        SearchHit[] results = searchRequestBuilder.get().getHits().getHits();
        List<Integer> alarmIdList = new ArrayList<>();
        if (results != null && results.length > 0) {
            for (SearchHit result : results) {
                alarmIdList.add((int) result.getSource().get(DynamicTable.ALARM_ID));
            }
        } else {
            LOG.info("no data get from es");
            return null;
        }
        return alarmIdList;
    }

    /**
     * delete a clustering
     *
     * @param clusterIdList clusteringId include region information
     * @param time          clustering time
     * @param flag          yes: delete the ignored clustering, no :delete the not ignored clustering
     * @return ture or false indicating whether delete is successful
     */
    @Override
    public boolean deleteClustering(List<String> clusterIdList, String time, String flag) {
        if (clusterIdList != null && time != null) {
            String clusteringId = clusterIdList.get(0);
            String region = clusteringId.split("-")[0];
            Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO);
            Get get = new Get(Bytes.toBytes(time + "-" + region));
            Put put = new Put(Bytes.toBytes(time + "-" + region));
            byte[] colName;
            if (flag.toLowerCase().equals(IGNORE_FLAG_YES)) {
                colName = ClusteringTable.ClUSTERINGINFO_COLUMN_NO;
            } else if (flag.toLowerCase().equals(IGNORE_FLAG_NO)) {
                colName = ClusteringTable.ClUSTERINGINFO_COLUMN_YES;
            } else {
                LOG.info("flag is error, it must be yes or no");
                return false;
            }
            try {
                Result result = clusteringInfoTable.get(get);
                byte[] bytes = result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colName);
                if (bytes != null) {
                    List<ClusteringAttribute> clusteringAttributeList = (List<ClusteringAttribute>) ObjectUtil.byteToObject(bytes);
                    for (String clusterId : clusterIdList) {
                        Iterator<ClusteringAttribute> iterator = clusteringAttributeList.iterator();
                        ClusteringAttribute clusteringAttribute;
                        while (iterator.hasNext()) {
                            clusteringAttribute = iterator.next();
                            if (clusterId.equals(clusteringAttribute.getClusteringId())) {
                                iterator.remove();
                            }
                        }
                    }
                    byte[] clusteringInfoByte = ObjectUtil.objectToByte(clusteringAttributeList);
                    put.addColumn(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colName, clusteringInfoByte);
                    clusteringInfoTable.put(put);
                    return true;
                } else {
                    LOG.info("no clustering in the database to be delete");
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * ignore a clustering
     *
     * @param clusterIdList cluteringId include region information
     * @param time          clutering time
     * @param flag          yes is ignore, no is not ignore
     * @return
     */
    @Override
    public boolean ignoreClustering(List<String> clusterIdList, String time, String flag) {
        if (clusterIdList != null && time != null) {
            String clusteringId = clusterIdList.get(0);
            String region = clusteringId.split("-")[0];
            Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO);
            Get get = new Get(Bytes.toBytes(time + "-" + region));
            Put put = new Put(Bytes.toBytes(time + "-" + region));
            byte[] colNameSrc;
            byte[] colNameDes;
            if (flag.toLowerCase().equals(IGNORE_FLAG_YES)) {
                colNameSrc = ClusteringTable.ClUSTERINGINFO_COLUMN_YES;
                colNameDes = ClusteringTable.ClUSTERINGINFO_COLUMN_NO;
            } else if (flag.toLowerCase().equals(IGNORE_FLAG_NO)) {
                colNameSrc = ClusteringTable.ClUSTERINGINFO_COLUMN_NO;
                colNameDes = ClusteringTable.ClUSTERINGINFO_COLUMN_YES;
            } else {
                LOG.info("flag is error, it must be yes or no");
                return false;
            }
            try {
                Result result = clusteringInfoTable.get(get);
                List<ClusteringAttribute> listSrc = new ArrayList<>();
                byte[] valueSrc = result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colNameSrc);
                if (valueSrc != null) {
                    listSrc = (List<ClusteringAttribute>) ObjectUtil.byteToObject(valueSrc);
                }
                List<ClusteringAttribute> listDes = new ArrayList<>();
                byte[] valuesDes = result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colNameDes);
                if (valuesDes != null) {
                    listDes = (List<ClusteringAttribute>) ObjectUtil.byteToObject(valuesDes);
                }
                //yes 表示数据需要忽略（HBase表中存入"n"列），no 表示数据不需要忽略（HBase表中存入"y"列）
                for (String clusterId : clusterIdList) {
                    Iterator<ClusteringAttribute> iterator = listSrc.iterator();
                    ClusteringAttribute clusteringAttribute;
                    while (iterator.hasNext()) {
                        clusteringAttribute = iterator.next();
                        if (clusterId.equals(clusteringAttribute.getClusteringId())) {
                            clusteringAttribute.setFlag(flag);
                            listDes.add(clusteringAttribute);
                            iterator.remove();
                        }
                    }
                }
                byte[] clusteringInfoSrc = ObjectUtil.objectToByte(listSrc);
                byte[] clusteringInfoDes = ObjectUtil.objectToByte(listDes);
                put.addColumn(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colNameSrc, clusteringInfoSrc);
                put.addColumn(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, colNameDes, clusteringInfoDes);
                clusteringInfoTable.put(put);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * get detail Clustering from HBase
     *
     * @param clusterId clustering id
     * @param time      clustering time
     * @param start     index start
     * @param limit     count of data
     * @param sortParam the parameters of sort
     * @return
     */
    @Deprecated

    public List<Integer> detailClusteringSearch_Hbase(String clusterId, String time, int start, int limit, String
            sortParam) {
        Table clusteringInfoTable = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO);
        Get get = new Get(Bytes.toBytes(time + "-" + clusterId));
        List<Integer> alarmInfoList = new ArrayList<>();
        try {
            Result result = clusteringInfoTable.get(get);
            alarmInfoList = (List<Integer>) ObjectUtil.byteToObject(result.getValue(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_YES));
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
}
