package com.hzgc.util;

public class StringUtil {
    public static boolean strIsRight(String str) {
        if (null != str && str.length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 根据排序string来生成sql排序
     *
     * @param sort 排序字符串，多个排序字段用逗号隔开。默认升序，'-'表示降序，如id,-name
     * @return sql排序语句
     */

    public static String getOrderStringBySort(String sort) {
        if (null == sort || sort.trim().equals("")) {
            return "";
        }

        StringBuffer orderString = new StringBuffer();
        String[] orderStringList = sort.split(",");
        for (String s : orderStringList) {
            char orderTypeChar = s.charAt(0);
            // 降序
            if ("-".toCharArray()[0] == orderTypeChar) {
                orderString.append(s.substring(1));
                orderString.append(" ");
                orderString.append("desc");
            }
            // 升序
            else {
                orderString.append(s);
                orderString.append(" ");
                orderString.append("asc");
            }
            if (!s.equals(orderStringList[orderStringList.length - 1])) {
                orderString.append(",");
            }
        }
        return orderString.toString();
    }

}
