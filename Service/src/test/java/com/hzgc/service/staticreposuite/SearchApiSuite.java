package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PSearchArgsModel;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchApiSuite {
    @Test
    public void testSearchByTeams(){
        List<String> pkeys = new ArrayList<>();
        pkeys.add("223456");
        pkeys.add("223458");
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        requestBuilder.setQuery(QueryBuilders.termsQuery("pkey", pkeys));
        System.out.println(requestBuilder.get().getHits().getTotalHits());
    }

    @Test
    public void testSearchByScrol01() {
        PSearchArgsModel pSearchArgsModel = new PSearchArgsModel();
        pSearchArgsModel.setRowkey(null);
        pSearchArgsModel.setPaltaformId("0001");
        pSearchArgsModel.setName(null);
        pSearchArgsModel.setIdCard(null);
        pSearchArgsModel.setSex(1);
        pSearchArgsModel.setImage(null);
        pSearchArgsModel.setFeature(null);
        pSearchArgsModel.setThredshold(-1);
        pSearchArgsModel.setCreator("破");
        List<String> pkeys = new ArrayList<>();
        pkeys.add("1234008");
        pkeys.add("1234010");
        pkeys.add("1234015");
        pkeys.add("1234016");
        pSearchArgsModel.setPkeys(pkeys);
        pSearchArgsModel.setStart(1);
        pSearchArgsModel.setPageSize(11);
        pSearchArgsModel.setSearchId(null);
        pSearchArgsModel.setSearchType("searchByMuti");
        pSearchArgsModel.setMoHuSearch(true);

        ObjectInfoHandler objectInfoHandler = new ObjectInfoHandlerImpl();
        System.out.println(objectInfoHandler.getObjectInfo(pSearchArgsModel));
    }

    @Test
    public void testStringContains(){
        System.out.println("根据搜索条件得到的记录数是".contains("到的记"));
    }

    //获取静态信息库下面的所有rowkeys
    public List<String> getAllRowKeyOfStaticRepo() {
        List<String> findResult = new ArrayList<>();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        try {
            ResultScanner resultScanner = objectinfo.getScanner(scan);
            Iterator<Result> iterator = resultScanner.iterator();
            while (iterator.hasNext()){
                Result result = iterator.next();
                String rowKey = Bytes.toString(result.getRow());
                findResult.add(rowKey);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(objectinfo);
        }
        return findResult;
    }

    @Test
    public void testUpdateObjectInfoTime(){
//        new ObjectInfoInnerHandlerImpl().updateObjectInfoTime(getAllRowKeyOfStaticRepo());
    }

    @Test
    public void testUpdateObjectInfoTimeDemo(){
//        new ObjectInfoInnerHandlerImpl().updateObjectInfoTimeDemo(getAllRowKeyOfStaticRepo());
    }

}
