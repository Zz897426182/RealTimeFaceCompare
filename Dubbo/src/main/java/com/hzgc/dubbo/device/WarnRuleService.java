package com.hzgc.dubbo.device;

import java.util.List;
import java.util.Map;

public interface WarnRuleService {
    /**
     * 配置布控规则（外）（赵喆）
     * 设置多个设备的对比规则，如果之前存在对比规则，先清除之前的规则，再重新写入
     *
     * @param ipcIDs 设备 ipcID 列表
     * @param rules         对比规则
     * @return 是否设置成功
     */
    Map<String,Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules);

    /**
     * 添加布控规则（外）（赵喆）
     *
     * @param ipcIDs 设备 ipcID 列表
     * @param rules         对比规则
     * @return boxChannelId 是否添加成功的 map
     */
    Map<String,Boolean> addRules(List<String> ipcIDs, List<WarnRule> rules);

    /**
     * 获取设备的对比规则（外）（赵喆）
     *
     * @param ipcID 设备 ipcID
     * @return 设备的布控规则
     */
    List<WarnRule> getCompareRules(String ipcID);

    /**
     * 删除设备的布控规则（外）（赵喆）
     *
     * @param ipcIDs 设备 ipcID 列表
     * @return channelId 是否删除成功的 map
     */
    Map<String,Boolean> deleteRules(List<String> ipcIDs);


    /**
     * 查看有多少设备绑定了此人员类型objectType（外）（赵喆）
     *
     * @param objectType  识别库 id
     * @return ipcIDs
     */
    List<String> objectTypeHasRule(String objectType);

    /**
     * 在所有设备列表绑定的规则中，删除此objectType（外）（赵喆）
     *
     * @param objectType 对象类型
     * @param ipcIDs 设备列表
     * @return 删除成功的条数
     */
    int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs);
}
