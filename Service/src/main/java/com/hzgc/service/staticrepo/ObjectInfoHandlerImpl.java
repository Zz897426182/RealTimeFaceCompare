package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.*;


public class ObjectInfoHandlerImpl implements ObjectInfoHandler {

    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);
    private static java.sql.Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();

    public ObjectInfoHandlerImpl() {
        NativeFunction.init();
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> personObject) {
        long start = System.currentTimeMillis();
        PersonObject person = PersonObject.mapToPersonObject(personObject);

        String sql = "upsert into objectinfo(id, name, platformid, tag, pkey, idcard, sex, photo, " +
                "feature, reason, creator, cphone, createtime, updatetime, important, status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = null;
        try {
            pstm = ObjectInfoHandlerTool.getStaticPrepareStatementV1(conn, person, sql);
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.info("添加一条数据到静态库花费时间： " + (System.currentTimeMillis() - start));
        return 0;
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        long start = System.currentTimeMillis();
        String sql = "delete from objectinfo where id = ?";
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
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
        String thePassId = (String) personObject.get(ObjectInfoTable.ROWKEY);
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
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.info("更新rowkey为: " + thePassId +  "数据花费的时间是: " + (System.currentTimeMillis() - start));
        return 0;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String id) {
        PreparedStatement pstm = null;
        ObjectSearchResult result = new ObjectSearchResult();
        PersonObject person = null;
        try {
            String sql = "select * from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, id);
            ResultSet resultSet = pstm.executeQuery();
            person = ObjectInfoHandlerTool.getPersonObjectFromResultSet(resultSet);
        } catch (SQLException e) {
            result.setSearchStatus(1);
            e.printStackTrace();
        }
        result.setSearchStatus(0);
        List<PersonSingleResult> personSingleResults = new ArrayList<>();
        PersonSingleResult personSingleResult = new PersonSingleResult();
        personSingleResult.setSearchNums(1);
        List<PersonObject> persons = new ArrayList<>();
        persons.add(person);
        personSingleResult.setPersons(persons);
        personSingleResults.add(personSingleResult);
        result.setFinalResults(personSingleResults);
//        result.setSearchTotalId(UUID.randomUUID().toString().replace("-", ""));
        return result;
    }

    @Override
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        long start = System.currentTimeMillis();
        ObjectSearchResult objectSearchResult;

        return null;
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
