package com.hzgc.service.staticrepo;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PhoenixJDBCHelper {
    private Logger LOG = Logger.getLogger(PhoenixJDBCHelper.class);
    private static Connection conn;

    private PhoenixJDBCHelper() {}
    public static Connection getPhoenixJdbcConn() {
        if (conn == null) {
            initConnection();
        }
        return conn;
    }


    private static void initConnection() {
        File file = FileUtil.loadResourceFile("jdbc.properties");
        Properties jdbcProp = new Properties();
        try {
            jdbcProp.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String phoenixJDBCURL = jdbcProp.getProperty("phoenixJDBCURL");
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            conn = DriverManager.getConnection(phoenixJDBCURL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
