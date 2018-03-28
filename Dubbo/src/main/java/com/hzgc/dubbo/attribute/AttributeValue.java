package com.hzgc.dubbo.attribute;

import java.io.Serializable;

/**
 * 单个属性值
 */
public class AttributeValue implements Serializable {
    /**
     * 属性的值
     */
    private Integer value;
    /**
     * 值描述
     */
    private String desc;
    /**
     * 属性统计（属性统计接口使用）
     */
    private long count;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "AttributeValue{" +
                "value=" + value +
                ", desc='" + desc + '\'' +
                ", count=" + count +
                '}';
    }
}
