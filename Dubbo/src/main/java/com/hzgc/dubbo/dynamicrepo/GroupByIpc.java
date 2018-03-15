package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.List;

public class GroupByIpc implements Serializable {
    private String ipc;    //ipcID
    private List<CapturedPicture> pictures;    //抓拍图片
    private int total;    //当前设备图片总计

    public String getIpc() {
        return ipc;
    }

    public void setIpc(String ipc) {
        this.ipc = ipc;
    }

    public List<CapturedPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<CapturedPicture> pictures) {
        this.pictures = pictures;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
