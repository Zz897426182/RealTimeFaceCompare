package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.IPCIDService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;

public class IPCIDServiceImpl implements IPCIDService {

    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceImpl.class);

    /**
     * 添加IPCID与ftpAddress映射关系
     *
     * @param ipcID      设备ID
     * @param ftpAddress ftp地址
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertIPCID(String ipcID, String ftpAddress) {
        if (ipcID != null && ftpAddress != null) {
            Table IPCIDTable = HBaseHelper.getTable(DynamicTable.TABLE_IPCID);
            Put put = new Put(Bytes.toBytes(ipcID));
            put.addColumn(DynamicTable.IPCID_COLUMNFAMILY, DynamicTable.IPCID_COLUMN_FTPADDRESS, Bytes.toBytes(ftpAddress));
            try {
                IPCIDTable.put(put);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("insert ftpAddress by ipcID from table_IPCID failed!");
            } finally {
                HBaseUtil.closTable(IPCIDTable);
            }
        } else {
            LOG.warn("method param is empty.");
        }
        return false;
    }

    /**
     * 删除IPCID与ftpAddress映射关系
     *
     * @param ipcID 设备ID
     * @return boolean 是否删除成功
     */
    @Override
    public boolean deleteIPCID(String ipcID) {
        if (ipcID != null) {
            Table IPCIDTable = HBaseHelper.getTable(DynamicTable.TABLE_IPCID);
            Delete delete = new Delete(Bytes.toBytes(ipcID));
            delete.addColumn(DynamicTable.IPCID_COLUMNFAMILY, DynamicTable.IPCID_COLUMN_FTPADDRESS);
            try {
                IPCIDTable.delete(delete);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("delete ftpAddress by ipcID from table_IPCID failed!");
            } finally {
                HBaseUtil.closTable(IPCIDTable);
            }
        } else {
            LOG.warn("method param is empty.");
        }
        return false;
    }

    /**
     * 更新IPCID与ftpAddress映射关系
     *
     * @param ipcID      设备ID
     * @param ftpAddress ftp地址
     * @return boolean 是否更新成功
     */
    @Override
    public boolean updateIPCID(String ipcID, String ftpAddress) {
        boolean b = insertIPCID(ipcID, ftpAddress);
        return b;
    }

    /**
     * 查询IPCID与ftpAddress映射关系
     *
     * @param ipcID 设备ID
     * @return String IPCID与ftpAddress映射关系
     */
    @Override
    public String getFtpAddress(String ipcID) {
        if (ipcID != null) {
            Table IPCIDTable = HBaseHelper.getTable(DynamicTable.TABLE_IPCID);
            Get get = new Get(Bytes.toBytes(ipcID));
            try {
                Result result = IPCIDTable.get(get);
                String ftpAddress = Bytes.toString(result.getValue(DynamicTable.IPCID_COLUMNFAMILY, DynamicTable.IPCID_COLUMN_FTPADDRESS));
                return ftpAddress;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("get ftpAddress by ipcID from table_IPCID failed!");
            } finally {
                HBaseUtil.closTable(IPCIDTable);
            }
        } else {
            LOG.warn("method param is empty.");
        }
        return null;
    }
}
