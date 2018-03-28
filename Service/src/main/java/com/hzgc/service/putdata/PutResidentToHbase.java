package com.hzgc.service.putdata;

import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.util.HBaseHelper;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class PutResidentToHbase {
    private static Logger LOG = Logger.getLogger(PutResidentToHbase.class);
    private static ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();

    private static void getPhotoName(String jsonFile, String xlsFile, String photoPath, String pkey) {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        String feature = "";
        Table table = HBaseHelper.getTable("objectinfo");
        long startNum = getHbaseNum(table);                                                 //插入前hbase的数据总数
        try {
            File jfile = new File(jsonFile);
            if (!jfile.exists()) {
                jfile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(jfile, true);
            String path = xlsFile;
            File file = new File(path);
            if (!file.exists()) {
                LOG.error(path + "not exists!");
            }
            File file1 = new File(photoPath);
            if (!file1.exists()) {
                LOG.error(photoPath + "not exists!");
            }
            File fa[] = file1.listFiles();
            Workbook workBook = null;
            try {
                workBook = Workbook.getWorkbook(file);
            } catch (BiffException e) {
                e.printStackTrace();
            }
            assert workBook != null;
            Sheet sheet = workBook.getSheet(0);
            int bidcardNum = sheet.findCell(ObjectInfoExl.BIDCARD).getColumn();
            int bnameNum = sheet.findCell(ObjectInfoExl.BNAME).getColumn();
            int bphoneNum = sheet.findCell(ObjectInfoExl.BPHONE).getColumn();
            int bsexNum = sheet.findCell(ObjectInfoExl.BSEX).getColumn();
            int a = sheet.getRows();
            for (int i = 1; i < a; i++) {
                String bidcard = sheet.getCell(bidcardNum, i).getContents().trim();
                String bname = sheet.getCell(bnameNum, i).getContents().trim();
                String bphone = sheet.getCell(bphoneNum, i).getContents().trim();
                String bsex = sheet.getCell(bsexNum, i).getContents().trim();
                LOG.info("[idcard: " + bidcard + ", " + "name: " + bname + ", " + "phone: " + bphone + ", " + "sex: " + bsex + "]");
                Put put = new Put(Bytes.toBytes(pkey + bidcard));
                for (File fs : fa) {
                    if (fs.isDirectory()) {
                        LOG.info(fs.getName() + ",这是一个目录！！！");
                    } else {
                        String pidcard = fs.getName().split(".jpg")[0];
                        if (bidcard.equals(pidcard)) {
                            byte[] photo = ImageToByte.image2byte(fs.getAbsolutePath());
                            if (photo != null && photo.length != 0) {
                                //将信息输出到hbase中
                                feature = objectInfoHandler.getFeature("person", photo);
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), photo);
                                break;
                            }
                        } else {
                            byte[] photo = new byte[0];
                            put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), photo);
                        }
                    }
                }
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformid"), Bytes.toBytes("0001"));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("tag"), Bytes.toBytes(1));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("pkey"), Bytes.toBytes(pkey));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("idcard"), Bytes.toBytes(bidcard));
                if ("男".equals(bsex)) {
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes(1));
                } else {
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes(0));
                }
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), feature.getBytes("UTF-8"));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("name"), Bytes.toBytes(bname));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("creator"), Bytes.toBytes("bigdata"));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("cphone"), Bytes.toBytes(bphone));
                table.put(put);
                //将信息输出到json文件中
                map2.put("_index", "objectinfo");
                map2.put("_type", "person");
                map2.put("_id", pkey + bidcard);
                map1.put("create", map2);
                String newJson = MapToJson.mapToJson(map1);
                fos.write(newJson.getBytes());
                fos.write("\n".getBytes());
                map3.put("platformid", "0001");
                map3.put("tag", 1);
                map3.put("pkey", pkey);
                map3.put("name", bname);
                map3.put("idcard", bidcard);
                map3.put("feature", feature);
                if ("男".equals(bsex)) {
                    map3.put("sex", 1);
                } else {
                    map3.put("sex", 0);
                }
                map3.put("creator", "bigdata");
                map3.put("cphone", bphone);
                String newJson1 = MapToJson.mapToJson(map3);
                fos.write(newJson1.getBytes());
                fos.write("\n".getBytes());
            }
            long endNum = getHbaseNum(table);                                                  //插入后hbase中的数据总数
            LOG.info("insert into Hbase data num is " + (endNum - startNum));
            workBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long getHbaseNum(Table table) {
        Scan scan = new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());
        long rowCount = 0;
        try {
            ResultScanner resultScanner = table.getScanner(scan);
            for (Result result : resultScanner) {
                rowCount += result.size();
            }
        } catch (IOException e) {
            LOG.info(e.getMessage(), e);
        }
        return rowCount;
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            LOG.error("Wrong number of input parameters");
        }
        String photoPath = args[0];
        String jsonFile = args[1];
        String xls = args[2];
        String pkey = args[3];
        long a = System.currentTimeMillis();
        getPhotoName(jsonFile, xls, photoPath, pkey);
        LOG.info("put data to HBase consume time " + (System.currentTimeMillis() - a));
    }
}
