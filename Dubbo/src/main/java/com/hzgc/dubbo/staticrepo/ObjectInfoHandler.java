package com.hzgc.dubbo.staticrepo;

import java.util.List;
import java.util.Map;

/**
 * 处理针对静态信息库的增删改查操作
 */
public interface ObjectInfoHandler {

    /**
     * 针对单个对象信息的添加处理  （外）（李第亮）
     * @param  platformId 表示的是平台的ID， 平台的唯一标识。
     * @param person K-V 对，里面存放的是字段和值之间的一一对应关系,
     *               例如：传入一个Map 里面的值如下map.put("idcard", "450722199502196939")
     *               表示的是身份证号（idcard）是450722199502196939，
     *               其中的K 的具体，请参考给出的数据库字段设计
     * @return 返回值为0，表示插入成功，返回值为1，表示插入失败
     */
    byte addObjectInfo(String platformId, Map<String, Object> person);

    /**
     * 删除对象的信息  （外）（李第亮）
     * @param rowkeys 具体的一个人员信息的ID，值唯一
     * @return 返回值为0，表示删除成功，返回值为1，表示删除失败
     */
    int deleteObjectInfo(List<String> rowkeys);

    /**
     * 修改对象的信息   （外）（李第亮）
     * @param person K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    int updateObjectInfo(Map<String, Object> person);

    /**
     * 可以匹配精确查找，以图搜索人员信息，模糊查找   （外）（李第亮）
     * @param pSearchArgsModel 搜索参数的封装
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel);

    /**
     * 根据传进来的平台id  和身份证号进行查询  （外）（李第亮）
     * @param platformId  平台ID
     * @param IdCard  身份证号
     * @param moHuSearch  是否模糊查询
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId,
                                                   String IdCard, boolean moHuSearch,
                                                   int start, int pageSize);

    /**
     * 根据rowkey 进行查询 （外）
     * @param rowkey  标记一条对象信息的唯一标志。
     * @return  返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByRowkey(String rowkey);


    /**
     * 根据布控人手机号进行查询  （外）
     * @param cphone 布控人手机号
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByCphone(String cphone, int start, int pageSize);


    /**
     * 根据布控人姓名进行查询  （外）
     * @param creator  布控人姓名
     * @param moHuSearch  是否模糊查询
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return  返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                       int start, int pageSize);

    /**
     * 根据人员信息表中的人员姓名进行查询   （外） （李第亮）
     * @param name 根据人员姓名进行查询
     * @param moHuSearch  是否模糊查询
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return  返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                    int start, int pageSize);

    /**
     * 根据图片进行搜索，涉及特征值对比算法，以及该怎么对比。  （外） （李第亮）
     * 需要保存这张图片和搜索出来的记录
     * @param platformId 平台ID
     * @param photo 图片
     * @param threshold 阈值
     * @param feature 特征值
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    ObjectSearchResult searchByPhotoAndThreshold(String platformId, byte[] photo,
                                                 float threshold, String feature,
                                                 long start, long pageSize);

    /**
     *  根据传进来的图片和人车标志，计算特征值，并且返回  （外） （李第亮）
     * @param tag  人车标志
     * @param photo  照片byte 数组
     * @return  照片的特征值
     */
    String getFeature(String tag, byte[] photo);

    /**
     * 根据rowkey 返回人员的照片
     * @param rowkey 人员在对象信息库中的唯一标志。
     * @return 图片的byte[] 数组
     */
    byte[] getPhotoByKey(String rowkey);




}
