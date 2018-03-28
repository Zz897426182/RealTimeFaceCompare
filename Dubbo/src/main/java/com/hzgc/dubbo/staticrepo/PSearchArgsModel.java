package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class PSearchArgsModel implements Serializable {
    private String rowkey;  // 即id
    private String paltaformId; // 平台Id
    private String name;  // 姓名
    private String idCard;  // 身份证号
    private int sex; // 性别
    private byte[] image; // 图片
    private String feature; // 特征值
    private float thredshold; // 阈值
    private List<String> pkeys; // 人员类型列表
    private String creator; // 布控人
    private String cphone; // 布控人手机号
    private int start;  // 开始的行数
    private int pageSize;  // 需要返回多少行
    private String searchId;  // 搜索Id
    private String searchType; // 搜索类型，对应的是调用的函数的名字
    private boolean moHuSearch; // 是否模糊查询， true ,是，false 不是

    public PSearchArgsModel() {
    }

    public PSearchArgsModel(String rowkey, String paltaformId,
                            String name, String idCard, int sex,
                            byte[] image, String feature,
                            int thredshold, List<String> pkeys,
                            String creator, String cphone,
                            int start, int pageSize,
                            String searchId, String searchType,
                            boolean moHuSearch) {
        this.rowkey = rowkey;
        this.paltaformId = paltaformId;
        this.name = name;
        this.idCard = idCard;
        this.sex = sex;
        this.image = image;
        this.feature = feature;
        this.thredshold = thredshold;
        this.pkeys = pkeys;
        this.creator = creator;
        this.cphone = cphone;
        this.start = start;
        this.pageSize = pageSize;
        this.searchId = searchId;
        this.searchType = searchType;
        this.moHuSearch = moHuSearch;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getPaltaformId() {
        return paltaformId;
    }

    public void setPaltaformId(String paltaformId) {
        this.paltaformId = paltaformId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public float getThredshold() {
        return thredshold;
    }

    public void setThredshold(float thredshold) {
        this.thredshold = thredshold;
    }

    public List<String> getPkeys() {
        return pkeys;
    }

    public void setPkeys(List<String> pkeys) {
        this.pkeys = pkeys;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCphone() {
        return cphone;
    }

    public void setCphone(String cphone) {
        this.cphone = cphone;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public boolean isMoHuSearch() {
        return moHuSearch;
    }

    public void setMoHuSearch(boolean moHuSearch) {
        this.moHuSearch = moHuSearch;
    }

    @Override
    public String toString() {
        return "PSearchArgsModel{" +
                "rowkey='" + rowkey + '\'' +
                ", paltaformId='" + paltaformId + '\'' +
                ", name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", sex=" + sex +
                ", image=" + Arrays.toString(image) +
                ", feature='" + feature + '\'' +
                ", thredshold=" + thredshold +
                ", pkeys=" + pkeys +
                ", creator='" + creator + '\'' +
                ", cphone='" + cphone + '\'' +
                ", start=" + start +
                ", pageSize=" + pageSize +
                ", searchId='" + searchId + '\'' +
                ", searchType='" + searchType + '\'' +
                ", moHuSearch=" + moHuSearch +
                '}';
    }
}
