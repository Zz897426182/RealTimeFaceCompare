package com.hzgc.dubbo.attribute;

import java.io.Serializable;
import java.util.List;

/**
 * 人脸特征属性对象
 */
public class Attribute implements Serializable {
    /**
     * 属性字段名称，平台传过来的是枚举类的类名称，数据库查询时需要转小写
     */
    private String identify;
    /**
     * 属性中文描述
     */
    private String desc;
    /**
     * 逻辑关系,AND,OR
     */
    private Logistic logistic;
    /**
     * 属性值
     */
    private List<AttributeValue> values;

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Logistic getLogistic() {
        return logistic;
    }

    public void setLogistic(Logistic logistic) {
        this.logistic = logistic;
    }

    public List<AttributeValue> getValues() {
        return values;
    }

    public void setValues(List<AttributeValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "identify='" + identify + '\'' +
                ", desc='" + desc + '\'' +
                ", logistic=" + logistic +
                ", values=" + values +
                '}';
    }
}
