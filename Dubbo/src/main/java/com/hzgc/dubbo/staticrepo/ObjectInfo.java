package com.hzgc.dubbo.staticrepo;

import java.util.Arrays;

public class ObjectInfo {
    private String id;  // rowkey
    private String name;  // 姓名
    private String idcard; // 身份证
    private byte sex;   // 性别
    private byte[] photo;  // 照片
    private String feature; // 特征值
    private String reason;  // 理由
    private String pkey;   // 人员类型key
    private String tag;    //  是人还是车
    private String creator;  // 布控人
    private String cphone;   // 布控人联系方式
    private String createtime; // 创建时间
    private String updatetime;   // 修改时间

    public ObjectInfo() {
    }

    public ObjectInfo(String id, String name, String idcard, byte sex,
                      byte[] photo, String feature, String reason, String pkey, String tag,
                      String createtime, String updatetime, String creator, String cphone) {
        this.id = id;
        this.name = name;
        this.idcard = idcard;
        this.sex = sex;
        this.photo = photo;
        this.feature = feature;
        this.reason = reason;
        this.pkey = pkey;
        this.tag = tag;
        this.createtime = createtime;
        this.updatetime = updatetime;
        this.creator = creator;
        this.cphone = cphone;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public byte getSex() {
        return sex;
    }

    public void setSex(byte sex) {
        this.sex = sex;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
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

    @Override
    public String toString() {
        return "ObjectInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", idcard='" + idcard + '\'' +
                ", sex=" + sex +
                ", photo=" + Arrays.toString(photo) +
                ", feature=" + feature +
                ", reason='" + reason + '\'' +
                ", pkey='" + pkey + '\'' +
                ", tag='" + tag + '\'' +
                ", creator='" + creator + '\'' +
                ", cphone='" + cphone + '\'' +
                ", createtime='" + createtime + '\'' +
                ", updatetime='" + updatetime + '\'' +
                '}';
    }
}
