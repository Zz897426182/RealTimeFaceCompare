package com.hzgc.service.staticrepo;

import java.util.List;

public interface ObjectInfoInnerHandler {
    /**
     * 根据人员类型keys 进行查询，返回rowkeys 和features 和pkeys，
     * 返回rowkeys 和特征值列表 （内-----To刘善斌） （陈珂）
     * @param pkeys 人员类型keys 列表
     * @return  rowkey 和 pkey 以及feature 的String 拼装List
     */
    public List<String[]> searchByPkeys(List<String> pkeys);
}
