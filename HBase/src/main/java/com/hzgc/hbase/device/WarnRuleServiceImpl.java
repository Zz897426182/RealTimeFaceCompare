package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.dubbo.device.WarnRuleService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class WarnRuleServiceImpl implements WarnRuleService {
    private static Logger LOG = Logger.getLogger(WarnRuleServiceImpl.class);

    /**
     * 配置布控规则（外）（赵喆）
     * 设置多个设备的对比规则，如果之前存在对比规则，先清除之前的规则，再重新写入
     */
    @Override
    public Map<String, Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Table objTypeTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
        Map<Integer, Map<String, Integer>> commonRule = new HashMap<>();
        Map<String, Map<String, Map<Integer, String>>> objType = new HashMap<>();
        Map<String, Map<String, Integer>> offlineMap = new HashMap<>();
        List<Put> putList = new ArrayList<>();
        Map<String, Boolean> reply = new HashMap<>();

        parseDeviceRule(rules, ipcIDs, commonRule);
        byte[] commonRuleBytes = ObjectUtil.objectToByte(commonRule);
        for (String ipcID : ipcIDs) {
            Put put = new Put(Bytes.toBytes(ipcID));
            put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
            putList.add(put);
            for (WarnRule rule : rules) {
                addMembers(objType, rule, ipcID);
                parseOfflineWarn(rule, ipcID, offlineMap);
            }
            reply.put(ipcID, true);
        }
        try {
            deviceTable.put(putList);
            configPutObjectTypeInfo(objType, objTypeTable);
            configOfflineWarn(offlineMap, deviceTable);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            HBaseUtil.closTable(deviceTable);
            HBaseUtil.closTable(objTypeTable);
        }
        return reply;
    }

    /**
     *添加布控规则（外）（赵喆）
     */
    @Override
    public Map<String, Boolean> addRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Table objTypeTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
        Map<Integer, Map<String, Integer>> commonRule = new HashMap<>();
        Map<String, Map<String, Map<Integer, String>>> objType = new HashMap<>();
        Map<String, Map<String, Integer>> offlineMap = new HashMap<>();
        List<Put> putList = new ArrayList<>();
        Map<String, Boolean> reply = new HashMap<>();

        parseDeviceRule(rules, ipcIDs, commonRule);
        byte[] commonRuleBytes = ObjectUtil.objectToByte(commonRule);
        for (String ipcID : ipcIDs) {
            Put put = new Put(Bytes.toBytes(ipcID));
            put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
            putList.add(put);
            for (WarnRule rule : rules) {
                addMembers(objType, rule, ipcID);
                parseOfflineWarn(rule, ipcID, offlineMap);
            }
            reply.put(ipcID, true);
        }
        try {
            for (String ipcID : ipcIDs) {
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = deviceTable.get(get);
                if (!result.isEmpty()) {
                    Map<Integer, Map<String, Integer>> tempMap =
                            deSerializDevice(result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN));
                    for (Integer code : commonRule.keySet()) {
                        if (tempMap.containsKey(code)) {
                            for (String type : commonRule.get(code).keySet()) {
                                tempMap.get(code).put(type, commonRule.get(code).get(type));
                            }
                        } else {
                            tempMap.put(code, commonRule.get(code));
                        }
                    }
                    Put put = new Put(Bytes.toBytes(ipcID));
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(tempMap));
                    putList.add(put);
                } else {
                    Put put = new Put(Bytes.toBytes(ipcID));
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
                    putList.add(put);
                }
            }
            deviceTable.put(putList);
            addPutObjectTypeInfo(objType, objTypeTable);
            configOfflineWarn(offlineMap, deviceTable);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            HBaseUtil.closTable(deviceTable);
            HBaseUtil.closTable(objTypeTable);
        }
        return reply;
    }

    /**
     * 获取设备的对比规则 （外）（赵喆）
     */
    @Override
    public List<WarnRule> getCompareRules(String ipcID) {
        List<WarnRule> reply = new ArrayList<>();
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID)) {
            try {
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = table.get(get);
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN)) {
                    byte[] deviceByte = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                    Map<Integer, Map<String, Integer>> deviceMap = deSerializDevice(deviceByte);
                    for (Integer code : deviceMap.keySet()) {
                        Map<String, Integer>  tempMap = deviceMap.get(code);
                        for (String type : deviceMap.get(code).keySet()) {
                            WarnRule warnRule = new WarnRule();
                            warnRule.setCode(code);
                            warnRule.setObjectType(type);
                            if (code == DeviceTable.OFFLINE) {
                                warnRule.setDayThreshold(tempMap.get(type));
                            } else {
                                warnRule.setThreshold(tempMap.get(type));
                            }
                            reply.add(warnRule);
                        }
                    }
                    return reply;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    /**
     * 删除设备的布控规则（外）（赵喆）
     */
    @Override
    public Map<String, Boolean> deleteRules(List<String> ipcIDs) {
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Table objTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
        Map<String, Boolean> reply = new HashMap<>();
        List<Delete> deviceDelList = new ArrayList<>();
        List<Delete> objDelList = new ArrayList<>();
        List<Put> objPutList = new ArrayList<>();
        String id = "";
        if (ipcIDs != null) {
            try {
                for (String ipc : ipcIDs) {
                    id = ipc;
                    Delete deviceDelete = new Delete(Bytes.toBytes(ipc));
                    deviceDelete.addColumns(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                    deviceDelList.add(deviceDelete);
                    reply.put(ipc, true);
                    LOG.info("release the rule binding, the device ID is:" + ipc);
                }
                Get offlineGet = new Get(DeviceTable.OFFLINERK);
                Result offlineResult = deviceTable.get(offlineGet);
                if (!offlineResult.isEmpty()) {
                    Map<String, Map<String, Integer>> offlineMap =
                            deSerializOffLine(offlineResult.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL));
                    for (String type : offlineMap.keySet()) {
                        for (String ipc : ipcIDs) {
                            offlineMap.get(type).remove(ipc);
                        }
                    }
                    Put offlinePut = new Put(DeviceTable.OFFLINERK);
                    offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offlineMap));
                    deviceTable.put(offlinePut);
                }
                Scan scan = new Scan();
                ResultScanner objscanner = objTable.getScanner(scan);
                for (Result result : objscanner) {
                    Get get = new Get(result.getRow());
                    Result tempResult = objTable.get(get);
                    Map<String, Map<Integer, String>> tempMap =
                            deSerializObjToDevice(tempResult.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL));
                    for (String ipc : ipcIDs) {
                        tempMap.remove(ipc);
                    }
                    if (tempMap.isEmpty()) {
                        Delete delete = new Delete(result.getRow());
                        delete.addColumns(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL);
                        objDelList.add(delete);
                    } else {
                        Put put = new Put(result.getRow());
                        put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(tempMap));
                        objPutList.add(put);
                    }
                }
                deviceTable.delete(deviceDelList);;
                objTable.delete(objDelList);
                objTable.put(objPutList);
                return reply;
            } catch (IOException e) {
                reply.put(id,false);
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(deviceTable);
                HBaseUtil.closTable(objTable);
            }
        }
        return reply;
    }

    /**
     * 查看有多少设备绑定了此人员类型objectType （外）（赵喆）
     */
    @Override
    public List<String> objectTypeHasRule(String objectType) {
        Table table = null;
        List<String> reply = new ArrayList<>();
        try {
            table = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
            Get get = new Get(Bytes.toBytes(objectType));
            Result result = table.get(get);
            if (result.isEmpty()) {
                return reply;
            } else {
                Map<String, Map<Integer, String>> temMap =
                        deSerializObjToDevice(result.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL));
                for (String ipc : temMap.keySet()) {
                    reply.add(ipc);
                }
                return reply;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return reply;
    }

    /**
     * objectTypeHasRule(String objectType)的子方法 （外）（赵喆）
     */
    @Override
    public int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs) {
        Table objTable = null;
        Table deviceTable = null;
        List<Put> putList = new ArrayList<>();
        int count = 0;
        if (StringUtil.strIsRight(objectType) && ipcIDs != null) {
            try {
                objTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
                deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
                Get objGet = new Get(Bytes.toBytes(objectType));
                Result objResult = objTable.get(objGet);
                if (!objResult.isEmpty()) {
                    Map<String, Map<Integer, String>> objMap =
                            deSerializObjToDevice(objResult.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL));
                    for (String ipc : ipcIDs) {
                        objMap.remove(ipc);
                        Get deviceGet = new Get(Bytes.toBytes(ipc));
                        Result deviceResult = deviceTable.get(deviceGet);
                        if (!deviceResult.isEmpty()) {
                            Map<Integer, Map<String, Integer>> deviceMap =
                                    deSerializDevice(deviceResult.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN));
                            for (Integer code : deviceMap.keySet()) {
                                deviceMap.get(code).remove(objectType);
                            }
                            Put devicePut = new Put(Bytes.toBytes(ipc));
                            devicePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(deviceMap));
                            putList.add(devicePut);
                        }
                        Get offlineGet = new Get(DeviceTable.OFFLINERK);
                        Result offlineResult = deviceTable.get(offlineGet);
                        if (!offlineResult.isEmpty()) {
                            Map<String, Map<String, Integer>> offlineMap =
                                    deSerializOffLine(offlineResult.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL));
                            for (String type : offlineMap.keySet()) {
                                offlineMap.get(type).remove(ipc);
                            }
                            Put offlinePut = new Put(DeviceTable.OFFLINERK);
                            offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offlineMap));
                            putList.add(offlinePut);
                        }
                        count++;
                    }
                    Put objPut = new Put(Bytes.toBytes(objectType));
                    objPut.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(objMap));
                    objTable.put(objPut);
                    deviceTable.put(putList);
                }
                return count;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(deviceTable);
                HBaseUtil.closTable(objTable);
            }
        }
        return 0;
    }

    /**
     * config模式下向objToDevice表插入数据 （内部方法）
     */
    private void configPutObjectTypeInfo(Map<String, Map<String, Map<Integer, String>>> objType, Table objTypeTable) {
        List<Put> putList = new ArrayList<>();
        try {
            for (String type : objType.keySet()) {
                Get get = new Get(Bytes.toBytes(type));
                Result result = objTypeTable.get(get);
                if (result.containsColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL)) {
                    Map<String, Map<Integer, String>> typeMap = deSerializObjToDevice(
                            result.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL));
                    for (String ipc : objType.get(type).keySet()) {
                        if (typeMap.containsKey(ipc)) {
                            typeMap.remove(ipc);
                            typeMap.put(ipc, objType.get(type).get(ipc));
                        } else {
                            typeMap.put(ipc, objType.get(type).get(ipc));
                        }
                        Put put = new Put(Bytes.toBytes(type));
                        put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(typeMap));
                        putList.add(put);
                    }
                } else {
                    Map<String, Map<Integer, String>> typeMap = objType.get(type);
                    Put put = new Put(Bytes.toBytes(type));
                    put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(typeMap));
                    putList.add(put);
                }
            }
            objTypeTable.put(putList);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * config模式下配置离线告警（内部方法）
     */
    private void configOfflineWarn(Map<String, Map<String, Integer>> offlineMap, Table deviceTable) {
        try {
            Get offlinGet = new Get(DeviceTable.OFFLINERK);
            Result offlineResult = deviceTable.get(offlinGet);
            if (!offlineResult.isEmpty()) {
                Map<String, Map<String, Integer>> tempMap = deSerializOffLine(offlineResult.
                        getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL));
                for (String type : offlineMap.keySet()) {
                    if (tempMap.containsKey(type)) {
                        for (String ipc : offlineMap.get(type).keySet()) {
                            tempMap.get(type).put(ipc, offlineMap.get(type).get(ipc));
                        }
                    } else {
                        tempMap.put(type, offlineMap.get(type));
                    }
                }
                Put offlinePut = new Put(DeviceTable.OFFLINERK);
                offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(tempMap));
                deviceTable.put(offlinePut);
            } else {
                Put offlinePut = new Put(DeviceTable.OFFLINERK);
                offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offlineMap));
                deviceTable.put(offlinePut);
            }

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * 将添加完成收的objectType插入“objToDevice”表（内部方法）
     */
    private void addPutObjectTypeInfo(Map<String, Map<String, Map<Integer, String>>> objType, Table objTypeTable) {
        List<Put> putList = new ArrayList<>();
        try {
            for (String type : objType.keySet()) {
                Get get = new Get(Bytes.toBytes(type));
                Result result = objTypeTable.get(get);
                if (!result.isEmpty()) {
                    Map<String, Map<Integer, String>> typeMap = deSerializObjToDevice(
                            result.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL));
                    for (String ipc : objType.get(type).keySet()) {
                        if (typeMap.containsKey(ipc)) {
                            for (Integer code : objType.get(type).get(ipc).keySet()) {
                                typeMap.get(ipc).put(code, objType.get(type).get(ipc).get(code));
                            }
                        } else {
                            typeMap.put(ipc, objType.get(type).get(ipc));
                        }
                        Put put = new Put(Bytes.toBytes(type));
                        put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(typeMap));
                        putList.add(put);
                    }
                } else {
                    Map<String, Map<Integer, String>> typeMap = objType.get(type);
                    Put put = new Put(Bytes.toBytes(type));
                    put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(typeMap));
                    putList.add(put);
                }
            }
            objTypeTable.put(putList);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * 解析configRules()传入的布控规则，并在解析的同时同步其他相关数据（内部方法）
     */
    private void parseDeviceRule(List<WarnRule> rules,
                                 List<String> ipcIDs,
                                 Map<Integer, Map<String, Integer>> commonRule) {
        if (rules != null && commonRule != null && ipcIDs != null) {
            for (WarnRule rule : rules) {
                if (Objects.equals(rule.getCode(), DeviceTable.IDENTIFY) || Objects.equals(rule.getCode(), DeviceTable.ADDED)) {
                    if (commonRule.containsKey(rule.getCode())) {
                        commonRule.get(rule.getCode()).put(rule.getObjectType(), rule.getThreshold());
                    } else {
                        Map<String, Integer> temMap = new HashMap<>();
                        temMap.put(rule.getObjectType(), rule.getThreshold());
                        commonRule.put(rule.getCode(), temMap);
                    }
                }
                if (Objects.equals(rule.getCode(), DeviceTable.OFFLINE)) {
                    if (commonRule.containsKey(rule.getCode())) {
                        commonRule.get(rule.getCode()).put(rule.getObjectType(), rule.getDayThreshold());
                    } else {
                        Map<String, Integer> tempMap = new HashMap<>();
                        tempMap.put(rule.getObjectType(), rule.getDayThreshold());
                        commonRule.put(rule.getCode(), tempMap);
                    }
                }
            }
        }
    }

    /**
     * 向参数objectType对应的数据类型中添加成员（内部方法）
     */
    private static void addMembers(Map<String, Map<String, Map<Integer, String>>> objType, WarnRule rule, String ipcID) {
        if (objType.get(rule.getObjectType()) == null) {
            Map<String, Map<Integer, String>> ipcMap = new HashMap<>();
            Map<Integer, String> warnMap = new HashMap<>();
            warnMap.put(rule.getCode(), "");
            ipcMap.put(ipcID, warnMap);
            objType.put(rule.getObjectType(), ipcMap);
        } else {
            Map<String, Map<Integer, String>> ipcMap = objType.get(rule.getObjectType());
            if (ipcMap.containsKey(ipcID)) {
                ipcMap.get(ipcID).put(rule.getCode(), "");
            } else {
                Map<Integer, String> warnMap = new HashMap<>();
                warnMap.put(rule.getCode(), "");
                ipcMap.put(ipcID, warnMap);
            }
        }
    }

    /**
     * 解析离线告警（内部方法）
     */
    private void parseOfflineWarn(WarnRule rule,
                                  String ipcID,
                                  Map<String, Map<String, Integer>> offlineMap) {
        if (offlineMap.containsKey(rule.getObjectType())) {
            offlineMap.get(rule.getObjectType()).put(ipcID, rule.getDayThreshold());
        } else {
            Map<String, Integer> ipcMap = new HashMap<>();
            ipcMap.put(ipcID, rule.getCode());
            offlineMap.put(rule.getObjectType(), ipcMap);
        }
    }

    /**
     * objectType数据类型：Map<Integer, String>（内部方法）
     * 反序列化此数据类型
     */
    private Map<String, Map<Integer, String>> deSerializObjToDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<Integer, String>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 设备布控预案数据类型：Map<Integer, Map<String, Integer>>（内部方法）
     * 反序列化此数据类型
     */
    private Map<Integer, Map<String, Integer>> deSerializDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<Integer, Map<String, Integer>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 删除离线告警中某一objectType包含的某一ipcID（内部方法）
     */
    private Map<String, Map<String, Integer>> deSerializOffLine(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<String, Integer>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }
}
