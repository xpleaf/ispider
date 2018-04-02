package cn.xpleaf.spider.utils;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * dbcp工具类
 */
public class DBCPUtil {
    private static DataSource ds;

    static {
        try {
            InputStream in = DBCPUtil.class.getClassLoader().getResourceAsStream("dbcp-config.properties");
            Properties props = new Properties();
            props.load(in);
            ds = BasicDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static DataSource getDataSource() {
        return ds;
    }
    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
