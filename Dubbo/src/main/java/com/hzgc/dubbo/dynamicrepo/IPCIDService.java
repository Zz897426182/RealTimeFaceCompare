package com.hzgc.dubbo.dynamicrepo;

public interface IPCIDService {
    /**
     * 添加IPCID与ftpAddress映射关系
     *
     * @param ipcID      设备ID
     * @param ftpAddress ftp地址
     * @return boolean 是否插入成功
     */
    boolean insertIPCID(String ipcID, String ftpAddress);

    /**
     * 删除IPCID与ftpAddress映射关系
     *
     * @param ipcID 设备ID
     * @return boolean 是否删除成功
     */
    boolean deleteIPCID(String ipcID);

    /**
     * 更新IPCID与ftpAddress映射关系
     *
     * @param ipcID      设备ID
     * @param ftpAddress ftp地址
     * @return boolean 是否更新成功
     */
    boolean updateIPCID(String ipcID, String ftpAddress);

    /**
     * 查询IPCID与ftpAddress映射关系
     *
     * @param ipcID 设备ID
     * @return String IPCID与ftpAddress映射关系
     */
    String getFtpAddress(String ipcID);
}
