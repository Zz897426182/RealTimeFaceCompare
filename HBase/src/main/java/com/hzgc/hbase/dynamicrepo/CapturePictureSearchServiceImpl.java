package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * 以图搜图接口实现类，内含四个方法（外）（彭聪）
 */
public class CapturePictureSearchServiceImpl implements CapturePictureSearchService {
    private static Logger LOG = Logger.getLogger(CapturePictureSearchServiceImpl.class);

    /**
     * 接收应用层传递的参数进行搜图，如果大数据处理的时间过长，
     * 则先返回searchId,finished=false,然后再开始计算；如果能够在秒级时间内计算完则计算完后再返回结果
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    @Override
    public SearchResult search(SearchOption option) {
        Scan scan = new Scan();
        if (null != option) {
            if (!option.getDeviceIds().isEmpty() && null != option.getSearchType()) {
           //     List<String> rowKeyListByDeviceId = new FilterByRowkey().filterByDeviceId(option, scan);

            } else if (option.getPlateNumber() != null && option.getSearchType() == SearchType.CAR) {
                List<String> rowKeyListByPlateNumber = new FilterByRowkey().filterByPlateNumber(option, scan);
            }
        }

        return null;
    }

    /**
     * @param searchId 搜索的 id（rowkey）（刘思阳）
     * @param offset   从第几条开始
     * @param count    条数
     * @return SearchResult对象
     */
    @Override
    public SearchResult getSearchResult(String searchId, int offset, int count) {
        Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);

        SearchResult searchResult = new SearchResult();
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        List<CapturedPicture> capturedPictureCutList = new ArrayList<>();

        Get get = new Get(Bytes.toBytes(searchId));
        Result result = null;
        try {
            result = searchResTable.get(get);
            String searchImageID = Bytes.toString(result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHIMAGEID));

            byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
            Map<String, Float> searchMessageMap = new HashMap<>();
            searchMessageMap = (Map<String, Float>) ObjectUtil.byteToObject(searchMessage);
            String returnId = null;
            Float similarity = null;
            if (!searchMessageMap.isEmpty()) {
                Iterator<String> iter = searchMessageMap.keySet().iterator();
                while (iter.hasNext()) {
                    returnId = iter.next();
                    similarity = searchMessageMap.get(returnId);
                    CapturedPicture capturedPicture = new CapturedPicture();
                    capturedPicture.setId(returnId);
                    capturedPicture.setSimilarity(similarity);

                    Get get1 = new Get(Bytes.toBytes(returnId));
                    Result personResult = personTable.get(get1);

                    String description = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
                    capturedPicture.setDescription(description);
                    String ipcID = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID));
                    capturedPicture.setIpcId(ipcID);
                    String extend = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
                    Map<String, Object> mapEx = new HashMap<>();
                    mapEx.put("ex", extend);
                    capturedPicture.setExtend(mapEx);
                    byte[] smallImage = personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    capturedPicture.setSmallImage(smallImage);
                    capturedPictureList.add(capturedPicture);
                }
            }
            if (offset + count - 1 > capturedPictureList.size()) {
                capturedPictureCutList = capturedPictureList.subList(offset - 1, capturedPictureList.size());
            } else {
                capturedPictureCutList = capturedPictureList.subList(offset - 1, offset + count - 1);
            }
            if (null != capturedPictureCutList) {
                searchResult.setPictures(capturedPictureCutList);
            }
            searchResult.setSearchId(searchId);
            searchResult.setTotal(capturedPictureList.size());


        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("get data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.getSearchRes.");
        } finally {
            HBaseUtil.closTable(searchResTable);
        }
        return searchResult;
    }

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 过滤参数键值对
     */
    @Override
    public Map<String, String> getSearchFilterParams(int type) {
        return null;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowkey）
     * @param type    图片类型，人/车
     * @return DynamicObject    动态库对象
     */
    @Override
    public CapturedPicture getCaptureMessage(String imageId, int type) {
        boolean param = false;
        switch (type) {
            case 0:
                if (PictureType.PERSON.getType() == type) {
                    param = true;
                }
                break;
            case 1:
                if (PictureType.CAR.getType() == type) {
                    param = true;
                }
                break;
            case 2:
                if (PictureType.SMALL_PERSON.getType() == type) {
                    param = true;
                }
                break;
            case 3:
                if (PictureType.SMALL_CAR.getType() == type) {
                    param = true;
                }
                break;
            case 4:
                if (PictureType.BIG_PERSON.getType() == type) {
                    param = true;
                }
                break;
            case 5:
                if (PictureType.BIG_CAR.getType() == type) {
                    param = true;
                }
                break;
        }

        CapturedPicture capturedPicture = new CapturedPicture();
        if (null != imageId && param) {
            capturedPicture.setId(imageId);

            Map<String, String> map = FtpUtil.getRowKeyMessage(imageId);
            if (!map.isEmpty()) {
                String ipcID = map.get("ipcID");
                capturedPicture.setIpcId(ipcID);
                String timeStampStr = map.get("time");
                capturedPicture.setTimeStamp(Long.valueOf(timeStampStr));
            } else {
                LOG.error("map is empty,used method CapturePictureSearchServiceImpl.getCaptureMessage.");
            }

            String rowKey = imageId.substring(0, imageId.lastIndexOf("_"));
            StringBuilder bigImageRowKey = new StringBuilder();
            bigImageRowKey.append(rowKey).append("_").append("00");

            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);

            Map<String, Object> mapEx = new HashMap<>();

            switch (type) {
                case 0:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);
                        setSmallImageToCapturedPicture_person(capturedPicture, result);
                        setCapturedPicture_person(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = person.get(bigImageGet);
                        setBigImageToCapturedPicture_person(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 0");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 1:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);
                        setSmallImageToCapturedPicture_car(capturedPicture, result);
                        setCapturedPicture_car(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = car.get(bigImageGet);
                        setBigImageToCapturedPicture_car(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 1");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 2:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);

                        setSmallImageToCapturedPicture_person(capturedPicture, result);
                        setCapturedPicture_person(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 2");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 3:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);

                        setSmallImageToCapturedPicture_car(capturedPicture, result);
                        setCapturedPicture_car(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 3");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 4:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);
                        setCapturedPicture_person(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = person.get(bigImageGet);
                        setBigImageToCapturedPicture_person(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 4");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 5:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);
                        setCapturedPicture_car(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = car.get(bigImageGet);
                        setBigImageToCapturedPicture_car(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 5");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage param is empty.");
        }
        return capturedPicture;
    }

    private void setSmallImageToCapturedPicture_person(CapturedPicture capturedPicture, Result result) {
        byte[] smallImage = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
        capturedPicture.setSmallImage(smallImage);
    }

    private void setCapturedPicture_person(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
        capturedPicture.setDescription(des);

        String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
        mapEx.put("ex", ex);
        capturedPicture.setExtend(mapEx);
    }

    private void setBigImageToCapturedPicture_person(CapturedPicture capturedPicture, Result bigImageResult) {
        byte[] bigImage = bigImageResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
        capturedPicture.setBigImage(bigImage);
    }

    private void setSmallImageToCapturedPicture_car(CapturedPicture capturedPicture, Result result) {
        byte[] smallImage = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
        capturedPicture.setSmallImage(smallImage);
    }

    private void setCapturedPicture_car(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
        capturedPicture.setDescription(des);

        String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
        mapEx.put("ex", ex);
        capturedPicture.setExtend(mapEx);
    }

    private void setBigImageToCapturedPicture_car(CapturedPicture capturedPicture, Result bigImageResult) {
        byte[] bigImage = bigImageResult.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
        capturedPicture.setBigImage(bigImage);
    }
}
