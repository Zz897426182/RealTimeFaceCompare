package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PSearchArgsModel;
import com.hzgc.dubbo.staticrepo.PersonObject;
import com.hzgc.dubbo.staticrepo.StaticSortParam;
import com.hzgc.jni.FaceFunction;
import org.apache.derby.vti.IFastPath;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseByOption {
    private static Logger LOG = Logger.getLogger(PSearchArgsModel.class);
    public static String getSqlFromPSearchArgsModel(Connection conn, PSearchArgsModel pSearchArgsModel) {
        StringBuffer sql = new StringBuffer("");

        Map<String, byte[]> photos = pSearchArgsModel.getImages();
        Map<String, FaceAttribute> faceAttributeMap = pSearchArgsModel.getFaceAttributeMap();


        sql.append("select ");
        if (photos != null && photos.size() != 0
                && faceAttributeMap != null && faceAttributeMap.size() != 0
                && faceAttributeMap.size() == photos.size()) {
            List<Object> setValues = new ArrayList<>();
            if (pSearchArgsModel.isTheSameMan()) {

            } else {
                // 最终需要返回的内容
                sql.append(sameFieldNeedReturn());
                sql.append(", type");
                sql.append(", sim");
                sql.append(" from (");

                // 拼装子sql
                List<StringBuffer> subSqls = new ArrayList<>();
                for (Map.Entry<String, FaceAttribute> entry : faceAttributeMap.entrySet()) {
                    String key = entry.getKey();
                    FaceAttribute faceAttribute = entry.getValue();
                    float[] feature = null;
                    if (faceAttribute != null && faceAttribute.getFeature() != null) {
                        feature = entry.getValue().getFeature();
                    }
                    StringBuffer subSql = new StringBuffer("");
                    sql.append("select ");
                    sql.append(sameFieldNeedReturn());
                    sql.append(", ? as type");
                    setValues.add(key);
                    sql.append("FACECOMP(");
                    sql.append(ObjectInfoTable.FEATURE);
                    sql.append(", ?");
                    try {
                        setValues.add(conn.createArrayOf("FLOAT", PersonObject.otherArrayToObject(feature)));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    sql.append(") as sim from ");
                    sql.append(ObjectInfoTable.TABLE_NAME);

                    float threthod = pSearchArgsModel.getThredshold();
                    sql.append(" where sim > ? ");
                    setValues.add(threthod);
                    subSqls.add(subSql);
                    subSql.append(sameWhereSql(pSearchArgsModel, setValues));
                }

                //完成子sql 的拼装
                sql.append(subSqls.get(0));
                for (int i = 1; i < subSqls.size(); i++) {
                    sql.append(" union all ");
                    sql.append(subSqls.get(i));
                }

                //排序sql 拼装
                List<StaticSortParam> params = pSearchArgsModel.getStaticSortParams();
                if (params != null && params.size() != 0) {
                    sql.append(" order by ");
                }


            }
        }




        // 拼装条件查询
        // 用来给后面的占位符进行赋值


        // 关于排序参数的sql 拼装
//
//        if (faceAttributeMap != null || faceAttributeMap.size() != 0 && theSameMan) {
//            if (params != null && params.size() != 0) {
//                sql.append(" order by ");
//            }
//            int i = 0;
//            int paramsMaxIndex = params.size() - 1;
//            for (StaticSortParam staticSortParam : params) {
//                if (StaticSortParam.THRESHOLDESC.equals(staticSortParam)) {
//
//                } else if (StaticSortParam.THRESHOLASC.equals(staticSortParam)) {
//
//                } else if(StaticSortParam.IMPORTANTASC.equals(staticSortParam)){
//                    sql.append(ObjectInfoTable.IMPORTANT);
//                    if (i == paramsMaxIndex){
//                        sql.append(" ASC");
//                    } else {
//                        sql.append(" ASC, ");
//                    }
//                } else if (StaticSortParam.IMPORTANTDESC.equals(staticSortParam)) {
//                    sql.append(ObjectInfoTable.IMPORTANT);
//                    if (i == paramsMaxIndex){
//                        sql.append("DESC");
//                    } else {
//                        sql.append("DESC, ");
//                    }
//
//                } else if (StaticSortParam.TIMEDESC.equals(staticSortParam)) {
//                    sql.append(ObjectInfoTable.CREATETIME);
//                    if (i == paramsMaxIndex){
//                        sql.append(" DESC");
//                    } else {
//                        sql.append(" DESC, ");
//                    }
//                } else if (StaticSortParam.TIMEASC.equals(staticSortParam)) {
//                    sql.append(ObjectInfoTable.CREATETIME);
//                    if (i == paramsMaxIndex) {
//                        sql.append(" ASC");
//                    }else {
//                        sql.append(" ASC, ");
//                    }
//                }
//                i++;
//            }
//        }

        // 进行分组
        LOG.info(sql);
        return new String(sql);
    }


    private static StringBuffer sameFieldNeedReturn() {
        StringBuffer sameFieldReturn = new StringBuffer("");
        sameFieldReturn.append(ObjectInfoTable.ROWKEY);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.PKEY);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.PLATFORMID);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.NAME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.SEX);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.IDCARD);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CREATOR);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CPHONE);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CPHONE);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CREATETIME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.UPDATETIME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.REASON);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.TAG);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.IMPORTANT);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.STATUS);
        return sameFieldReturn;
    }



    /**
     * 封装共同的子where 查询
     * @param pSearchArgsModel 传过来的搜索参数
     * @param setArgsList 需要对sql 设置的值
     * @return 子where查询
     */
    private static StringBuffer sameWhereSql(PSearchArgsModel pSearchArgsModel, List<Object> setArgsList) {
        StringBuffer whereQuery = new StringBuffer("");
        // 关于平台的搜索
        String platformId = pSearchArgsModel.getPaltaformId();
        if (platformId != null && !"".equals(platformId)) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.ROWKEY);
            whereQuery.append(" = ?");
            setArgsList.add(platformId);
        }
        // 关于姓名的搜索
        String name = pSearchArgsModel.getName();
        if (name != null && !"".equals(name) && pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.NAME);
            whereQuery.append(" like ?");
            setArgsList.add("%" + name + "%");
        } else if (name != null && !"".equals(name) && !pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.NAME);
            whereQuery.append(" = ?");
            setArgsList.add(name);
        }

        // 关于身份证号的查询
        String idCard = pSearchArgsModel.getIdCard();
        if (idCard != null && !"".equals(idCard) && pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.IDCARD);
            whereQuery.append(" like ?");
            setArgsList.add("%" + idCard + "%");
        } else if (idCard != null && !"".equals(idCard) && !pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.IDCARD);
            whereQuery.append(" = ?");
            setArgsList.add(idCard);
        }

        // 关于性别的查询
        int sex = pSearchArgsModel.getSex();
        if (sex != -1) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.SEX);
            whereQuery.append(" = ?");
            setArgsList.add(sex);
        }

        // 关于人员类型列表的查询
        List<String> pkeys = new ArrayList<>();
        for (int i = 0;i < pkeys.size(); i++) {
            if (i == pkeys.size() - 1) {
                whereQuery.append(" or ");
                whereQuery.append(ObjectInfoTable.PKEY);
                whereQuery.append(" = ?)");
                setArgsList.add(pkeys.get(i));
            } else if (i == 0){
                whereQuery.append(" and(");
                whereQuery.append(ObjectInfoTable.PKEY);
                whereQuery.append(" = ?");
                setArgsList.add(pkeys.get(i));
            } else {
                whereQuery.append(" or ");
                whereQuery.append(ObjectInfoTable.PKEY);
                whereQuery.append(" = ?");
                setArgsList.add(pkeys.get(i));
            }
        }

        // 关于创建人姓名的查询
        String creator = pSearchArgsModel.getCreator();
        if (creator != null && !"".equals(creator) && pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.CREATOR);
            whereQuery.append(" like ?");
            setArgsList.add("%" + creator + "%");
        } else if (creator != null && !"".equals(creator) && !pSearchArgsModel.isMoHuSearch()) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.CREATOR);
            whereQuery.append(" = ?");
            setArgsList.add(creator);
        }

        // 关于布控人手机号的查询
        String cPhone = pSearchArgsModel.getCphone();
        if (cPhone != null && !"".equals(cPhone)) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.CPHONE);
            whereQuery.append(" = ?");
            setArgsList.add(cPhone);
        }

        //
        // 关于是否是重点人员的查询
        int important = pSearchArgsModel.getImportant();
        if (important != -1) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.IMPORTANT);
            whereQuery.append(" = ?");
            setArgsList.add(important);
        }

        // 属于人员状态，建议迁入和常住人口的查询
        int status= pSearchArgsModel.getStatus();
        if (status != -1) {
            whereQuery.append(" and ");
            whereQuery.append(ObjectInfoTable.STATUS);
            whereQuery.append(" = ?");
            setArgsList.add(status);
        }
        return whereQuery;
    }

    /**
     * 根据传过来的person的封装的数据Map，进行生成一个sql,用来进行插入和更新
     * @param person 需要更新的数据
     * @return  拼装成的sql 以及需要设置的值
     */
    public static Map<String, List<Object>> getUpdateSqlFromPersonMap(Map<String, Object> person) {
        List<Object> setValues = new ArrayList<>();
        StringBuffer sql = new StringBuffer("");
        sql.append("upsert into ");
        sql.append(ObjectInfoTable.TABLE_NAME);
        sql.append("(");
        sql.append(ObjectInfoTable.ROWKEY);


        setValues.add(person.get(ObjectInfoTable.ROWKEY));
        String name = (String) person.get(ObjectInfoTable.NAME);
        if (name != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.NAME);
            setValues.add(name);
        }
        String platformid = (String) person.get(ObjectInfoTable.PLATFORMID);
        if (platformid != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.PLATFORMID);
            setValues.add(platformid);
        }
        String tag = (String) person.get(ObjectInfoTable.TAG);
        if (tag != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.TAG);
            setValues.add(tag);
        }
        String pkey = (String) person.get(ObjectInfoTable.PKEY);
        if (pkey != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.PKEY);
            setValues.add(pkey);
        }
        String idcard = (String) person.get(ObjectInfoTable.IDCARD);
        if (idcard != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.IDCARD);
            setValues.add(idcard);
        }
        if(person.get(ObjectInfoTable.SEX) != null) {
            int sex = (int) person.get(ObjectInfoTable.SEX);
            sql.append(", ");
            sql.append(ObjectInfoTable.SEX);
            setValues.add(sex);
        }

        String reason = (String) person.get(ObjectInfoTable.REASON);
        if (reason != null){
            sql.append(", ");
            sql.append(ObjectInfoTable.REASON);
            setValues.add(reason);
        }
        String creator = (String) person.get(ObjectInfoTable.CREATOR);
        if (creator != null){
            sql.append(", ");
            sql.append(ObjectInfoTable.CREATOR);
            setValues.add(creator);
        }
        String cphone = (String) person.get(ObjectInfoTable.CPHONE);
        if (cphone != null){
            sql.append(", ");
            sql.append(ObjectInfoTable.CPHONE);
            setValues.add(cphone);
        }

        if(person.get(ObjectInfoTable.IMPORTANT) != null) {
            int important = (int) person.get(ObjectInfoTable.IMPORTANT);
            sql.append(", ");
            sql.append(ObjectInfoTable.IMPORTANT);
            setValues.add(important);
        }

        if(person.get(ObjectInfoTable.STATUS) != null) {
            int status = (int) person.get(ObjectInfoTable.STATUS);
            sql.append(", ");
            sql.append(ObjectInfoTable.STATUS);
            setValues.add(status);
        }
        sql.append(") values(?");
        StringBuffer tmp = new StringBuffer();
        for (int i = 0;i <= setValues.size() - 2; i++) {
            tmp.append(", ?");
        }
        sql.append(tmp);
        sql.append(")");
        Map<String, List<Object>> sqlAndSetValues = new HashMap<>();
        sqlAndSetValues.put(new String(sql), setValues);
        return sqlAndSetValues;
    }


}
