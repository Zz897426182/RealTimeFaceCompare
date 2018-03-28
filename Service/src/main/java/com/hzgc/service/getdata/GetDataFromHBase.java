package com.hzgc.service.getdata;

import com.hzgc.service.putdata.MapToJson;
import com.hzgc.service.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;


public class GetDataFromHBase {
    private static Logger LOG = Logger.getLogger(GetDataFromHBase.class);

    private static void getJson(String jsonFile, String photoFilePath) {
        Table table = HBaseHelper.getTable("objectinfo");
        Scan scan = new Scan();
        try {
            ResultScanner resultScanner = table.getScanner(scan);
            File file1 = new File(jsonFile);
            if (!file1.exists()) {
                file1.createNewFile();
            }
            String fileName = new File(photoFilePath).getName();
            File file2 = new File(fileName);
            if (!(file2.exists() && file2.isDirectory())) {
                file2.mkdirs();
            }
            FileOutputStream fos1 = new FileOutputStream(file1);
            Map<String, Object> map1 = new HashMap<>();
            Map<String, Object> map2 = new HashMap<>();
            Map<String, Object> map3 = new HashMap<>();
            for (Result result : resultScanner) {
                String rowkey = Bytes.toString(result.getRow());
                String platformid = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("platformid")));
                String tag = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("tag")));
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("pkey")));
                String idcard = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("idcard")));
                String sex = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("sex")));
                String feature = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("feature")));
                String creator = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("creator")));
                String cphone = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("cphone")));
                String name = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("name")));
                //输出图片
                byte[] photo = result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
                if (photo != null && photo.length != 0) {
                    ByteArrayInputStream byteArrayPhoto = new ByteArrayInputStream(photo);
                    BufferedImage bufferedImage = ImageIO.read(byteArrayPhoto);
                    String photoFile = file2 + "//" + idcard + ".jpg";
                    File file = new File(photoFile);
                    ImageIO.write(bufferedImage, "jpg", file);
                } else {
                    LOG.warn("get: " + name + " photo from HBase failure");
                }
                //写入json文件，用来导ES
                map2.put("_index", "objectinfo");
                map2.put("_type", "person");
                map2.put("_id", rowkey);
                map1.put("create", map2);
                String newJson1 = MapToJson.mapToJson(map1);
                fos1.write(newJson1.getBytes());
                fos1.write("\n".getBytes());
                map3.put("platformid", platformid);
                map3.put("feature", feature);
                map3.put("name", name);
                map3.put("tag", tag);
                map3.put("pkey", pkey);
                map3.put("idcard", idcard);
                map3.put("sex", sex);
                map3.put("creator", creator);
                map3.put("cphone", cphone);
                String newJson2 = MapToJson.mapToJson(map3);
                fos1.write(newJson2.getBytes());
                fos1.write("\n".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            LOG.error("Wrong number of input parameters");
        }
        String jsonFile = args[0];
        String photoFile = args[1];
        long a = System.currentTimeMillis();
        getJson(jsonFile, photoFile);
        LOG.info("get data from HBase time is :" + (System.currentTimeMillis() - a));
    }
}
