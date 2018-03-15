package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SingleResult implements Serializable {
    private String id;    //查询子ID
    private List<byte[]> binPicture;    //图片二进制数据
    private int total;    //单一结果的结果总数
    /**
     * 非设备归类时的结果集
     * 在第一次查询返回结果是肯定是按此种集合返回
     * 后续再次查询时如果按照设备归类的则有可能按照picturesByIpc来返回
     */
    private List<CapturedPicture> pictures;
    private List<GroupByIpc> picturesByIpc;    //按设备归类时的结果集

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CapturedPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<CapturedPicture> pictures) {
        this.pictures = pictures;
    }

    public List<byte[]> getBinPicture() {
        return binPicture;
    }

    public void setBinPicture(List<byte[]> binPicture) {
        this.binPicture = binPicture;
    }

    public List<GroupByIpc> getPicturesByIpc() {
        return picturesByIpc;
    }

    public void setPicturesByIpc(List<GroupByIpc> picturesByIpc) {
        this.picturesByIpc = picturesByIpc;
    }

    @Override
    public String toString() {
        return "Single search id is:"
                + id
                + ", picture is:"
                + (binPicture != null ? "true":"false")
                + ", total is:"
                + total
                + ", CapturePicture"
                + Arrays.toString(pictures.toArray());
    }
}
