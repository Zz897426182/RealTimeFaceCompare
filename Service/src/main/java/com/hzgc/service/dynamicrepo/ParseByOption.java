package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.dynamicrepo.SearchOption;

import java.sql.Date;

class ParseByOption {
    private static String MID_FIELD = null;
    private static String FINAL_FTIEL = null;

    static {
        StringBuilder field = new StringBuilder();
        field.append(DynamicTable.FTPURL)
                .append(",")
                .append(DynamicTable.IPCID)
                .append(",")
                .append(DynamicTable.TIMESLOT)
                .append(",")
                .append(DynamicTable.TIMESTAMP)
                .append(",")
                .append(DynamicTable.DATE);
        MID_FIELD = field.toString();

        FINAL_FTIEL = field.append(",")
                .append(DynamicTable.SIMILARITY).toString();
    }

    /**
     * 获取拼接sql
     *
     * @param searchFeaStr 特征值
     * @param option       搜索条件
     * @return 返回拼接的sql
     */
    static String getFinalSQLwithOption(String searchFeaStr, SearchOption option) {
        //无阈值不进行比对
        if (option.getThreshold() == 0.0) {
            return "";
        }
        StringBuilder finalSql = new StringBuilder();
        finalSql.append("select ")
                .append(FINAL_FTIEL)
                .append(" from (")
                .append(getSQLbyOption(DynamicTable.PERSON_TABLE, searchFeaStr, option))
                .append(" union all ")
                .append(getSQLbyOption(DynamicTable.MID_TABLE, searchFeaStr, option))
                .append(") temp_table where ")
                .append(DynamicTable.SIMILARITY)
                .append(">=")
                .append(option.getThreshold());
        //判断人脸对象属性
        if (option.getAttributes() != null && option.getAttributes().size() > 0) {
            finalSql.append(" and (");
            Attribute attribute = new Attribute();
            for (int i = 0; i < option.getAttributes().size(); i++) {
                if (attribute.getValues() != null && attribute.getValues().size() > 0) {
                    finalSql
                            .append(attribute.getIdentify().toLowerCase())
                            .append(" in ")
                            .append("(");
                    for (int j = 0; j < attribute.getValues().size(); j++) {
                        if (attribute.getValues().size() - 1 > j) {
                            finalSql
                                    .append(attribute.getValues().get(j).getValue())
                                    .append(",");
                        } else {
                            finalSql
                                    .append(attribute.getValues().get(j).getValue())
                                    .append(")");
                        }
                    }
                }
                if (option.getAttributes().size() - 1 > i) {
                    switch (attribute.getLogistic()) {
                        case AND:
                            finalSql.append(" and ");
                            break;
                        case OR:
                            finalSql.append(" or ");
                            break;
                    }
                }
            }
        }
        //判断一个或多个时间区间 数据格式 小时+分钟 例如:1122
        if (option.getIntervals() != null && option.getIntervals().size() > 0) {
            finalSql.append(") and (");
            for (int i = 0; option.getIntervals().size() > i; i++) {
                int start_sj = option.getIntervals().get(i).getStart();
                int start_st = (start_sj / 60) * 100 + start_sj % 60;
                int end_sj = option.getIntervals().get(i).getEnd();
                int end_st = (end_sj / 60) * 100 + end_sj % 60;
                if (option.getIntervals().size() - 1 > i) {
                    finalSql
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append(start_st)
                            .append(" and ")
                            .append(end_st)
                            .append(" or ");
                } else {
                    finalSql
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append(start_st)
                            .append(" and ")
                            .append(end_st);
                }
            }
            finalSql.append(")");
        }
        if (option.getStartDate() != null && option.getEndDate() != null) {
            //判断开始时间和结束时间 数据格式 年-月-日 时:分:秒
            finalSql
                    .append(" and ")
                    .append(DynamicTable.TIMESTAMP)
                    .append(">=")
                    .append("'")
                    .append(option.getStartDate())
                    .append("'")
                    .append(" and ")
                    .append(DynamicTable.TIMESTAMP)
                    .append("<=")
                    .append("'")
                    .append(option.getEndDate())
                    .append("'");
            //判断日期分区 数据格式 年-月-日
            finalSql
                    .append(" and ")
                    .append(DynamicTable.DATE)
                    .append(" between ")
                    .append("'")
                    .append(Date.valueOf(option.getStartDate().split(" ")[0]))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(Date.valueOf(option.getEndDate().split(" ")[0]))
                    .append("'");
        }
        //判断一个或多个设备id
        /*if (option.getDeviceIds() != null) {
            finalSql.append(" and ")
            .append(DynamicTable.IPCID)
            .append(" in ")
            .append("(");
            for (int i = 0; option.getDeviceIds().size() > i; i++) {
                String ipcid = option.getDeviceIds().get(i);
                if (option.getDeviceIds().size() - 1 > i) {
                    finalSql.append("'")
                            .append(ipcid)
                            .append("'")
                            .append(",");
                } else {
                    finalSql.append("'")
                            .append(ipcid)
                            .append("'")
                            .append(")");
                }
            }
        }*/
        if (option.getSortParams() != null && option.getSortParams().length() > 0) {
            finalSql.append(" order by ");
            String[] splitStr = option.getSortParams().split(",");
            for (int i = 0; i < splitStr.length; i++) {
                if (splitStr[i].startsWith("+")) {
                    finalSql.append(splitStr[i].substring(1));
                    if (splitStr.length - 1 > i) {
                        finalSql.append(",");
                    }
                } else if (splitStr[i].startsWith("-")) {
                    finalSql.append(splitStr[i].substring(1))
                            .append(" desc");
                    if (splitStr.length - 1 > i) {
                        finalSql.append(",");
                    }
                }
            }
        }
        finalSql.append(" limit 1000");
        return finalSql.toString();
    }

    private static String getSQLbyOption(String tableName, String searchFeaStr, SearchOption option) {
        //date分区字段
        return "select " +
                MID_FIELD +
                "," +
                DynamicTable.FUNCTION_NAME +
                "('" +
                searchFeaStr +
                "', " +
                DynamicTable.FEATURE +
                ") as " +
                DynamicTable.SIMILARITY +
                " from " +
                tableName;
    }
}
