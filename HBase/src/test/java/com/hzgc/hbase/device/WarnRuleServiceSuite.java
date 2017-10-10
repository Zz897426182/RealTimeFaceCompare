package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.hbase.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class WarnRuleServiceSuite {
    @Test
    public void testConfigRules(){
       WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
       Map<String,Boolean> map =  warnRuleService.configRules(PutData.setIpcId(),PutData.setwarnRuleData());
        System.out.println(map);
    }
    @Test
    public void testAddRules(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        Map<String,Boolean> map = warnRuleService.addRules(PutData.setIpcId(),PutData.setwarnRuleData());
        System.out.println(map);
    }
    @Test
    public void testGetCompareRules(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        String ipcid = "hhh100";
        List<WarnRule> list = warnRuleService.getCompareRules(ipcid);
        System.out.println(list);
    }
    @Test
    public void testDeleteRules(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        Map<String, Boolean> map = warnRuleService.deleteRules(PutData.setIpcId());
        System.out.println(map);
    }
    @Test
    public void testObjectTypeHasRule(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        String type = "teacher";
        List<String> list = warnRuleService.objectTypeHasRule(type);
        System.out.println(list);
    }
    @Test
    public void testDeleteObjectTypeOfRules(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        String type = "teacher";
        int num = warnRuleService.deleteObjectTypeOfRules(type,PutData.setIpcId());
        System.out.println(num);
    }
    @Test
    public void testAddPutObjectTypeInfo(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        Table obj = HBaseHelper.getTable("objToDevice");
        //warnRuleService.addPutObjectTypeInfo(PutData.objType(),obj);
    }
    @Test
    public void testParseDeviceRule(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
       // warnRuleService.parseDeviceRule(PutData.setwarnRuleData(),PutData.setIpcId(),PutData.commonRule());
    }
    @Test
    public void testAddMembers(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        WarnRule warnRule1 = new WarnRule();
        warnRule1.setCode(0);
        warnRule1.setDayThreshold(3);
        warnRule1.setThreshold(70);
        warnRule1.setObjectType("police");
        String ipcid = "hhh100";
        System.out.println(warnRule1);
    }
    @Test
    public void testParseOfflineWarn(){
        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
        WarnRule warnRule1 = new WarnRule();
        warnRule1.setCode(2);
        warnRule1.setDayThreshold(3);
        warnRule1.setThreshold(70);
        warnRule1.setObjectType("police");
        String ipcid = "hhh100";
        //warnRuleService.parseOfflineWarn(warnRule1,ipcid,PutData.offlineMap());
    }
}
