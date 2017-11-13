package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.dynamicrepo.SearchOption;

import java.sql.Date;


class ParseByOption {

    /**
     * 获取拼接sql
     *
     * @param searchFeaStr 特征值
     * @param option       搜索条件
     * @return 返回拼接的sql
     */
    String getFinalSQLwithOption(String searchFeaStr, SearchOption option) {
        return getSQLbyOption(DynamicTable.PERSON_TABLE, searchFeaStr, option) +
                " union " +
                getSQLbyOption(DynamicTable.MID_TABLE, searchFeaStr, option) +
                " order by "
                + DynamicTable.SIMILARITY
                + " limit 1000";
    }

    private String getSQLbyOption(String tableName, String searchFeaStr, SearchOption option) {
        //无阈值不进行比对
        if (option.getThreshold() == 0.0) {
            return "";
        }
        StringBuilder finalSql = new StringBuilder();
        finalSql
                .append("select * from (select *, ")
                .append(DynamicTable.FUNCTION_NAME)
                .append("('")
                .append(searchFeaStr)
                .append("', ")
                .append(DynamicTable.FEATURE)
                .append(") as ")
                .append(DynamicTable.SIMILARITY)
                .append(" from ")
                .append(tableName)
                .append(") temp_table where ")
                .append(DynamicTable.SIMILARITY)
                .append(">=").append(option.getThreshold());

        //判断人脸对象属性
        if (option.getAttributes() != null && option.getAttributes().size() > 0) {
            for (Attribute attribute : option.getAttributes()) {
                if (attribute.getValues() != null) {
                    switch (attribute.getLogistic()) {
                        case AND:
                            finalSql.append(" and ");
                            break;
                        case OR:
                            finalSql.append(" or ");
                            break;
                    }
                    for (int i = 0; i < attribute.getValues().size(); i++) {
                        finalSql
                                .append(attribute.getIdentify().toLowerCase())
                                .append("=")
                                .append(attribute.getValues().get(i).getValue());
                        if (attribute.getValues().size() - 1 > i) {
                            finalSql.append(" or ");
                        }
                    }
                }
            }
        }
        //判断一个或多个时间区间 数据格式 小时+分钟 例如:1122
        if (option.getIntervals() != null && option.getIntervals().size() > 0) {
            for (int i = 0; option.getIntervals().size() > i; i++) {
                int start_sj = option.getIntervals().get(i).getStart();
                int start_st = (start_sj / 60) * 100 + start_sj % 60;
                int end_sj = option.getIntervals().get(i).getEnd();
                int end_st = (end_sj / 60) * 100 + end_sj % 60;
                if (option.getIntervals().size() - 1 > i) {
                    finalSql.append(" and ")
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
        if (option.getDeviceIds() != null) {
            finalSql.append(" and ");
            for (int i = 0; option.getDeviceIds().size() > i; i++) {
                String ipcid = option.getDeviceIds().get(i);
                if (option.getDeviceIds().size() - 1 > i) {
                    finalSql
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'")
                            .append(" or ");
                } else {
                    finalSql
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'");
                }
            }
        }
        return finalSql.toString();
    }
}
