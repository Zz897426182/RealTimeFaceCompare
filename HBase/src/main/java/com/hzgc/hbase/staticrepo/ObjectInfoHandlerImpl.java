package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfo;
import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler{

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> person) {
        // 或者所有的字段fieldset，转化为List 列表的fieldlist 方便后续处理
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<String>();
        Iterator<String> it = fieldset.iterator();
        while (it.hasNext()){
            fieldlist.add(it.next());
        }

        String idcard = "";
        String pkey = "";
        // 判断身份证号是否为空，如果为空，则补全成18位字母形式的内容，用来瓶装rowkey 用。
        if (!fieldlist.contains("idcard") || person.get("idcard") == null ){
            idcard = StaticRepoUtil.getIdCardByRandom();
        } else {
            idcard = (String) person.get("idcard");
        }
        // 判断人员类型是否为空
        if (!fieldlist.contains("pkey")  || person.get("pkey") == null){
            pkey = StaticRepoUtil.getPkeyByRandom();
        } else {
            pkey = (String) person.get("pkey");
        }

        // 拼装rowkey
        String rowkey = idcard + pkey;

        // 获取table 对象，通过封装HBaseHelper 来获取
        HBaseHelper helper = new HBaseHelper();
        Table objectinfo = helper.getTable("objectinfo");
        Table feature = helper.getTable("feature");

        //构造Put 对象
        Put put = new Put(Bytes.toBytes(rowkey));

        // 添加列族属性
        for (String field:fieldlist){
            if ("photo".equals(field)){
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field), (byte[])person.get(field));
            } else {
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field), Bytes.toBytes((String) person.get(field)));
            }
        }

        // 给表格添加两个时间的字段，一个是创建时间，一个是更新时间
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);

        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("createtime"), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("updatetime"), Bytes.toBytes(dateString));


        Put putOfFeature = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes("feature"),Bytes.toBytes("feature"), (byte[]) person.get("feature"));

        // 执行Put 操作，往表格里面添加一行数据
        try {
            objectinfo.put(put);
            feature.put(putOfFeature);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } finally {
            // 关闭表格和链接对象。
            try {
                objectinfo.close();
                feature.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //    helper.close();
        }
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        HBaseHelper helper = new HBaseHelper();
        Table table = helper.getTable("objectinfo");

        //封装删除的List
        List<Delete> deletes = new ArrayList<Delete>();
        Delete delete = null;
        for (String rowkey: rowkeys){
            delete = new Delete(Bytes.toBytes(rowkey));
            deletes.add(delete);
        }

        // 执行删除操作
        try {
            table.delete(deletes);
            return  0;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int updateObjectInfo(Map<String, Object> person) {
        // 获取table 对象，通过封装HBaseHelper 来获取,先修改，然后把身份证
        HBaseHelper helper = new HBaseHelper();
        Table table = helper.getTable("objectinfo");

        // 解析穿过来的person对象,把获取字段名字。
        Set<String> fieldset = person.keySet();
        Iterator<String> it = fieldset.iterator();
        List<String> fieldlist = new ArrayList<String>();
        while (it.hasNext()){
            fieldlist.add(it.next());
        }

        // 判断是否会修改身份证号
        if (fieldlist.contains("idcard") && person.get("idcard") != null){
            Get get = new Get(Bytes.toBytes((String) person.get("id")));
            Result result = null;
            try {
                 result = table.get(get);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            result.getValue(Bytes.toBytes("person"))
        }

        // 判断是否有修改照片
        if (fieldlist.contains("photo") && person.get("photo") != null){

        }

        return 0;
    }

    @Override
    public ObjectSearchResult getObjectInfo(String platformId, String idCard,
                                            String rowkey, byte[] image, int threshold,
                                            List<String> pkeys, Map<String, String> rowClomn,
                                            long start, long pageSize, int serachId,
                                            String serachType, boolean moHuSearch) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String IdCard,
                                                          boolean moHuSearch, long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        return null;
    }

    @Override
    public ObjectSearchResult serachByCphone(String cphone) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult serachByPhotoAndThreshold(String platformId, byte[] photo,
                                                        int threshold, String feature,
                                                        long start, long pageSize) {
        return null;
    }

    @Override
    public String getEigenValue(String tag, byte[] photo) {
        return null;
    }


}
