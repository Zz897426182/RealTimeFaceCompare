package com.hzgc.cluster.message;

import java.io.Serializable;

/**
 * 识别告警静态库比对结果类。（刘善彬）
 */
public class Item implements Serializable {

    /**
     * 静态库id
     */
    private String staticID;

    /**
     * 相似度
     */
    private String similarity;

    /**
     * 对象类型
     */
    private String objType;

    /**
     * 构造函数
     **/
    public Item(String staticID, String similarity,String objType) {
        this.staticID = staticID;
        this.similarity = similarity;
        this.objType=objType;
    }

    public Item() {
    }

    /**
     * Getter and Setter
     **/
    public String getStaticID() {
        return staticID;
    }

    public void setStaticID(String staticID) {
        this.staticID = staticID;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

}
