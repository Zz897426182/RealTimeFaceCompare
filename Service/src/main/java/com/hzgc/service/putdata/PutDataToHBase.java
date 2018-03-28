package com.hzgc.service.putdata;

import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.util.HBaseHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PutDataToHBase {
    private static Logger LOG = Logger.getLogger(PutDataToHBase.class);
    private ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
    private static Map<String,byte[]> getPhotoName(String jsonFile, String photoPath){
        String path = photoPath;
        File f = new File(path);
        Map<String,byte[]> map = new HashMap<>();
        if (!f.exists()){
            System.out.println(path + " not exists!");
        }
        File fa[] = f.listFiles();
        for (int i = 0; i < fa.length; i ++ ){
            File fs = fa[i];
            if (fs.isDirectory()){
                System.out.println(fs.getName() + "，这是个目录！！！");
            } else {
                String a = fs.getName().split(".jpg")[0];
                try {
                    byte[] photo = ImageToByte.image2byte(fs.getAbsolutePath());
                    map.put(a,photo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
       return map;
   }

   private void putdate(String jsonFile, String photoPath){
       final BufferedMutator.ExceptionListener listener = new BufferedMutator.ExceptionListener() {
           @Override
           public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator bufferedMutator)
                   throws RetriesExhaustedWithDetailsException {
               for (int i = 0; i < e.getNumExceptions(); i++){
                   LOG.info("Failed to send put " + e.getRow(i) + ".");
               }
           }
       };
       TableName tableName = TableName.valueOf("objectinfo");
       BufferedMutatorParams params = new BufferedMutatorParams(tableName).listener(listener);
       params.writeBufferSize(1*1024*1024);
       Connection conn = HBaseHelper.getHBaseConnection();
       try {
           BufferedMutator mutator = conn.getBufferedMutator(params);
           Map<String,byte[]> map = getPhotoName(jsonFile, photoPath);
           Set<String> set = map.keySet();
           Iterator it = set.iterator();
           List<Mutation> mutations = new ArrayList<>();
           int count = 0;
           File file = new File(jsonFile);
           if (!file.exists()){
               file.createNewFile();
           }
           FileWriter fw = new FileWriter(file);
           BufferedWriter bufferedWriter = new BufferedWriter(fw);
           MapToJson mapToJson = new MapToJson();
           while (it.hasNext()) {
               Map<String,Object> map1 = new HashMap<>();
               Map<String,Object> map2 = new HashMap<>();
               Map<String,Object> map3 = new HashMap<>();

               String a = String.valueOf(it.next());
               byte[] photo = map.get(a);
               System.out.println(photo.length);
               String feature = "";
               Put put = new Put(Bytes.toBytes("0001111" + a));
               put.setDurability(Durability.ASYNC_WAL);
               if (photo.length != 0) {
                   feature = objectInfoHandler.getFeature("person", photo);
                   put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), photo);
               }
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformid"), Bytes.toBytes("0001"));
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("tag"), Bytes.toBytes("1"));
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("pkey"), Bytes.toBytes("0001111"));
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("idcard"), Bytes.toBytes(a));
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes("1"));
               if (!"".equals(feature) && feature.length() != 0){
                   put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), feature.getBytes("ISO8859-1"));
                   map3.put("feature",feature);
               }
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("creator"), Bytes.toBytes("bigdata"));
               put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("cphone"), Bytes.toBytes("18888888"));
               mutations.add(put);
               count++;
               if (count == 100){
                   count = 0;
                   mutator.mutate(mutations);
                   mutations.clear();
               }

               map2.put("_index","objectinfo");
               map2.put("_type","person");
               map2.put("_id","0001111" + a);
               map1.put("create",map2);
               String newJson = mapToJson.mapToJson(map1);
               bufferedWriter.write(newJson);
               bufferedWriter.write("\n");
               map3.put("platformid","0001");
               map3.put("tag","1");
               map3.put("pkey","0001111");
               map3.put("idcard",a);
               map3.put("sex","1");
               map3.put("creator","bigdata");
               map3.put("cphone","18888888");
               String newJson1 = mapToJson.mapToJson(map3);
               bufferedWriter.write(newJson1);
               bufferedWriter.write("\n");
           }
           if (mutations.size() > 0){
               mutator.mutate(mutations);
           }

           bufferedWriter.close();
           fw.close();
           mutator.close();
           conn.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("");
        }
        String jsonFile = args[1];
        String photoPath = args[0];
        long a = System.currentTimeMillis();
        PutDataToHBase putDataToHBase = new PutDataToHBase();
        putDataToHBase.putdate(jsonFile, photoPath);
        System.out.println(System.currentTimeMillis() - a);
    }
}
