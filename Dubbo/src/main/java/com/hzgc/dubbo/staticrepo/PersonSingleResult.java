package com.hzgc.dubbo.staticrepo;

import java.util.List;

/**
 * 多个人的的情况下，每个图片对应的
 */
public class PersonSingleResult {
    private String searchRowkey;        // 子搜索Id
    private int searchNums;             // 搜索的总数
    private List<byte[]> searchPhotos;      // 图片
    private List<PersonObject> persons;     // 不用聚类的时候的返回结果
    private List<GroupByPkey> groupByPkeys;  // 根据pkey 分类后的返回结果

    public String getSearchRowkey() {
        return searchRowkey;
    }

    public void setSearchRowkey(String searchRowkey) {
        this.searchRowkey = searchRowkey;
    }

    public int getSearchNums() {
        return searchNums;
    }

    public void setSearchNums(int searchNums) {
        this.searchNums = searchNums;
    }

    public List<byte[]> getSearchPhotos() {
        return searchPhotos;
    }

    public void setSearchPhotos(List<byte[]> searchPhotos) {
        this.searchPhotos = searchPhotos;
    }

    public List<PersonObject> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonObject> persons) {
        this.persons = persons;
    }

    public List<GroupByPkey> getGroupByPkeys() {
        return groupByPkeys;
    }

    public void setGroupByPkeys(List<GroupByPkey> groupByPkeys) {
        this.groupByPkeys = groupByPkeys;
    }

    @Override
    public String toString() {
        return "PersonSingleResult{" +
                "searchRowkey='" + searchRowkey + '\'' +
                ", searchNums=" + searchNums +
                ", searchPhotos=" + searchPhotos +
                ", persons=" + persons +
                ", groupByPkeys=" + groupByPkeys +
                '}';
    }
}
