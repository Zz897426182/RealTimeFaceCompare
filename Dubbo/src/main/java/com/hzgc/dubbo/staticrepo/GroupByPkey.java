package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.List;

/**
 * 按照对象类型分类，排序返回
 */
public class GroupByPkey implements Serializable{
    private String pkey;    //对象类型Key
    private List<PersonObject> persons;  // 底库信心
    private int total;  // 当前pkey 下的人的总数

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public List<PersonObject> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonObject> persons) {
        this.persons = persons;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "GroupByPkey{" +
                "pkey='" + pkey + '\'' +
                ", persons=" + persons +
                ", total=" + total +
                '}';
    }
}
