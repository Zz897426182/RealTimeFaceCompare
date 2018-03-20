package com.hzgc.service.putdata;

import com.hzgc.jni.FaceFunction;
import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-11-13.
 */
public class PutCriminalToHbase {
    private static Logger LOG = Logger.getLogger(PutDataToHBase.class);
    private static ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();

    public static void getPhotoName(String jsonFile, String photoPath) {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        String path = photoPath;
        File f = new File(path);
        String feature = "";
        if (!f.exists()) {
            System.out.println(path + " not exists!");
        }
        File fa[] = f.listFiles();
        Table table = HBaseHelper.getTable("objectinfo");
        try {
            File file = new File(jsonFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            MapToJson mapToJson = new MapToJson();
            FileOutputStream fos = new FileOutputStream(file, true);
            for (int i = 0; i < fa.length; i++) {
                File fs = fa[i];
                if (fs.isDirectory()) {
                    LOG.info(fs.getName() + "，这是个目录！！！");
                } else {
                    String id = fs.getName().split(".jpg")[0];
                    String tag = fs.getName().split(" ")[1];
                    Put put = new Put(Bytes.toBytes("0001111" + id));
                    try {
                        byte[] photo = ImageToByte.image2byte(fs.getAbsolutePath());
                        if (photo.length != 0) {
                            feature = FaceFunction.floatArray2string(FaceFunction.featureExtract(photo).getFeature());
                            put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), photo);
                        }
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformid"), Bytes.toBytes("0001"));
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("tag"), Bytes.toBytes(tag));
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("pkey"), Bytes.toBytes("0001111"));
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("idcard"), Bytes.toBytes(id));
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes("1"));
                        if (!"".equals(feature) && feature.length() != 0) {
                            put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), feature.getBytes("ISO8859-1"));
                            map3.put("feature", feature);
                        }
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("creator"), Bytes.toBytes("bigdata"));
                        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("cphone"), Bytes.toBytes("18888887"));
                        table.put(put);

                        map2.put("_index", "objectinfo");
                        map2.put("_type", "person");
                        map2.put("_id", "0001111" + id);
                        map1.put("create", map2);
                        String newJson = mapToJson.mapToJson(map1);
                        fos.write(newJson.getBytes());
                        fos.write("\n".getBytes());
                        map3.put("platformid", "0001");
                        map3.put("tag", tag);
                        map3.put("pkey", "0001111");
                        map3.put("idcard", id);
                        map3.put("sex", "1");
                        map3.put("creator", "bigdata");
                        map3.put("cphone", "18888887");
                        String newJson1 = mapToJson.mapToJson(map3);
                        System.out.println(newJson1);
                        fos.write(newJson1.getBytes());
                        fos.write("\n".getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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
