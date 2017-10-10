package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import com.hzgc.util.PinYinUtil;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.log4j.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler {

    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

    private Random random = new Random();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Map<String, Map<String, Object>> BASE_LIBRARY = getAllObjectInfo();

    public ObjectInfoHandlerImpl() {
        NativeFunction.init();
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> person) {
        long start = System.currentTimeMillis();
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<>();
        fieldlist.addAll(fieldset);
        String idcard = (String) person.get(ObjectInfoTable.IDCARD);
        String pkey = (String) person.get(ObjectInfoTable.PKEY);
        if (idcard == null || idcard.length() != 18) {
            idcard = (random.nextInt(900000000) + 100000000) + ""
                    + (random.nextInt(900000000) + 100000000);
        }
        String rowkey = pkey + idcard;
        LOG.info("addObjectInfo, rowkey: " + rowkey);
        List<Put> puts = new ArrayList<>();
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        //构造Put 对象
        Put put = new Put(Bytes.toBytes(rowkey));
        put.setDurability(Durability.ASYNC_WAL);
        // 添加列族属性
        for (String field : fieldlist) {
            if (ObjectInfoTable.PHOTO.equals(field)) {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else if (ObjectInfoTable.FEATURE.equals(field)) {
                try {
                    put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                            ((String) person.get(field)).getBytes("ISO8859-1"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
            if (ObjectInfoTable.NAME.equals(field)){
                person.put(ObjectInfoTable.NAME_PIN, PinYinUtil.toHanyuPinyin((String) person.get(field)));
            }
            if (ObjectInfoTable.CREATOR.equals(field)){
                person.put(ObjectInfoTable.CREATOR_PIN, PinYinUtil.toHanyuPinyin((String) person.get(field)));
            }
        }
        // 给表格添加两个时间的字段，一个是创建时间，一个是更新时间
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.CREATETIME), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.PLATFORMID), Bytes.toBytes(platformId));
        //在ES中执行数据同步插入操作
        person.remove(ObjectInfoTable.PHOTO);
        //同步到内存
        person.put(ObjectInfoTable.ROWKEY, rowkey);
        person.putIfAbsent(ObjectInfoTable.PLATFORMID, platformId);
        BASE_LIBRARY.put(rowkey, person);
        ElasticSearchHelper.getEsClient()
                .prepareIndex(ObjectInfoTable.TABLE_NAME, ObjectInfoTable.PERSON_COLF, rowkey)
                .setSource(person).get();
        // 执行Put 操作，往表格里面添加一行数据
        try {
            puts.add(put);
            //总记录数加1，用于标志HBase 数据库中的数据有变动
            Put putOfTNums = new Put(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            putOfTNums.setDurability(Durability.ASYNC_WAL);
            Get getOfTNums = new Get(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            Result resultTNums = objectinfo.get(getOfTNums);
            long tatalNums = Bytes.toLong(resultTNums.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS)));
            putOfTNums.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS),
                    Bytes.toBytes(tatalNums + 1));
            puts.add(putOfTNums);

            objectinfo.put(puts);
            LOG.info("Add a single record to success!");
            return 0;
        } catch (IOException e) {
            LOG.error("Add a single record to failed!");
            e.printStackTrace();
            return 1;
        } finally {
            // 关闭表格和连接对象。
            HBaseUtil.closTable(objectinfo);
            LOG.info("function[addObjectInfo] total time: " + (System.currentTimeMillis() - start));
        }
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        long start = System.currentTimeMillis();
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        List<Delete> deletes = new ArrayList<>();
        for (String rowkey : rowkeys) {
            //删除内存中rowkey
            BASE_LIBRARY.remove(rowkey);
            LOG.info("delete object info, the rowkey is: " + rowkey);
            Delete delete = new Delete(Bytes.toBytes(rowkey));
            delete.setDurability(Durability.ASYNC_WAL);
            deletes.add(delete);
            //在ES中执行同步删除操作
            ElasticSearchHelper.getEsClient()
                    .prepareDelete(ObjectInfoTable.TABLE_NAME, ObjectInfoTable.PERSON_COLF, rowkey)
                    .get();
        }
        // 执行删除操作
        try {
            //总记录数减1，用于标志HBase 数据库中的数据有变动
            Put put = new Put(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            put.setDurability(Durability.ASYNC_WAL);
            Get getOfTNums = new Get(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            Result resultTNums = table.get(getOfTNums);
            long tatalNums = Bytes.toLong(resultTNums.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS)));
            put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS),
                    Bytes.toBytes(tatalNums - 1));
            table.delete(deletes);
            table.put(put);
            LOG.info("object info delete successed!");
            return 0;
        } catch (IOException e) {
            LOG.error("object info delete failed!");
            e.printStackTrace();
            return 1;
        } finally {
            //关闭表连接
            HBaseUtil.closTable(table);
            LOG.info("function[deleteObjectInfo] total time： " + (System.currentTimeMillis() - start));
        }
    }

    private long getTotalNums(String tableName, String family) {
        long start = System.currentTimeMillis();
        AggregationClient ac = new AggregationClient(HBaseHelper.getHBaseConfiguration());
        String coprocessorClassName = "org.apache.hadoop.hbase.coprocessor.AggregateImplementation";
        Admin admin;
        long rowCount = 0;
        try {
            admin = HBaseHelper.getHBaseConnection().getAdmin();
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
            final LongColumnInterpreter longColumnInterpreter = new LongColumnInterpreter();
            try {
                rowCount = ac.rowCount(TableName.valueOf(tableName), longColumnInterpreter, scan);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("function[getTotalNums] total time: " + (System.currentTimeMillis() - start));
        return rowCount;
    }

    @Override
    public int updateObjectInfo(Map<String, Object> person) {
        long start = System.currentTimeMillis();
        if (person == null || person.size() == 0) {
            LOG.info("updateObjectInfo, the person cannot be null");
            return 1;
        }
        String id = (String) person.get(ObjectInfoTable.ROWKEY);
        if (id == null) {
            LOG.info("updateObjectInfo, rowKey cannot be null");
            return 1;
        }

        //修改加载到内存中的数据......
        Map<String, Object> tempMap = BASE_LIBRARY.get(id);
        if (tempMap == null) {
            LOG.info("the person not exists in base library");
            return 1;
        }

        tempMap.putAll(person);
        BASE_LIBRARY.put(id, tempMap);

        // 获取table 对象，通过封装HBaseHelper 来获取
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<>();
        fieldlist.addAll(fieldset);
        Get get = new Get(Bytes.toBytes(id));
        Result result_tmp;
        String originIdCard = "";
        String originPKey = "";
        try {
            result_tmp = table.get(get);
            originIdCard = Bytes.toString(result_tmp.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.IDCARD)));
            originPKey = Bytes.toString(result_tmp.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.PKEY)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Put put = new Put(Bytes.toBytes(id));
        put.setDurability(Durability.ASYNC_WAL);
        for (String field : fieldlist) {
            if (ObjectInfoTable.PHOTO.equals(field)) {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else if (ObjectInfoTable.FEATURE.equals(field)) {
                try {
                    put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                            ((String) person.get(field)).getBytes("ISO8859-1"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
            if (ObjectInfoTable.NAME.equals(field)){
                person.put(ObjectInfoTable.NAME_PIN, PinYinUtil.toHanyuPinyin((String) person.get(field)));
            }
            if (ObjectInfoTable.CREATOR.equals(field)){
                person.put(ObjectInfoTable.CREATOR_PIN, PinYinUtil.toHanyuPinyin((String) person.get(field)));
            }
        }

        Date date = new Date();
        String dateString = format.format(date);

        Map<String, Object> dataToEs = new HashMap<>();
        dataToEs.putAll(person);
        dataToEs.put(ObjectInfoTable.UPDATETIME, dateString);
        dataToEs.remove(ObjectInfoTable.PHOTO);

        ElasticSearchHelper.getEsClient()
                .prepareUpdate(ObjectInfoTable.TABLE_NAME, ObjectInfoTable.PERSON_COLF, id)
                .setDoc(dataToEs).get();


        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
        Map<String, Object> map = new HashMap<>();
        try {
            table.put(put);
            //总记录数加1，用于标志HBase 数据库中的数据有变动
            //获取当前条数
            Get getOfTNums = new Get(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            Result resultTNums = table.get(getOfTNums);
            long tatalNums = Bytes.toLong(resultTNums.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS)));
            //更新当前条数
            Put putOfTNums = new Put(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            putOfTNums.setDurability(Durability.ASYNC_WAL);
            putOfTNums.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS),
                    Bytes.toBytes(tatalNums + 1));
            table.put(putOfTNums);

            LOG.info("function[updateObjectInfo], not include IDCard and pkey, the time：" + (System.currentTimeMillis() - start));
            if ((fieldlist.contains(ObjectInfoTable.IDCARD) && !person.get(ObjectInfoTable.IDCARD).equals(originIdCard))
                    || (fieldlist.contains(ObjectInfoTable.PKEY) && !person.get(ObjectInfoTable.PKEY).equals(originPKey))) {
                Result result = table.get(get);

                String idCard = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD)));
                String pKey = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY)));
                //传过来的IDCard
                String personIdCard = (String) person.get(ObjectInfoTable.IDCARD);
                if ((personIdCard != null && !"".equals(personIdCard))){
                    idCard = personIdCard;
                }
                String personPkey =  (String) person.get(ObjectInfoTable.PKEY);
                if ((personPkey != null && !"".equals(personPkey))){
                    pKey = personPkey;
                }
                String newRowKey = pKey + idCard;

                // 移除内存中原先数据，然后添加更新之后的数据。
                Map<String, Object> mapInMemery = BASE_LIBRARY.get(id);
                mapInMemery.put(ObjectInfoTable.ROWKEY, newRowKey);
                BASE_LIBRARY.remove(id);
                BASE_LIBRARY.put(newRowKey, mapInMemery);

                //新的rowkey返回到ES中
                String name = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.NAME)));
                String creator = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATOR)));
                map.put(ObjectInfoTable.NAME, name);
                map.put(ObjectInfoTable.NAME_PIN, PinYinUtil.toHanyuPinyin(name));
                map.put(ObjectInfoTable.CREATOR, creator);
                map.put(ObjectInfoTable.CREATOR_PIN, PinYinUtil.toHanyuPinyin(creator));

                map.put(ObjectInfoTable.IDCARD, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD))));
                map.put(ObjectInfoTable.PKEY, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY))));
                map.put(ObjectInfoTable.PLATFORMID, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PLATFORMID))));
                map.put(ObjectInfoTable.TAG, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.TAG))));
                map.put(ObjectInfoTable.SEX, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.SEX))));
                map.put(ObjectInfoTable.FEATURE, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE))));
                map.put(ObjectInfoTable.REASON, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.REASON))));
                map.put(ObjectInfoTable.CPHONE, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CPHONE))));
                map.put(ObjectInfoTable.CREATETIME, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATETIME))));
                map.put(ObjectInfoTable.UPDATETIME, Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.UPDATETIME))));

                ElasticSearchHelper.getEsClient()
                        .prepareIndex(ObjectInfoTable.TABLE_NAME, ObjectInfoTable.PERSON_COLF, newRowKey)
                        .setSource(map).get();
                //将数据存放到HBase
                Put put1 = new Put(Bytes.toBytes(newRowKey));
                put1.setDurability(Durability.ASYNC_WAL);
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PLATFORMID),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PLATFORMID)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.TAG),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.TAG)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PKEY),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PKEY)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.NAME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.NAME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.SEX),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.SEX)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PHOTO),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PHOTO)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.FEATURE),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.FEATURE)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.REASON),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.REASON)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.CREATOR),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CREATOR)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.CPHONE),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CPHONE)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.CREATETIME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CREATETIME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.UPDATETIME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.UPDATETIME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD), Bytes.toBytes(idCard));
                table.put(put1);
                //将ES中的原数据删除
                ElasticSearchHelper.getEsClient()
                        .prepareDelete(ObjectInfoTable.TABLE_NAME, ObjectInfoTable.PERSON_COLF, id).get();
                //将HBase的原数据删除
                Delete delete = new Delete(Bytes.toBytes(id));
                delete.setDurability(Durability.ASYNC_WAL);
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PLATFORMID));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.TAG));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.NAME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.SEX));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PHOTO));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.REASON));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATOR));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CPHONE));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATETIME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.UPDATETIME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.RELATED));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.ROWKEY));
                table.delete(delete);
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("updateObjectInfo, failed!");
            return 1;
        } finally {
            //关闭表连接
            HBaseUtil.closTable(table);
            LOG.info("function[updateObjectInfo], include idcard and pkey, the time：" + (System.currentTimeMillis() - start));
        }
    }

    @Override
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        long start = System.currentTimeMillis();
        ObjectSearchResult objectSearchResult;
        switch (pSearchArgsModel.getSearchType()) {
            case "searchByPlatFormIdAndIdCard": {
                objectSearchResult = searchByPlatFormIdAndIdCard(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getIdCard(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByRowkey": {
                objectSearchResult = searchByRowkey(pSearchArgsModel.getRowkey());
                break;
            }
            case "searchByCphone": {
                objectSearchResult = searchByCphone(pSearchArgsModel.getCphone(), pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByCreator": {
                objectSearchResult = searchByCreator(pSearchArgsModel.getCreator(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByName": {
                objectSearchResult = searchByName(pSearchArgsModel.getName(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize());
                break;
            }
            default: {
                objectSearchResult = searchByMutiCondition(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getIdCard(), pSearchArgsModel.getName(),
                        pSearchArgsModel.getSex(), pSearchArgsModel.getImage(), pSearchArgsModel.getFeature(),
                        pSearchArgsModel.getThredshold(), pSearchArgsModel.getPkeys(),
                        pSearchArgsModel.getCreator(), pSearchArgsModel.getCphone(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize(),
                        pSearchArgsModel.isMoHuSearch());
                break;
            }
        }
        LOG.info("funtion[getObjectInfo], total search time: " + (System.currentTimeMillis() - start));
        return objectSearchResult;
    }

    //多条件查询
    private ObjectSearchResult searchByMutiCondition(String platformId, String idCard, String name, int sex,
                                                     byte[] photo, String feature, float threshold,
                                                     List<String> pkeys, String creator, String cphone,
                                                     int start, int pageSize, boolean moHuSearch) {
        SearchRequestBuilder requestBuilder;
        if (start == -1) {
            start = 1;
        }
        if (pageSize == -1) {
            pageSize = 100;
        }
        if (photo != null && feature != null) {
            // 现根据内存中的静态信息库进行过滤
            Map<String, Map<String, Object>> filteredBaseRepo = new HashMap<>();
            filteredBaseRepo.putAll(BASE_LIBRARY);
            long startTime = System.currentTimeMillis();
            if (platformId != null && platformId.length() >0){
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()){
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String platformId_tmp = (String) person.get(ObjectInfoTable.PLATFORMID);
                    if (!platformId.equals(platformId_tmp)) {
                        it_map.remove();
                    }
                }
            }

            if (pkeys != null && pkeys.size() > 0) {
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()){
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String pkey_tmp = (String) person.get(ObjectInfoTable.PKEY);
                    if (!pkeys.contains(pkey_tmp)) {
                        it_map.remove();
                    }
                }
            }

            if (sex != -1) {
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()){
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String sex_str = (String) person.get(ObjectInfoTable.SEX);
                    int sex_tmp = -1;
                    if (sex_str !=null && sex_str.length() > 0){
                        sex_tmp = Integer.parseInt(sex_str);
                    }
                    if (sex != sex_tmp) {
                        it_map.remove();
                    }
                }
            }

            if (idCard != null && idCard.length() > 0) {
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()){
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String idCard_tmp = (String) person.get(ObjectInfoTable.IDCARD);
                    if (idCard_tmp == null || idCard_tmp.length() == 0
                            || !Pattern.matches("\\d{0,18}" + idCard + "\\d{0,18}", idCard_tmp)) {
                        it_map.remove();
                    }
                }
            }
            if (creator != null && creator.length() > 0) {
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()) {
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String creator_tmp = (String) person.get(ObjectInfoTable.CREATOR);
                    String pattern = "[-_A-Za-z0-9\\u4e00-\\u9fa5]{0,30}" + creator
                            + "[-_A-Za-z0-9\\u4e00-\\u9fa5]{0,30}";
                    if (creator_tmp == null || creator_tmp.length() == 0
                            || !Pattern.matches(pattern, creator_tmp)) {
                        it_map.remove();
                    }
                }
            }
            if (name != null && name.length() > 0 ) {
                Iterator<Map.Entry<String, Map<String, Object>>> it_map = filteredBaseRepo.entrySet().iterator();
                while (it_map.hasNext()) {
                    Map.Entry<String, Map<String, Object>> person_map = it_map.next();
                    Map<String, Object> person = person_map.getValue();
                    String name_tmp = (String) person.get(ObjectInfoTable.NAME);
                    String pattern = "[-_A-Za-z0-9\\u4e00-\\u9fa5]{0,30}" + name
                            + "[-_A-Za-z0-9\\u4e00-\\u9fa5]{0,30}";
                    if (name_tmp == null || name_tmp.length() == 0
                            || !Pattern.matches(pattern, name_tmp)) {
                        it_map.remove();
                    }
                }
            }
            LOG.info("以图搜图的时候，先根据条件进行过滤花费时间是：" + (System.currentTimeMillis() - startTime));
            ObjectSearchResult objectSearchResult = searchByPhotoAndThreshold(filteredBaseRepo,threshold, feature);
            objectSearchResult = HBaseUtil.dealWithPaging(objectSearchResult, start, pageSize);
            putSearchRecordToHBase(platformId, objectSearchResult, photo);
            return objectSearchResult;
        }

        // 以下是处理没有图片的情况下的搜索
        requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setFetchSource(null, new String[]{ObjectInfoTable.FEATURE})
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(ObjectInfoTable.NAME, SortOrder.DESC)
                .addSort(ObjectInfoTable.IDCARD, SortOrder.DESC)
                .addSort(ObjectInfoTable.UPDATETIME, SortOrder.DESC)
                .setFrom(start - 1).setSize(pageSize);
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();

        // 传入平台ID ，必须是确定的
        if (platformId != null) {
            booleanQueryBuilder.must(QueryBuilders.termQuery(ObjectInfoTable.PLATFORMID, platformId));
        }
        // 性别要么是1，要么是0，即要么是男，要么是女
        if (sex != -1) {
            booleanQueryBuilder.must(QueryBuilders.termQuery(ObjectInfoTable.SEX, sex));
        }
        // 多条件下，输入手机号，只支持精确的手机号
        if (cphone != null) {
            booleanQueryBuilder.must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.CPHONE, cphone)
                    .analyzer("standard"));
        }
        // 人员类型，也是精确的lists
        if (pkeys != null && pkeys.size() > 0) {
            booleanQueryBuilder.must(QueryBuilders.termsQuery(ObjectInfoTable.PKEY, pkeys));
        }
        // 身份证号可以是模糊的
        if (idCard != null) {
            if (moHuSearch) {
                booleanQueryBuilder.must(QueryBuilders.matchQuery(ObjectInfoTable.IDCARD, idCard));
            } else {
                booleanQueryBuilder.must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.IDCARD, idCard)
                        .analyzer("standard"));
            }
        }
        // 名字可以是模糊的
        if (name != null) {
            if (moHuSearch) {
                booleanQueryBuilder.must(QueryBuilders.matchQuery(ObjectInfoTable.NAME_PIN,
                        PinYinUtil.toHanyuPinyin(name)));
            } else {
                booleanQueryBuilder.must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.NAME, name));
            }
        }
        // 创建者姓名可以是模糊的
        if (creator != null) {
            if (moHuSearch) {
                booleanQueryBuilder.must(QueryBuilders.matchQuery(ObjectInfoTable.CREATOR_PIN,
                        PinYinUtil.toHanyuPinyin(creator)));
            } else {
                booleanQueryBuilder.must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.CREATOR, creator));
            }
        }
        requestBuilder.setQuery(booleanQueryBuilder);
        // 后续，根据查出来的人员信息，如果有图片，特征值，以及阈值，（则调用算法进行比对，得出相似度比较高的）
        // 由或者多条件查询里面不支持传入图片以及阈值，特征值。
        // 对返回结果进行处理
        // 只有搜索条件的情况下。
        //处理搜索的数据,根据是否需要分页进行返回
        ObjectSearchResult objectSearchResult_Tmp = dealWithSearchRequesBuilder(platformId, requestBuilder, photo,
                null, null,
                start, pageSize, moHuSearch);

        List<Map<String,Object>> tempList = objectSearchResult_Tmp.getResults();
        Iterator<Map<String, Object>> it = tempList.iterator();
        while (it.hasNext()){
            Map<String, Object> person = it.next();
            String name_tmp = (String) person.get(ObjectInfoTable.NAME );
            String creator_tmp = (String) person.get(ObjectInfoTable.CREATOR);
            if (name != null && !"".equals(name)){
                if (name_tmp == null || "".equals(name_tmp) || (!name_tmp.contains(name))){
                    it.remove();
                }
            }
            if (creator != null && !"".equals(creator)){
                if (creator_tmp == null || "".equals(creator_tmp) || (!creator_tmp.contains(creator))){
                    it.remove();
                }
            }
        }

        ObjectSearchResult tmp = HBaseUtil.dealWithPaging(objectSearchResult_Tmp, start, pageSize);
        putSearchRecordToHBase(platformId, tmp, null);
        return tmp;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String idCard,
                                                          boolean moHuSearch, int start, int pageSize) {
        return searchByRowkey(platformId + idCard);
    }

    /**
     * 获取数据待优化
     *
     * @param rowkey 标记一条对象信息的唯一标志。
     */
    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result;
        ObjectSearchResult searchResult = new ObjectSearchResult();
        boolean tableExits;
        String searchRowkey = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchRowkey);
        try {
            tableExits = table.exists(get);
            if (tableExits) {
                result = table.get(get);
                String[] tmp = result.toString().split(":");
                List<String> cols = new ArrayList<>();
                for (int i = 1; i < tmp.length; i++) {
                    cols.add(tmp[i].substring(0, tmp[i].indexOf("/")));
                }
                Map<String, Object> person = new HashMap<>();
                List<Map<String, Object>> hits = new ArrayList<>();
                for (String col : cols) {
                    String value = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                            Bytes.toBytes(col)));
                    person.put(col, value);
                }
                hits.add(person);
                searchResult.setResults(hits);
                searchResult.setSearchStatus(0);
                searchResult.setPhotoId(null);
                searchResult.setSearchNums(1);
            } else {
                searchResult.setResults(null);
                searchResult.setSearchStatus(0);
                searchResult.setSearchNums(0);
                searchResult.setPhotoId(null);
            }
            return searchResult;
        } catch (IOException e) {
            LOG.info("根据rowkey获取对象信息的时候异常............");
            searchResult.setSearchStatus(1);
            e.printStackTrace();
            putSearchRecordToHBase(null, searchResult, null);
            return searchResult;
        } finally {
            HBaseUtil.closTable(table);
        }
    }

    @Override
    public ObjectSearchResult searchByCphone(String cphone, int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setFetchSource(null, new String[]{ObjectInfoTable.FEATURE})
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(QueryBuilders.termQuery(ObjectInfoTable.CPHONE, cphone))
                .addSort(ObjectInfoTable.NAME, SortOrder.DESC)
                .addSort(ObjectInfoTable.IDCARD, SortOrder.DESC)
                .addSort(ObjectInfoTable.UPDATETIME, SortOrder.DESC)
                .setFrom(start - 1).setSize(1000);
        return dealWithSearchRequesBuilder(null, requestBuilder, null,
                null, null,
                start, pageSize, false);
    }

    // 处理精确查找下，IK 分词器返回多余信息的情况，
    // 比如只需要小王炸，但是返回了小王炸 和小王炸小以及小王炸大的情况
    private void dealWithCreatorAndNameInNoMoHuSearch(ObjectSearchResult searchResult, String searchType,
                                                      String nameOrCreator,
                                                      boolean moHuSearch) {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> exectResult = new ArrayList<>();
        List<Map<String, Object>> tempList = searchResult.getResults();
        if (!moHuSearch && tempList != null && (ObjectInfoTable.CREATOR.equals(searchType) // 处理精确查找，按照中文分词器查找的情况下
                || ObjectInfoTable.NAME.equals(searchType))) {                               // （模糊查找），返回的数据过多的情况，
            for (Map<String, Object> objectMap : tempList) {
                String temp = null;
                if (ObjectInfoTable.CREATOR.equals(searchType)) {
                    temp = (String) objectMap.get(ObjectInfoTable.CREATOR);
                } else if (ObjectInfoTable.NAME.equals(searchType)) {
                    temp = (String) objectMap.get(ObjectInfoTable.NAME);
                }
                if (temp != null && temp.equals(nameOrCreator)) {
                    exectResult.add(objectMap);
                }
            }
            searchResult.setResults(exectResult);
            searchResult.setSearchNums(exectResult.size());
        } else if (moHuSearch && tempList != null && (ObjectInfoTable.CREATOR.equals(searchType) // 处理同拼音的情况，李，理，离，张，章等
                || ObjectInfoTable.NAME.equals(searchType))) {
            for (Map<String, Object> objectMap : tempList) {
                String temp = null;
                if (ObjectInfoTable.CREATOR.equals(searchType)) {
                    temp = (String) objectMap.get(ObjectInfoTable.CREATOR);
                } else if (ObjectInfoTable.NAME.equals(searchType)) {
                    temp = (String) objectMap.get(ObjectInfoTable.NAME);
                }
                if (temp != null) {
                    for (int i = 0; i < nameOrCreator.length(); i++) {
                        if (temp.contains(String.valueOf(nameOrCreator.charAt(i)))) {
                            exectResult.add(objectMap);
                            break;
                        }
                    }
                }
            }
            searchResult.setResults(exectResult);
            searchResult.setSearchNums(exectResult.size());
        }
        LOG.info("dealWithCreatorAndNameInNoMoHuSearch, time: " + (System.currentTimeMillis() - start));
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setFetchSource(null, new String[]{ObjectInfoTable.FEATURE})
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(ObjectInfoTable.NAME, SortOrder.DESC)
                .addSort(ObjectInfoTable.IDCARD, SortOrder.DESC)
                .addSort(ObjectInfoTable.UPDATETIME, SortOrder.DESC)
                .setFrom(start - 1).setSize(1000);
        if (moHuSearch) {
            requestBuilder.setQuery(QueryBuilders.matchQuery(ObjectInfoTable.CREATOR_PIN, PinYinUtil.toHanyuPinyin(creator)));
        } else {
            requestBuilder.setQuery(QueryBuilders.matchPhraseQuery(ObjectInfoTable.CREATOR, creator));
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(null, requestBuilder, null,
                ObjectInfoTable.CREATOR, creator,
                start, pageSize, moHuSearch);
        List<Map<String,Object>> tempList = searchResult.getResults();
        Iterator<Map<String, Object>> it = tempList.iterator();
        while (it.hasNext()){
            Map<String, Object> person = it.next();
            String creator_tmp = (String) person.get(ObjectInfoTable.CREATOR );
            if (creator != null && !"".equals(creator)){
                if (creator_tmp == null || "".equals(creator_tmp) || (!creator_tmp.contains(creator))){
                    it.remove();
                }
            }
        }
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setFetchSource(null, new String[]{ObjectInfoTable.FEATURE})
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(ObjectInfoTable.NAME, SortOrder.DESC)
                .addSort(ObjectInfoTable.IDCARD, SortOrder.DESC)
                .addSort(ObjectInfoTable.UPDATETIME, SortOrder.DESC)
                .setFrom(start - 1).setSize(1000);
        if (moHuSearch) {
            requestBuilder.setQuery(QueryBuilders.matchQuery(ObjectInfoTable.NAME_PIN, PinYinUtil.toHanyuPinyin(name)));
        } else {
            requestBuilder.setQuery(QueryBuilders.matchPhraseQuery(ObjectInfoTable.NAME, name));
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(null, requestBuilder, null,
                ObjectInfoTable.NAME, name,
                start, pageSize, moHuSearch);
        List<Map<String,Object>> tempList = searchResult.getResults();
        Iterator<Map<String, Object>> it = tempList.iterator();
        while (it.hasNext()){
            Map<String, Object> person = it.next();
            String name_tmp = (String) person.get(ObjectInfoTable.NAME );
            if (name != null && !"".equals(name)){
                if (name_tmp == null || "".equals(name_tmp) || (!name_tmp.contains(name))){
                    it.remove();
                }
            }
        }

        return searchResult;
    }

    private static Map<String, Map<String, Object>> getAllObjectInfo() {
        long start = System.currentTimeMillis();
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setScroll(new TimeValue(300000)).setSize(5000);
        requestBuilder.setQuery(QueryBuilders.matchAllQuery());
        SearchResponse response = requestBuilder.get();
        Map<String, Map<String, Object>> results = new HashMap<>();
        do {
            SearchHits hits = response.getHits();
            SearchHit[] searchHits = hits.getHits();
            if (searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    Map<String, Object> source = hit.getSource();
                    source.put(ObjectInfoTable.ROWKEY, hit.getId());
                    // ES 的文档名，对应着HBase 的rowkey
                    results.put(hit.getId(), source);
                }
            }
            response = ElasticSearchHelper.getEsClient().prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(300000))
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
        LOG.info("getAllObjectINfo, time: " + (System.currentTimeMillis() - start));
        return results;
    }

    private ObjectSearchResult searchByPhotoAndThreshold(Map<String, Map<String, Object>> filteredMap,
                                                         float threshold, String feature) {
        long start_time = System.currentTimeMillis();
        List<Map<String, Object>> resultsFinal = new ArrayList<>();
        if (filteredMap != null && filteredMap.size() > 0 && feature.length() == 2048){
            Set<String> tempSet = filteredMap.keySet();
            for (String rk : tempSet) {
                String histFeature = (String) filteredMap.get(rk).get(ObjectInfoTable.FEATURE);
                if (histFeature != null && histFeature.length() == 2048) {
                    float sim = FaceFunction.featureCompare(feature, histFeature);
                    boolean pp = sim > threshold;
                    if (pp) {
                        Map<String, Object> temMap = new HashMap<>();
                        temMap.putAll(filteredMap.get(rk));
                        temMap.put(ObjectInfoTable.RELATED, sim);
                        resultsFinal.add(temMap);
                    }
                }
            }
        }
        String searchId = UUID.randomUUID().toString().replace("-", "");
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        objectSearchResult.setSearchId(searchId); // searchId
        objectSearchResult.setSearchStatus(0);  // status
        objectSearchResult.setSearchNums(resultsFinal.size());   // results nums
        // 按照相似度从大小排序
        resultsFinal.sort((o1, o2) -> {
            float relate01 = (float) o1.get(ObjectInfoTable.RELATED);
            float relate02 = (float) o2.get(ObjectInfoTable.RELATED);
            return Float.compare(relate02, relate01);
        });
        objectSearchResult.setResults(resultsFinal);  // results
        objectSearchResult.setPhotoId(searchId);   // photoId
        LOG.info("searchByPhotoAndThreshold, time: " + (System.currentTimeMillis() - start_time));
        return objectSearchResult;

    }

    @Override
    public ObjectSearchResult searchByPhotoAndThreshold(String platformId,
                                                        byte[] photo,
                                                        float threshold,
                                                        String feature,
                                                        long start,
                                                        long pageSize) {
        return searchByPhotoAndThreshold(BASE_LIBRARY,threshold, feature);
    }

    @Override
    public String getFeature(String tag, byte[] photo) {
        long start = System.currentTimeMillis();
        float[] floatFeature = FaceFunction.featureExtract(photo);
        String feature = "";
        if (floatFeature != null && floatFeature.length == 512) {
            feature = FaceFunction.floatArray2string(floatFeature);
        }
        LOG.info("getFeature, time: " + (System.currentTimeMillis() - start));
        return feature;
    }

    @Override
    public byte[] getPhotoByKey(String rowkey) {
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        get.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PHOTO));
        Result result;
        byte[] photo;
        try {
            result = table.get(get);
            photo = result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
        } catch (IOException e) {
            LOG.error("get data from table failed!");
            e.printStackTrace();
            return null;
        } finally {
            HBaseUtil.closTable(table);
        }
        return photo;
    }

    // 保存历史查询记录
    private void putSearchRecordToHBase(String platformId, ObjectSearchResult searchResult, byte[] photo) {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        byte[] results = null;
        if (searchResult != null) {
            List<Map<String, Object>> persons = searchResult.getResults();
            if (persons != null) {
                for (Map<String, Object> person : persons) {
                    Iterator<Map.Entry<String, Object>> it = person.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Object> entry = it.next();
                        String key = entry.getKey();
                        if (ObjectInfoTable.FEATURE.equals(key)) {
                            it.remove();
                        }
                    }
                }
            }
            try {
                oout = new ObjectOutputStream(bout);
                oout.writeObject(new ArrayList(searchResult.getResults()));
                results = bout.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oout != null) {
                        oout.close();
                    }
                    bout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (searchResult != null) {
            Table table = HBaseHelper.getTable(SrecordTable.TABLE_NAME);
            String srecordRowKey = searchResult.getSearchId();
            if (srecordRowKey == null) {
                LOG.info("putSearchRecordToHBase, failed:  rowkey cannnot be null.");
                return;
            }
            Put put = new Put(Bytes.toBytes(srecordRowKey));
            put.setDurability(Durability.ASYNC_WAL);
            LOG.info("srecord rowkey is:  " + searchResult.getSearchId());
            put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.SEARCH_STATUS),
                    Bytes.toBytes(searchResult.getSearchStatus()))
                    .addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.SEARCH_NUMS),
                            Bytes.toBytes(searchResult.getSearchNums()));
            if (platformId != null) {
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PLATFORM_ID),
                        Bytes.toBytes(platformId));
            }
            if (searchResult.getPhotoId() != null) {
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTOID),
                        Bytes.toBytes(searchResult.getPhotoId()));
            }
            if (results != null) {
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.RESULTS), results);
            }
            if (photo != null) {
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTO), photo);
            }
            try {
                table.put(put);
            } catch (IOException e) {
                LOG.info("excute putSearchRecordToHBase failed.");
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(table);
                LOG.info("putSearchRecordToHBase, time: " + (System.currentTimeMillis() - start));
            }
        }
    }

    // 根据ES的SearchRequesBuilder 来查询，并封装返回结果
    private ObjectSearchResult dealWithSearchRequesBuilder(String paltformID, SearchRequestBuilder searchRequestBuilder,
                                                           byte[] photo, String searchType, String creatorOrName,
                                                           int start, int pageSize, boolean moHuSearch) {
        return dealWithSearchRequesBuilder(searchRequestBuilder,
                photo,
                searchType,
                creatorOrName,
                start,
                pageSize,
                moHuSearch);
    }

    private ObjectSearchResult dealWithSearchRequesBuilder(SearchRequestBuilder searchRequestBuilder,
                                                           byte[] photo,
                                                           String searchType,
                                                           String creatorOrName,
                                                           int start,
                                                           int pageSize,
                                                           boolean moHuSearch) {
        long start_time = System.currentTimeMillis();
        SearchResponse response = searchRequestBuilder.get();
        ObjectSearchResult searchResult = new ObjectSearchResult();
        List<Map<String, Object>> results = new ArrayList<>();
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        String searchId = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchId);
        if (photo == null) {
            searchResult.setPhotoId(null);
        } else {
            searchResult.setPhotoId(searchId);
        }
        searchResult.setSearchNums(hits.getTotalHits());
        if (searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                Map<String, Object> source = hit.getSource();
                // ES 的文档名，对应着HBase 的rowkey
                source.put(ObjectInfoTable.ROWKEY, hit.getId());
                results.add(source);
            }
        }
        searchResult.setSearchStatus(0);
        searchResult.setResults(results);
        // 处理精确查找下，IK 分词器返回多余信息的情况，
        // 比如只需要小王炸，但是返回了小王炸 和小王炸小以及小王炸大的情况
        LOG.info("dealWithSearchRequesBuilder, time: " + (System.currentTimeMillis() - start_time));
        return searchResult;
    }
}
