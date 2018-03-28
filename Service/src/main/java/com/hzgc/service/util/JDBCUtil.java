package com.hzgc.service.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC工具类 乔凯峰（内）
 */
public class JDBCUtil {
    private static Logger LOG = Logger.getLogger(JDBCUtil.class);
    private static Properties propertie = new Properties();
//    private static JDBCUtil instance = null;
//    private static DataSource dataSource = new DruidDataSource();
//    private static Properties propertie = new Properties();

//    private JDBCUtil() {
//    }

    /*
      加载数据源配置信息
     */
    static {
        FileInputStream fis = null;
        try {
            File resourceFile = FileUtil.loadResourceFile("jdbc.properties");
            if (resourceFile != null) {
                fis = new FileInputStream(resourceFile);
                propertie.load(new FileInputStream(resourceFile));
            }
//            dataSource = DruidDataSourceFactory.createDataSource(propertie);
//            dataSource.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("get jdbc.properties failure");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取单例
     *
     * @return 返回JDBCUtil单例对象
     */
//    public static JDBCUtil getInstance() {
//        if (instance == null) {
//            synchronized (JDBCUtil.class) {
//                if (instance == null) {
//                    instance = new JDBCUtil();
//                }
//            }
//        }
//        return instance;
//    }

    /**
     * 获取数据库连接池连接
     *
     * @return 返回Connection对象
     */
    public static Connection getConnection() {
        long start = System.currentTimeMillis();
        Connection conn = null;
//        try {
//            return dataSource.getConnection();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(propertie.getProperty("url"));
        } catch (SQLException e) {
            if (e.getMessage().contains("Unable to read HiveServer2 uri from ZooKeeper")){
                LOG.error("Please start Spark JBDC Service !");
            }else {
                e.printStackTrace();
            }
        }
        LOG.info("get jdbc connection time is:" + (System.currentTimeMillis() - start));
        return conn;
    }
}
