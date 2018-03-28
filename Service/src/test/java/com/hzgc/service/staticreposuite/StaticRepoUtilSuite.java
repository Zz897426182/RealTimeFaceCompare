package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.service.staticrepo.*;
import com.hzgc.service.util.HBaseHelper;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.elasticsearch.client.Client;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class StaticRepoUtilSuite {

    // 测试往静态信息库中插入一条数据
    @Test
    public void testAddObjectInfo(){
        String platformId = "1234";
        Map<String, Object> person = new HashMap<String, Object>();
//        person.put("id","1111111111jkh11111111");
        person.put("name", "化满天");
        person.put("idcard", "1111111111jkh11111");
        person.put("sex", "1");
//       try {
//            person.put("photo", Image2Byte2Image.image2byte("E:\\1.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        person.put("reason", "赌博");
        person.put("pkey", "123456");
        person.put("creator", "羊驼");
        person.put("cphone", "18069222222");
        person.put("tag", "person");
        person.put("feature", "123455555555");

        int flag = new ObjectInfoHandlerImpl().addObjectInfo(platformId, person);
        System.out.println(flag);
    }

    //测试获取hbase 中静态信息库的总记录数
    @Test
    public void testGetTotalNums(){
//        System.out.println(new ObjectInfoInnerHandlerImpl().getTotalNums());
    }

    @Test
    public void testUpdateObjectInfo(){
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("id", "1234561111111111jkh11111");
        //      person.put("platformid","1234");
//        person.put("name", "小王炸炸");
        person.put("idcard", "1111111111jkh11111");
//        person.put("sex", "0");
//        try {
//            person.put("photo", Image2Byte2Image.image2byte("E:\\1.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        person.put("pkey", "123456");
//        person.put("reason", "赌博+暴力倾向");
        person.put("creator", "nihao");
//        person.put("feature", "123455555555");

        int flag = new ObjectInfoHandlerImpl().updateObjectInfo(person);
        System.out.println(flag);
    }

    // 测试删除静态信息库里面的一条数据
    @Test
    public void testDeleteObjectInfo() throws IOException {
//        Table tableName = HBaseHelper.getTable("objectinfo");
//        Put put = new Put(Bytes.toBytes("111111111111111111123456"));
//        put.addColumn(Bytes.toBytes("person"),Bytes.toBytes("name"),Bytes.toBytes("Liu siyang"));
//        tableName.put(put);
        List<String> rowkeys = new ArrayList<>();
        rowkeys.add("06efa837aec14fbebb1fd4435e8b9994");
        int flag = new ObjectInfoHandlerImpl().deleteObjectInfo(rowkeys);
        System.out.println(flag);
    }

    // 测试HBbase 连接
    @Test
    public void testHbaseConn(){
        Connection conn = HBaseHelper.getHBaseConnection();
        System.out.println(conn);
    }

    @Test
    public void testEsHbaseConn(){
        Client client = ElasticSearchHelper.getEsClient();
        System.out.println(client);
    }

    @Test
    public void testByGetByPlatFormIdAndIdCard(){
        ObjectInfoHandlerImpl impl = new ObjectInfoHandlerImpl();
        ObjectSearchResult searchResult = impl.searchByPlatFormIdAndIdCard("12341", "13353271701785082384",
                false, 1, 3);
        System.out.println(searchResult);
    }

    @Test
    public void testSearchByRowkey(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByRowkey("nihaomedadafdfeawdafa");
        System.out.println(objectSearchResult);
    }

    @Test
    public void testSearchByPhone(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByCphone("185546925522", 1, 5);
        System.out.println(objectSearchResult);
    }

    @Test
    public void testSearchByCreator(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByCreator("2羊驼",
                false, 1, 5);
        System.out.println(objectSearchResult.getResults().size() + " " + objectSearchResult);
    }

    @Test
    public void testSearchByName(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByName("花",
                true, 1, 5);
        System.out.println(objectSearchResult.getResults().size() + " " + objectSearchResult);
        for (Map<String, Object> person:objectSearchResult.getResults()){
            System.out.println(person.get("name"));
        }
    }

    @Test
    public void testGetPhotoByKey() throws IOException {
        byte[] photo = new ObjectInfoHandlerImpl().getPhotoByKey("nihaomedadafdfeawdafa");
        System.out.println(photo);
        Image2Byte2Image.byte2image(photo, "C:\\Users\\lenovo\\Desktop\\nkkma.png");
    }

   @Test
    public void testimpl(){
//        ObjectInfoInnerHandlerImpl objectInfoInnerHandler = new ObjectInfoInnerHandlerImpl();
//        List<String> a = new ArrayList<>();
//        a.add("223458");
//        a.add("123456");
//        List<String> b = objectInfoInnerHandler.searchByPkeys(a);
//        System.out.println(b);
    }
    @Test
    public void testGetPhotoByRowkey() throws IOException {
       ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
        String rk = "6c18bbb907664f499199f1ffb4338520";
        byte[] photo = objectInfoHandler.getPhotoByKey(rk);
        System.out.println(photo);
        Image2Byte2Image.byte2image(photo, "C:\\nika1.png");
    }
    @Test
    public void testGetRocordOfObjectInfo(){
        SearchRecordHandlerImpl searchRecordHandler = new SearchRecordHandlerImpl();
        String rk = "101b568b87fc4d19aac1a0e2b00e5ae5";
        ObjectSearchResult o = searchRecordHandler.getRocordOfObjectInfo(rk,1,3);
        System.out.println(o);
    }

    @Test
    public void testSpilitToFindCol(){
        String demo = "keyvalues={a4495983547d4bec828a6b31e12bea80/person:cphone/1501481043631/Put/vlen=12/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:createtime/1501481043631/Put/vlen=19/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:creator/1501481043631/Put/vlen=9/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:feature/1501481043631/Put/vlen=12/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:idcard/1501481043631/Put/vlen=20/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:name/1501481043631/Put/vlen=9/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:photo/1501481043631/Put/vlen=0/seqid=0," +
                " a4495983547d4bec828a6b31e12bea80/person:pkey/1501481043631/Put/vlen=6/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:platformId/1501481043631/Put/vlen=4/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:reason/1501481043631/Put/vlen=6/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:sex/1501481043631/Put/vlen=1/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:tag/1501481043631/Put/vlen=6/seqid=0, " +
                "a4495983547d4bec828a6b31e12bea80/person:updatetime/1501481043631/Put/vlen=19/seqid=0}";
        String[] demos = demo.split(":");
        List<String> clos = new ArrayList<>();
        for (int i = 1;i < demos.length; i ++){
            System.out.println(demos[i].substring(0, demos[i].indexOf("/")));
            clos.add(demos[i].substring(0, demos[i].indexOf("/")));
        }
    }
    @Test
    public void testgetSearchPhoto(){
        SearchRecordHandlerImpl searchRecordHandler = new SearchRecordHandlerImpl();
        String rk = "fd62eda051c54cfmmnc7df";
        byte[] a = searchRecordHandler.getSearchPhoto(rk);
        System.out.println(a);
    }

    public static long rowCount(String tableName, String family) throws IOException {
        AggregationClient ac = new AggregationClient(HBaseHelper.getHBaseConfiguration());
        String coprocessorClassName = "org.apache.hadoop.hbase.coprocessor.AggregateImplementation";
        Admin admin = HBaseHelper.getHBaseConnection().getAdmin();
        TableName tableName1 = TableName.valueOf(tableName);
        HTableDescriptor htd = admin.getTableDescriptor(tableName1);
        boolean flag = htd.hasCoprocessor(coprocessorClassName);// 有就是true 没有就是 false
        if (!flag) {
            admin.disableTable(tableName1);
            htd.addCoprocessor(coprocessorClassName);
            admin.modifyTable(tableName1, htd);
            admin.enableTable(tableName1);
        }
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes(family));
        long rowCount = 0;
        final LongColumnInterpreter longColumnInterpreter = new LongColumnInterpreter();
        try {
            rowCount = ac.rowCount(TableName.valueOf(tableName), longColumnInterpreter, scan);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return rowCount;
    }

    @Test
    public void testRowCount() throws IOException {
        System.out.println(rowCount("objectinfo", "person"));
    }
}
