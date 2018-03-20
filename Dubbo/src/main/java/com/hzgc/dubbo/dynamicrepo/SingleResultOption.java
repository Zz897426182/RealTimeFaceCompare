package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SingleResultOption implements Serializable {
    private String id;    //子ID
    private List<String> ipcList;    //传入的设备列表,在按设备归类并进行分页查询时有效

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getIpcList() {
        return ipcList;
    }

    public void setIpcList(List<String> ipcList) {
        this.ipcList = ipcList;
    }

    @Override
    public String toString() {
        return "Single search id is "
                + id
                + " ipc list is "
                + (null == ipcList ? "null" :
                Arrays.toString(ipcList.toArray()));
    }
}
