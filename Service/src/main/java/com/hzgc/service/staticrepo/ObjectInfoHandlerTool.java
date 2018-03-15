package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PersonObject;

import java.sql.*;

public class ObjectInfoHandlerTool {
    public static PersonObject getPersonObjectFromResultSet(ResultSet resultSet) {
        PersonObject personObject = new PersonObject();
        try {
            while (resultSet.next()) {
                personObject.setId(resultSet.getString(ObjectInfoTable.ROWKEY));
                personObject.setPkey(resultSet.getString(ObjectInfoTable.PKEY));
                personObject.setPlatformid(resultSet.getString(ObjectInfoTable.PLATFORMID));
                personObject.setName(resultSet.getString(ObjectInfoTable.NAME));
                personObject.setSex(resultSet.getInt(ObjectInfoTable.SEX));
                personObject.setIdcard(resultSet.getString(ObjectInfoTable.IDCARD));
                personObject.setPhoto(resultSet.getBytes(ObjectInfoTable.PHOTO));
                Array array = resultSet.getArray(ObjectInfoTable.FEATURE);
                if (array != null) {
                    personObject.setFeature((float[]) array.getArray());
                }
                personObject.setCreator(resultSet.getString(ObjectInfoTable.CREATOR));
                personObject.setCphone(resultSet.getString(ObjectInfoTable.CPHONE));
                personObject.setUpdatetime(resultSet.getDate(ObjectInfoTable.UPDATETIME));
                personObject.setCreatetime(resultSet.getDate(ObjectInfoTable.CREATETIME));
                personObject.setReason(resultSet.getString(ObjectInfoTable.REASON));
                personObject.setTag(resultSet.getString(ObjectInfoTable.TAG));
                personObject.setImportant(resultSet.getInt(ObjectInfoTable.IMPORTANT));
                personObject.setStatus(resultSet.getInt(ObjectInfoTable.STATUS));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return personObject;
    }

    public static PreparedStatement getStaticPrepareStatementV1(Connection conn, PersonObject person, String sql) {
        PreparedStatement pstm;
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, person.getId());
            pstm.setString(2, person.getName());
            pstm.setString(3, person.getPlatformid());
            pstm.setString(4, person.getTag());
            pstm.setString(5, person.getPkey());
            pstm.setString(6, person.getIdcard());
            pstm.setInt(7, person.getSex());
            pstm.setBytes(8, person.getPhoto());
            if (person.getFeature() != null && person.getFeature().length == 512) {
                pstm.setArray(9,
                        conn.createArrayOf("FLOAT", PersonObject.otherArrayToObject(person.getFeature())));
            } else {
                pstm.setArray(9, null);
            }
            pstm.setString(10, person.getReason());
            pstm.setString(11, person.getCreator());
            pstm.setString(12, person.getCphone());
            long dateNow = System.currentTimeMillis();
            pstm.setDate(13, new Date(dateNow));
            pstm.setDate(14, person.getUpdatetime());
            pstm.setInt(15, person.getImportant());
            pstm.setInt(16, person.getStatus());
            return pstm;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PreparedStatement getStaticPrepareStatementV2(Connection conn, PersonObject person, String sql) {
        PreparedStatement pstm;
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, person.getId());
            pstm.setString(2, person.getName());
            pstm.setString(3, person.getPlatformid());
            pstm.setString(4, person.getTag());
            pstm.setString(5, person.getPkey());
            pstm.setString(6, person.getIdcard());
            pstm.setInt(7, person.getSex());
            pstm.setBytes(8, person.getPhoto());
            if (person.getFeature() != null && person.getFeature().length == 512) {
                pstm.setArray(9,
                        conn.createArrayOf("FLOAT", PersonObject.otherArrayToObject(person.getFeature())));
            } else {
                pstm.setArray(9, null);
            }
            pstm.setString(10, person.getReason());
            pstm.setString(11, person.getCreator());
            pstm.setString(12, person.getCphone());
            pstm.setDate(13, person.getUpdatetime());
            pstm.setInt(14, person.getImportant());
            pstm.setInt(15, person.getStatus());
            return pstm;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
