package com.hzgc.hbase.putdata;

import com.hzgc.hbase.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.hbase.util.HBaseHelper;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class putResidentToHbase {
    private static Logger LOG = Logger.getLogger(putResidentToHbase.class);
    private static ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();

    private static void getPhotoName(String jsonFile, String photoPath) {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        String feature = "";
        Table table = HBaseHelper.getTable("objectinfo");
        try {
            File jfile = new File(jsonFile);
            if (!jfile.exists()) {
                jfile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(jfile, true);
            String path = "/opt/back.xls";
            File file = new File(path);
            if (!file.exists()) {
                LOG.error(path + "not exists!");
            }
            File file1 = new File(photoPath);
            if (!file1.exists()) {
                LOG.error(photoPath + "not exists!");
            }
            File fa[] = file1.listFiles();
            Workbook workbook = null;
            try {
                workbook = Workbook.getWorkbook(file);
            } catch (BiffException e) {
                e.printStackTrace();
            }
            assert workbook != null;
            Sheet sheet = workbook.getSheet(0);
            int a = sheet.getRows();
            for (int i = 1; i < a; i++) {
                Cell cell = sheet.getCell(12, i);
                Cell cell1 = sheet.getCell(3, i);
                Cell cell2 = sheet.getCell(4, i);
                Cell cell3 = sheet.getCell(7, i);
                String bidcard = cell.getContents();
                String bname = cell1.getContents();
                String bphone = cell2.getContents();
                String bsex = cell3.getContents();
                System.out.println(bidcard + "====" + bname + "====" + bphone + "====" + bsex);
                for (File fs : fa) {
                    if (fs.isDirectory()) {
                        LOG.info(fs.getName() + ",这是一个目录！！！");
                    } else {
                        String pidcard = fs.getName().split(".jpg")[0];
                        if (bidcard.equals(pidcard)) {
                            Put put = new Put(Bytes.toBytes("0001014" + bidcard));
                            byte[] photo = ImageToByte.image2byte(fs.getAbsolutePath());
                            if (photo != null && photo.length != 0) {
                                //将信息输出到hbase中
                                feature = objectInfoHandler.getFeature("person", photo);
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), photo);
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformid"), Bytes.toBytes("0001"));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("tag"), Bytes.toBytes(1));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("pkey"), Bytes.toBytes("0001014"));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("idcard"), Bytes.toBytes(bidcard));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes(bsex));
                                if (!"".equals(feature) && feature.length() != 0) {
                                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), feature.getBytes("ISO8859-1"));
                                    map3.put("feature", feature);
                                }
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("name"), Bytes.toBytes(bname));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("creator"), Bytes.toBytes("bigdata"));
                                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("cphone"), Bytes.toBytes(bphone));
                                table.put(put);
                                //将信息输出到json文件中
                                map2.put("_index", "objectinfo");
                                map2.put("_type", "person");
                                map2.put("_id", "0001014" + bidcard);
                                map1.put("create", map2);
                                String newJson = MapToJson.mapToJson(map1);
                                fos.write(newJson.getBytes());
                                fos.write("\n".getBytes());
                                map3.put("platformid", "0001");
                                map3.put("tag", 1);
                                map3.put("pkey", "0001014");
                                map3.put("name", bname);
                                map3.put("idcard", bidcard);
                                map3.put("sex", bsex);
                                map3.put("creator", "bigdata");
                                map3.put("cphone", bphone);
                                String newJson1 = MapToJson.mapToJson(map3);
                                System.out.println(newJson1);
                                fos.write(newJson1.getBytes());
                                fos.write("\n".getBytes());
                            }
                        }
                    }
                }
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("");
        }
        String jsonFile = args[1];
        String photoPath = args[0];
        long a = System.currentTimeMillis();
        getPhotoName(jsonFile, photoPath);
        System.out.println(System.currentTimeMillis() - a);
    }
}
