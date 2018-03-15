package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.sql.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


/**
 * 静态库人员中每个人的信息
 */
public class PersonObject implements Serializable{
    private String id;  // 数据库中的唯一标志
    private String pkey;  // 对象类型key
    private String platformid;  // 平台Id
    private String name;  // 姓名
    private int sex;   // 性别
    private String idcard;  // 身份证号
    private byte[] photo;   // 照片
    private float[] feature;  // 特征值
    private String creator;   // 创建者
    private String cphone;  // 创建者手机号
    private Date createtime;  // 创建时间
    private Date updatetime;   // 更新时间
    private String reason;   // 布控理由
    private String tag;  // 人车标志
    private int important; // 0,重点关注，1，非重点关注
    private int status; // 0,常住人口，1，建议迁出
    private float threthold; // 相似度

    public float getThrethold() {
        return threthold;
    }

    public void setThrethold(float threthold) {
        this.threthold = threthold;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getPlatformid() {
        return platformid;
    }

    public void setPlatformid(String platformid) {
        this.platformid = platformid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
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

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
        return "PersonObject{" +
                "id='" + id + '\'' +
                ", pkey='" + pkey + '\'' +
                ", platformid='" + platformid + '\'' +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", idcard='" + idcard + '\'' +
                ", photo=" + Arrays.toString(photo) +
                ", feature=" + Arrays.toString(feature) +
                ", creator='" + creator + '\'' +
                ", cphone='" + cphone + '\'' +
                ", createtime=" + createtime +
                ", updatetime=" + updatetime +
                ", reason='" + reason + '\'' +
                ", tag='" + tag + '\'' +
                ", important=" + important +
                ", status=" + status +
                ", threthold=" + threthold +
                '}';
    }

    public static PersonObject mapToPersonObject(Map<String, Object> person) {
        PersonObject personObject = new PersonObject();

        String id = UUID.randomUUID().toString().replace("-", "");
        personObject.setId(id);
        personObject.setPkey((String) person.get(ObjectInfoTable.PKEY));
        personObject.setPlatformid((String) person.get(ObjectInfoTable.PLATFORMID));
        personObject.setName((String) person.get(ObjectInfoTable.NAME));
        if (person.get(ObjectInfoTable.SEX) != null) {
            personObject.setSex((Integer) person.get(ObjectInfoTable.SEX));
        }
        personObject.setIdcard((String) person.get(ObjectInfoTable.IDCARD));
        personObject.setPhoto((byte[]) person.get(ObjectInfoTable.PHOTO));
        personObject.setFeature((float[]) person.get(ObjectInfoTable.FEATURE));
        personObject.setCreator((String) person.get(ObjectInfoTable.CREATOR));
        personObject.setCphone((String) person.get(ObjectInfoTable.CPHONE));
        personObject.setReason((String) person.get(ObjectInfoTable.REASON));
        personObject.setTag((String) person.get(ObjectInfoTable.TAG));
        if (person.get(ObjectInfoTable.IMPORTANT) != null) {
            personObject.setImportant((Integer) person.get(ObjectInfoTable.IMPORTANT));
        }
        if (person.get(ObjectInfoTable.STATUS) != null) {
            personObject.setStatus((Integer) person.get(ObjectInfoTable.STATUS));
        }
        long dateNow = System.currentTimeMillis();
        personObject.setUpdatetime(new Date(dateNow));
        return personObject;
    }

    public static Object[] otherArrayToObject(float [] in) {
        if (in == null || in.length <= 0) {
            return null;
        }
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    public static Object[] otherArrayToObject(byte [] in) {
        if (in == null || in.length <= 0) {
            return null;
        }
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }
}
