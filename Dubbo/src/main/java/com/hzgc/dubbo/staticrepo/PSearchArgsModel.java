package com.hzgc.dubbo.staticrepo;

import com.hzgc.dubbo.feature.FaceAttribute;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 即时查询的时候传过来的参数
 */
public class PSearchArgsModel implements Serializable {
    private String rowkey;  // 即id
    private String paltaformId; // 平台Id
    private String name;  // 姓名
    private String idCard;  // 身份证号
    private int sex; // 性别

    private Map<String,byte[]> images;  //图片列表
    private Map<String,FaceAttribute> faceAttributeMap; //特征值列表
    private boolean theSameMan; //是否是同一个人
    private List<StaticSortParam> staticSortParams; //排序参数

    private float thredshold; // 阈值
    private List<String> pkeys; // 人员类型列表
    private String creator; // 布控人
    private String cphone; // 布控人手机号
    private int start;  // 开始的行数
    private int pageSize;  // 需要返回多少行
    private String searchId;  // 搜索Id
    private String searchType; // 搜索类型，对应的是调用的函数的名字
    private boolean moHuSearch; // 是否模糊查询， true ,是，false 不是
    private int important; //0,重点关注，1非重点关注
    private int status;  // 0 ,常住人口，1 建议迁出

    public PSearchArgsModel() {
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

    public Map<String, byte[]> getImages() {
        return images;
    }

    public void setImages(Map<String, byte[]> images) {
        this.images = images;
    }

    public Map<String, FaceAttribute> getFaceAttributeMap() {
        return faceAttributeMap;
    }

    public void setFaceAttributeMap(Map<String, FaceAttribute> faceAttributeMap) {
        this.faceAttributeMap = faceAttributeMap;
    }

    public boolean isTheSameMan() {
        return theSameMan;
    }

    public void setTheSameMan(boolean theSameMan) {
        this.theSameMan = theSameMan;
    }

    public List<StaticSortParam> getStaticSortParams() {
        return staticSortParams;
    }

    public void setStaticSortParams(List<StaticSortParam> staticSortParams) {
        this.staticSortParams = staticSortParams;
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

    public int getImportant() {
        return important;
    }

    public void setImportant(int important) {
        this.important = important;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PSearchArgsModel{" +
                "rowkey='" + rowkey + '\'' +
                ", paltaformId='" + paltaformId + '\'' +
                ", name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", sex=" + sex +
                ", images=" + images +
                ", faceAttributeMap=" + faceAttributeMap +
                ", theSameMan=" + theSameMan +
                ", staticSortParams=" + staticSortParams +
                ", thredshold=" + thredshold +
                ", pkeys=" + pkeys +
                ", creator='" + creator + '\'' +
                ", cphone='" + cphone + '\'' +
                ", start=" + start +
                ", pageSize=" + pageSize +
                ", searchId='" + searchId + '\'' +
                ", searchType='" + searchType + '\'' +
                ", moHuSearch=" + moHuSearch +
                ", important=" + important +
                ", status=" + status +
                '}';
    }
}
