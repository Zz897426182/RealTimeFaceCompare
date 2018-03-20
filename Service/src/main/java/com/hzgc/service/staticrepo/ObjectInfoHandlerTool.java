package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.util.common.ObjectUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class ObjectInfoHandlerTool {
    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ObjectInfoHandlerTool.class);
    public void saveSearchRecord(Connection conn, ObjectSearchResult objectSearchResult) {
        if (objectSearchResult == null || objectSearchResult.getSearchStatus() == 1
                || objectSearchResult.getFinalResults() == null || objectSearchResult.getFinalResults().size() == 0) {
            LOG.info("获取的结果为空");
            return;
        }
        List<PersonSingleResult> personSingleResults = objectSearchResult.getFinalResults();

        PreparedStatement pstm;
        String sql = "upsert into " + SearchRecordTable.TABLE_NAME + "(" + SearchRecordTable.ID
                + ", " +SearchRecordTable.RESULT + ", " + SearchRecordTable.RECORDDATE + ") values(?,?,?)";
        if (conn != null) {
            try{
                for (PersonSingleResult personSingleResult : personSingleResults) {
                    pstm = conn.prepareStatement(sql);
                    String id;
                    if (personSingleResults.size() == 1) {
                        id = objectSearchResult.getSearchTotalId();
                    } else {
                        id = personSingleResult.getSearchRowkey();
                    }
                    pstm.setString(1, id);
                    pstm.setBytes(2, ObjectUtil.objectToByte(personSingleResult));
                    pstm.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    pstm.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public  PersonObject getPersonObjectFromResultSet(ResultSet resultSet) {
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
                personObject.setUpdatetime(resultSet.getTimestamp(ObjectInfoTable.UPDATETIME));
                personObject.setCreatetime(resultSet.getTimestamp(ObjectInfoTable.UPDATETIME));
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

    public  PreparedStatement getStaticPrepareStatementV1(Connection conn, PersonObject person, String sql) {
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
            pstm.setTimestamp(13, new Timestamp(dateNow));
            pstm.setTimestamp(14, person.getUpdatetime());
            pstm.setInt(15, person.getImportant());
            pstm.setInt(16, person.getStatus());
            return pstm;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param personSingleResult
     * @param resultSet
     * @param searchByPics
     * @return personSingelResult
     */
     PersonSingleResult getPersonSingleResult(PersonSingleResult personSingleResult, ResultSet resultSet, boolean searchByPics) {
        List<PersonObject> personObjects = new ArrayList<>();
        try {
            while (resultSet.next()) {
                PersonObject personObject = new PersonObject();
                personObject.setId(resultSet.getString(ObjectInfoTable.ROWKEY));
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
                if (searchByPics) {
                    personObject.setSim(resultSet.getFloat(ObjectInfoTable.RELATED));
                }
                personObjects.add(personObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        personSingleResult.setPersons(personObjects);
        personSingleResult.setSearchNums(personObjects.size());
        return personSingleResult;
    }

    /**
     * 根据请求参数，进行分页处理
     */
    void formatTheObjectSearchResult(ObjectSearchResult objectSearchResult, int start, int size) {
        if (objectSearchResult == null) {
            return;
        }
        List<PersonSingleResult> personSingleResults = objectSearchResult.getFinalResults();

        List<PersonSingleResult> finalPersonSingleResults = new ArrayList<>();
        if (personSingleResults != null) {
            for (PersonSingleResult personSingleResult : personSingleResults) {
                List<PersonObject> personObjects = personSingleResult.getPersons();
                if(personObjects != null) {
                    if ((start + size) > personObjects.size()) {
                        personSingleResult.setPersons(personObjects.subList(start, personObjects.size()));
                    } else {
                        personSingleResult.setPersons(personObjects.subList(start, start + size));
                    }
                }

                List<GroupByPkey> groupByPkeys = personSingleResult.getGroupByPkeys();
                List<GroupByPkey> finaLGroupByPkeys = new ArrayList<>();

                if (groupByPkeys != null) {
                    for (GroupByPkey groupByPkey : groupByPkeys) {
                        List<PersonObject> personObjectsV1 = groupByPkey.getPersons();
                        if (personObjectsV1 != null) {
                            if ((start + size) > personObjectsV1.size()) {
                                groupByPkey.setPersons(personObjectsV1.subList(start, personObjectsV1.size()));
                            } else {
                                groupByPkey.setPersons(personObjectsV1.subList(start, start + size));
                            }
                            finaLGroupByPkeys.add(groupByPkey);
                        }
                    }
                    personSingleResult.setGroupByPkeys(finaLGroupByPkeys);
                }
                finalPersonSingleResults.add(personSingleResult);
            }
        }
        objectSearchResult.setFinalResults(finalPersonSingleResults);

    }

}
