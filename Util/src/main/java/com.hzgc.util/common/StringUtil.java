package com.hzgc.util.common;

import org.apache.log4j.Logger;

public class StringUtil {
    private static Logger LOG = Logger.getLogger(StringUtil.class);

    public static boolean strIsRight(String str) {
        return null != str && str.length() > 0;
    }
}
