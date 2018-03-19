package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.*;


public class ObjectInfoHandlerImpl implements ObjectInfoHandler {

    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

    public ObjectInfoHandlerImpl() {
        NativeFunction.init();
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> personObject) {
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        long start = System.currentTimeMillis();
        PersonObject person = PersonObject.mapToPersonObject(personObject);
        LOG.info("the rowkey off this add person is: " + person.getId());

        String sql = "upsert into objectinfo(" + ObjectInfoTable.ID+ ", " + ObjectInfoTable.NAME  + ", "
                + ObjectInfoTable.PLATFORMID + ", " + ObjectInfoTable.TAG + ", " + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", " + ObjectInfoTable.SEX + ", " + ObjectInfoTable.PHOTO + ", "
                + ObjectInfoTable.FEATURE + ", " + ObjectInfoTable.REASON + ", " + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", " + ObjectInfoTable.CREATETIME + ", " + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", "  + ObjectInfoTable.STATUS
                + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = null;
        try {
            pstm = new ObjectInfoHandlerTool().getStaticPrepareStatementV1(conn, person, sql);
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(conn, pstm);
        }
        LOG.info("添加一条数据到静态库花费时间： " + (System.currentTimeMillis() - start));
        return 0;
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        // 获取table 对象，通过封装HBaseHelper 来获取
        long start = System.currentTimeMillis();
        String sql = "delete from objectinfo where " + ObjectInfoTable.ID  +" = ?";
        PreparedStatement pstm = null;
        try {
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i< rowkeys.size(); i++) {
                pstm.setString(1, rowkeys.get(i));
                pstm.executeUpdate();
                if (i % 10 == 0) {
                    conn.commit();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(conn, pstm);
        }
        LOG.info("删除静态信息库的" + rowkeys.size() + "条数据花费时间： " + (System.currentTimeMillis() - start));
        return 0;
    }

    /**
     * @param personObject K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 更新成功与否的标志，0成功，1失败
     */
    @Override
    public int updateObjectInfo(Map<String, Object> personObject) {
        long start = System.currentTimeMillis();
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        String thePassId = (String) personObject.get(ObjectInfoTable.ID);
        if (thePassId == null) {
            LOG.info("the pass Id can not be null....");
            return 1;
        }
        PreparedStatement pstm = null;
        try {
            Map<String, List<Object>> sqlAndSetValues = ParseByOption.getUpdateSqlFromPersonMap(personObject);
            String sql = null;
            List<Object> setValues = new ArrayList<>();
            for (Map.Entry<String, List<Object>> entry : sqlAndSetValues.entrySet()) {
                sql = entry.getKey();
                setValues = entry.getValue();
            }
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < setValues.size(); i++) {
                pstm.setObject(i+1, setValues.get(i));
            }
            pstm.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm);
        }
        LOG.info("更新rowkey为: " + thePassId +  "数据花费的时间是: " + (System.currentTimeMillis() - start));
        return 0;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String id) {
        long start = System.currentTimeMillis();
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        PreparedStatement pstm = null;
        ObjectSearchResult result = new ObjectSearchResult();
        PersonObject person;
        ResultSet resultSet = null;
        try {
            String sql = "select * from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, id);
            resultSet = pstm.executeQuery();
            person = new ObjectInfoHandlerTool().getPersonObjectFromResultSet(resultSet);
            result.setSearchStatus(0);
            List<PersonSingleResult> personSingleResults = new ArrayList<>();
            PersonSingleResult personSingleResult = new PersonSingleResult();
            personSingleResult.setSearchNums(1);
            List<PersonObject> persons = new ArrayList<>();
            persons.add(person);
            personSingleResult.setPersons(persons);
            personSingleResults.add(personSingleResult);
            result.setFinalResults(personSingleResults);
        } catch (SQLException e) {
            result.setSearchStatus(1);
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(conn, pstm, resultSet);
        }
        LOG.info("获取一条数据的时间是：" + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        long start = System.currentTimeMillis();
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        // 总的结果
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        String searchTotalId = UUID.randomUUID().toString().replace("-", "");
        objectSearchResult.setSearchTotalId(searchTotalId);
        List<PersonSingleResult> finalResults = new ArrayList<>();


        //封装的sql 以及需要设置的值
        Map<String, List<Object>> finalSqlAndValues = ParseByOption.getSqlFromPSearchArgsModel(conn, pSearchArgsModel);

        PreparedStatement pstm = null;
        ResultSet resultSet = null;

        // 取出封装的sql 以及需要设置的值，进行sql 查询
        if (finalSqlAndValues == null) {
            objectSearchResult.setSearchStatus(1);
            LOG.info("创建sql 失败，请检查代码");
            return objectSearchResult;
        }
        for (Map.Entry<String, List<Object>> entry : finalSqlAndValues.entrySet()) {
            String sql = entry.getKey();
            List<Object> setValues = entry.getValue();
            try {
                // 实例化pstm 对象，并且设置值，
                pstm = conn.prepareStatement(sql);
                for (int i = 0; i< setValues.size(); i++) {
                    pstm.setObject(i + 1, setValues.get(i));
                }
                resultSet = pstm.executeQuery();
                Map<String, byte[]> photos = pSearchArgsModel.getImages();
                Map<String, FaceAttribute> faceAttributeMap = pSearchArgsModel.getFaceAttributeMap();
                // 有图片的情况下
                if (photos != null && photos.size() != 0
                        && faceAttributeMap != null && faceAttributeMap.size() != 0
                        && faceAttributeMap.size() == photos.size()) {
                    if (pSearchArgsModel.isTheSameMan()) {  // 不是同一个人
                        // 分类的人
                        Map<String, List<PersonObject>> personObjectsMap = new HashMap<>();

                        List<PersonObject> personObjects = new ArrayList<>();
                        PersonObject personObject;
                        Set<String> types = new HashSet<>();
                        while (resultSet.next()) {
                            String type = resultSet.getString("type");
                            if (!types.contains(type)){
                                types.add(type);
                                personObjectsMap.put(type, personObjects);
                                personObjects.clear();
                            }
                            personObject = new PersonObject();
                            personObject.setId(resultSet.getString(ObjectInfoTable.ID));
                            personObject.setPkey(resultSet.getString(ObjectInfoTable.PKEY));
                            personObject.setPlatformid(resultSet.getString(ObjectInfoTable.PLATFORMID));
                            personObject.setName(resultSet.getString(ObjectInfoTable.NAME));
                            personObject.setSex(resultSet.getInt(ObjectInfoTable.SEX));
                            personObject.setIdcard(resultSet.getString(ObjectInfoTable.IDCARD));
                            personObject.setCreator(resultSet.getString(ObjectInfoTable.CREATOR));
                            personObject.setCphone(resultSet.getString(ObjectInfoTable.CPHONE));
                            personObject.setUpdatetime(resultSet.getTimestamp(ObjectInfoTable.UPDATETIME));
                            personObject.setCreatetime(resultSet.getTimestamp(ObjectInfoTable.CREATETIME));
                            personObject.setReason(resultSet.getString(ObjectInfoTable.REASON));
                            personObject.setTag(resultSet.getString(ObjectInfoTable.TAG));
                            personObject.setImportant(resultSet.getInt(ObjectInfoTable.IMPORTANT));
                            personObject.setStatus(resultSet.getInt(ObjectInfoTable.STATUS));
                            personObject.setSim(resultSet.getFloat(ObjectInfoTable.RELATED));
                            personObjects.add(personObject);
                        }
                        for (Map.Entry<String, List<PersonObject>> entryVV : personObjectsMap.entrySet()) {
                            String key = entryVV.getKey();
                            List<PersonObject> persons = entryVV.getValue();
                            PersonSingleResult personSingleResult = new PersonSingleResult();
                            personSingleResult.setSearchRowkey(searchTotalId + key);
                            personSingleResult.setSearchNums(persons.size());
                            personSingleResult.setPersons(persons);
                            List<byte[]> photosTmp = new ArrayList<>();
                            photosTmp.add(photos.get(key));
                            personSingleResult.setSearchPhotos(photosTmp);
                            finalResults.add(personSingleResult);
                        }
                    } else {  // 是同一个人
                        PersonSingleResult personSingleResult = new PersonSingleResult();
                        personSingleResult.setSearchRowkey(searchTotalId);

                        List<byte[]> searchPhotos = new ArrayList<>();
                        for (Map.Entry<String, byte[]> entryV1 : photos.entrySet()) {
                            searchPhotos.add(entryV1.getValue());
                        }
                        personSingleResult.setSearchPhotos(searchPhotos);
                        // 封装personSingleResult
                        new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, true);
                        finalResults.add(personSingleResult);
                    }
                } else { // 没有图片的情况下
                   PersonSingleResult personSingleResult = new PersonSingleResult();   // 需要进行修改
                   personSingleResult.setSearchRowkey(searchTotalId);
                    //封装personSingleResult
                   new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, false);
                   finalResults.add(personSingleResult);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
            }
        }

        objectSearchResult.setFinalResults(finalResults);
        objectSearchResult.setSearchStatus(0);

        LOG.info("总的搜索时间是: " + (System.currentTimeMillis() - start));
        new ObjectInfoHandlerTool().saveSearchRecord(conn, objectSearchResult);
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return objectSearchResult;
    }

    @Override
    public String getFeature(String tag, byte[] photo) {
        long start = System.currentTimeMillis();
        float[] floatFeature = FaceFunction.featureExtract(photo).getFeature();
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
}
