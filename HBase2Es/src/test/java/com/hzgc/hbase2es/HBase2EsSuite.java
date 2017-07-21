package com.hzgc.hbase2es;

import com.hzgc.hbase2es.utils.HBaseHelper;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HBase2EsSuite {

    @Test
    public void test(){
//        String hosts = "s100,s200,s300";
//        String[] hostlist = hosts.split(",");
//        for (String host:hostlist){
//            System.out.println(host);
//        }
        System.out.println(new Path("hdfs:///user/ldl/hbase2es-2.0-jar-with-dependencies.jar"));
        System.out.println((char) 47);
    }

    @Test
    public void testCrateTableWithCoprocessor(){
        //es_cluster=zcits,es_type=zcestestrecord,es_index=zcestestrecord,es_port=9100,es_host=master
        // 注意，索引的命名规则，必须是小写字母的，不可以包含大写字母
        Map<String, String> mapOfOberserverArgs = new HashMap<String, String>();
        mapOfOberserverArgs.put("es_cluster","my-cluser");
        mapOfOberserverArgs.put("es_index","objectinfo");
        mapOfOberserverArgs.put("es_type","pcl");
        mapOfOberserverArgs.put("es_hosts","s100_s101_s102");
        HBaseHelper helper = new HBaseHelper();
        try {
            helper.dropTable("ObjectInfo");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Table table = helper.crateTableWithCoprocessor("ObjectInfo",
                "com.hzgc.hbase2es.HBase2EsObserver",
                "hdfs:///user/ldl/hbase2es-1.0-jar-with-dependencies.jar",
                mapOfOberserverArgs, 3, null,"pcl");
        try {
            HBaseHelper.getHBaseConnection().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(table);
    }
}
