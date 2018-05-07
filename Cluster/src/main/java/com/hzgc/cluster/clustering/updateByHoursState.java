package com.hzgc.cluster.clustering;

import com.hzgc.cluster.util.PropertiesUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class updateByHoursState {

    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Properties properties = PropertiesUtils.getProperties();
        Long capture_data_time = Long.valueOf(properties.getProperty("job.clustering.capture.time"));
        String capture_data_table = properties.getProperty("job.clustering.capture.data");
        String capture_data_feedback = properties.getProperty("job.clustering.capture.feedback");
        String capture_data_feedback_url = properties.getProperty("job.clustering.capture.database.url");
        String jdbc_name = "com.mysql.jdbc.Driver";
        String user = properties.getProperty("job.clustering.capture.database.user");
        String password = properties.getProperty("job.clustering.capture.database.password");

        try {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = dateFormat.format(now);
            long nowtime = dateFormat.parse(date).getTime();
            long timebydate = capture_data_time * 60 * 60 * 1000;
            long datebytime = nowtime - timebydate;
            String date2 = dateFormat.format(datebytime);
            Class.forName(jdbc_name);
            Connection conn = DriverManager.getConnection(capture_data_feedback_url, user, password);
            String sql1 = "select id,identity_code,identity_type,i_flag,data_id,update_time from " + capture_data_feedback + " where update_time >= " + "\'" + date2 + "\'" + " and update_time <= " + "\'" + date + "\'";
            PreparedStatement pst1 = conn.prepareStatement(sql1);
            ResultSet rs1 = pst1.executeQuery();
            UpdateToHBase updateToHBase = new UpdateToHBase();
            System.out.println(rs1.toString()+"1111111111111111111");
            while (rs1.next()) {
                String dataId = rs1.getString("data_id");
                System.out.println(dataId + "_+_+_+_+_+_");
                String sql2 = "select small_picture from " + capture_data_table + " where id = " + "\'" + dataId + "\'";
                System.out.println(sql2 +"666666666666666666666");
                PreparedStatement pst2 = conn.prepareStatement(sql2);
                ResultSet rs2 = pst2.executeQuery();
                byte[] smallPicture = null;
                while (rs2.next()){
                    smallPicture = BlobToBytes.blobToBytes(rs2.getBlob("small_picture"));
                }
                rs2.close();
                Map<String, Object> map = new HashMap<>();
                map.put("photo", smallPicture);
                map.put("id", rs1.getString("id"));
                map.put("idcard", rs1.getString("identity_code"));
                map.put("status", rs1.getString("identity_type"));
                map.put("pkey", rs1.getString("i_flag"));
                map.put("platformid", rs1.getString("data_id"));
                map.put("updatatime", rs1.getString("update_time"));
                updateToHBase.addObjectInfoToHbase(map);
            }
            rs1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
