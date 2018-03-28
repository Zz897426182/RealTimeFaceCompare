package com.hzgc.service.device;

import org.junit.Test;
import java.util.Map;

/**
 * Created by Administrator on 2017-8-4.
 */
public class DeviceServiceSuite {
    @Test
    public void testBindDevice(){
        String platformId = "hhhhh100";
        String ipcID = "hhh100";
        String notes = "police";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a = deviceService.bindDevice(platformId,ipcID,notes);
        System.out.println(a);
    }
    @Test
    public void testUnbindDevice(){
        String platformId = "hhhhh100";
        String ipcID = "hhh100";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a = deviceService.unbindDevice(platformId,ipcID);
        System.out.println(a);
    }
    @Test
    public void testRenameNotes(){
        String notes = "policeff";
        String ipcID = "hhh100";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a  = deviceService.renameNotes(notes,ipcID);
        System.out.println(a);
    }
    @Test
    public void testGetplatfromID(){
        String ipcID = "hhh100";
        DeviceUtilImpl deviceUtil = new DeviceUtilImpl();
        String a = deviceUtil.getplatfromID(ipcID);
        System.out.println(a);
    }
    @Test
    public void testIsWarnTypeBinding(){
        String ipcID = "hhh100";
        DeviceUtilImpl deviceUtil = new DeviceUtilImpl();
        Map<Integer, Map<String, Integer>> a = deviceUtil.isWarnTypeBinding(ipcID);
        System.out.println(a);
    }
    @Test
    public void testGetThreshold(){
        DeviceUtilImpl deviceUtil = new DeviceUtilImpl();
        Map<String, Map<String, Integer>> a = deviceUtil.getThreshold();
        System.out.println(a);
    }
}
