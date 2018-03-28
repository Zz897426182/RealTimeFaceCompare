package com.hzgc.util.sort;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ListUtils {
    private static Logger LOG = Logger.getLogger(ListUtils.class);

    /**
     * 根据排序string来生成sql排序，多个排序字段用逗号隔开。'+'表示升序，'-'表示降序，如+id,-name
     *
     * @param sortParams 排序字符串
     * @return SortParam 排序入参对象
     */
    public static SortParam getOrderStringBySort(String sortParams) {
        SortParam sortParam = new SortParam();
        if (null != sortParams && sortParams.length() > 0) {
            StringBuilder orderString = new StringBuilder();
            String[] orderStringList = sortParams.split(",");
            for (String s : orderStringList) {
                char orderTypeChar = s.charAt(0);
                // 降序
                if ("-".toCharArray()[0] == orderTypeChar) {
                    orderString.append(s.substring(1));
                    orderString.append(" ");
                    orderString.append("false");
                }
                // 升序
                else {
                    orderString.append(s.substring(1));
                    orderString.append(" ");
                    orderString.append("true");
                }
                if (!s.equals(orderStringList[orderStringList.length - 1])) {
                    orderString.append(",");
                }
            }

            String[] splitStr = orderString.toString().split(",");
            String[] sortNameArr = new String[splitStr.length];
            boolean[] isAscArr = new boolean[splitStr.length];
            for (int i = 0; i < splitStr.length; i++) {
                String[] oneParam = splitStr[i].split(" ");
                sortNameArr[i] = oneParam[0];
                isAscArr[i] = oneParam[1].equals("true");
            }
            sortParam.setSortNameArr(sortNameArr);
            sortParam.setIsAscArr(isAscArr);
        } else {
            LOG.info("sortParam is null");
            sortParam = null;
        }
        return sortParam;
    }

    /*
     * 对list的元素按照多个属性名称排序,
     * list元素的属性可以是数字（byte、short、int、long、float、double等，支持正数、负数、0）、char、String、java.util.Date
     *
     * @param list        排序对象
     * @param sortNameArr list元素的属性名称
     * @param isAsc       true升序，false降序
     */
    public static <CapturedPicture> void sort(List<CapturedPicture> list, final boolean isAsc, final String... sortNameArr) {
        Collections.sort(list, new Comparator<CapturedPicture>() {
            public int compare(CapturedPicture a, CapturedPicture b) {
                int ret = 0;
                if (a == null && b == null) {
                    return 0;
                }
                if (a == null) {
                    return -1;
                }
                if (b == null) {
                    return 1;
                }
                try {
                    for (String sortName : sortNameArr) {
                        ret = ListUtils.compareObject(sortName, isAsc, a, b);
                        if (0 != ret) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ret;
            }
        });
    }

    /**
     * 给list的每个属性都指定是升序还是降序
     *
     * @param list        排序对象
     * @param sortNameArr 参数数组
     * @param typeArr     每个属性对应的升降序数组， true升序，false降序
     */

    public static <CapturedPicture> void sort(List<CapturedPicture> list, final String[] sortNameArr, final boolean[] typeArr) {
        if (sortNameArr.length != typeArr.length) {
            throw new RuntimeException("属性数组元素个数和升降序数组元素个数不相等");
        }
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<CapturedPicture>() {
                public int compare(CapturedPicture a, CapturedPicture b) {
                    int ret = 0;
                    if (a == null && b == null) {
                        return 0;
                    }
                    if (a == null) {
                        return -1;
                    }
                    if (b == null) {
                        return 1;
                    }
                    try {
                        for (int i = 0; i < sortNameArr.length; i++) {
                            ret = ListUtils.compareObject(sortNameArr[i], typeArr[i], a, b);
                            if (0 != ret) {
                                break;//如果两个数据根据某个字段相等，则根据下一个字段进行比较
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ret;
                }
            });
        }
    }

    /**
     * 对2个对象按照指定属性名称进行排序
     *
     * @param sortName 属性名称
     * @param isAsc    true升序，false降序
     * @param a        对象a
     * @param b        对象b
     * @return 比对结果-1、0、1，分别代表小于、等于、大于
     */
    private static <E> int compareObject(final String sortName, final boolean isAsc, E a, E b) {
        int ret = 0;
        if (null != a && null != b) {
            Object value1 = null;
            Object value2 = null;
            try {
                value1 = ListUtils.forceGetFieldValue(a, sortName);
                value2 = ListUtils.forceGetFieldValue(b, sortName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String str1 = null;
            if (value1 != null) {
                str1 = value1.toString();
            }
            String str2 = null;
            if (value2 != null) {
                str2 = value2.toString();
            }
            if (value1 instanceof Number && value2 instanceof Number) {
                int maxlen = Math.max(str1.length(), str2.length());
                str1 = ListUtils.addZero2Str((Number) value1, maxlen);
                str2 = ListUtils.addZero2Str((Number) value2, maxlen);
            } else if (value1 instanceof Date && value2 instanceof Date) {
                long time1 = ((Date) value1).getTime();
                long time2 = ((Date) value2).getTime();
                int maxlen = Long.toString(Math.max(time1, time2)).length();
                str1 = ListUtils.addZero2Str(time1, maxlen);
                str2 = ListUtils.addZero2Str(time2, maxlen);
            }
            if (isAsc) {
                if (str2 != null && str1 != null) {
                    ret = str1.compareTo(str2);
                }
            } else {
                if (str2 != null && str1 != null) {
                    ret = str2.compareTo(str1);
                }
            }

        }
        return ret;
    }

    /**
     * 给数字对象按照指定长度在左侧补0.
     * <p>
     * 使用案例: addZero2Str(11,4) 返回 "0011", addZero2Str(-18,6)返回 "-000018"
     *
     * @param numObj 数字对象
     * @param length 指定的长度
     * @return 格式化后的数字
     */
    private static String addZero2Str(Number numObj, int length) {
        NumberFormat nf = NumberFormat.getInstance();
        // 设置是否使用分组
        nf.setGroupingUsed(false);
        // 设置最大整数位数
        nf.setMaximumIntegerDigits(length);
        // 设置最小整数位数
        nf.setMinimumIntegerDigits(length);
        return nf.format(numObj);
    }

    /**
     * 获取指定对象的指定属性值（去除private,protected的限制）
     *
     * @param obj       属性名称所在的对象
     * @param fieldName 属性名称
     * @return Object
     */
    private static Object forceGetFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        Object object;
        boolean accessible = field.isAccessible();
        if (!accessible) {
            // 如果是private,protected修饰的属性，需要修改为可以访问的
            field.setAccessible(true);
            object = field.get(obj);
            // 还原private,protected属性的访问性质
            field.setAccessible(false);
            return object;
        }
        object = field.get(obj);
        return object;
    }
}
