package com.hzgc.util.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     * 将String类型时间转换为long类型时间
     *
     * @param timeStamp String类型时间
     * @return long类型时间
     */
    public static long dateToTimeStamp(String timeStamp) {
        if (timeStamp != null && timeStamp.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = dateFormat.parse(timeStamp);
                return date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
