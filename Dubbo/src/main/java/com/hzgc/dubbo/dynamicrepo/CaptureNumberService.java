package com.hzgc.dubbo.dynamicrepo;

import java.util.List;
import java.util.Map;

/**
 * 这个接口是为了大数据可视化而指定的，主要包含三个方法：
 * 1、dynaicNumberService：查询es的动态库，返回总抓拍数量和今日抓拍数量
 * 2、staticNumberService：查询es的静态库，返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
 * 3、timeSoltNumber：根据入参ipcid的list、startTime和endTime去es查询到相应的值
 */
public interface CaptureNumberService {
    String totolNum ="totolNumber";
    String todyTotolNumber ="todyTotolNumber";

    /**
     * 查询es的动态库，返回总抓拍数量和今日抓拍数量
     * @param ipcId 设备ID：ipcId
     * @return 返回总抓拍数和今日抓拍数量
     */
    Map<String,Integer> dynaicNumberService(List<String> ipcId);

    /**
     * 查询es的静态库，返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
     * @param platformId 平台ID
     * @return 返回对应平台下的每个pkey的数量
     */
    Map<String,Integer> staticNumberService(String platformId);


    /**
     * 根据入参ipcid的list、startTime和endTime去es查询到相应的值
     *
     * @param ipcIds 设备ID
     * @param startTime 搜索的开始时间
     * @param endTime 搜索的结束时间
     * @return 返回对应这些设备ID的这段时间内每一小时抓拍的总数量
     */
    Map<String,Integer> timeSoltNumber(List<String> ipcIds, String startTime, String endTime);
}
