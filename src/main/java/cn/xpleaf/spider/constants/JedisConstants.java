package cn.xpleaf.spider.constants;

/**
 * 专门用于存放Jedis的常量类
 */
public interface JedisConstants {

    // 表示jedis的服务器主机名
    String JEDIS_HOST = "jedis.host";
    // 表示jedis的服务的端口
    String JEDIS_PORT = "jedis.port";
    // 表示jedis的服务密码
    String JEDIS_PASSWORD = "jedis.password";

    // jedis连接池中最大的连接个数
    String JEDIS_MAX_TOTAL = "jedis.max.total";
    // jedis连接池中最大的空闲连接个数
    String JEDIS_MAX_IDLE = "jedis.max.idle";
    // jedis连接池中最小的空闲连接个数
    String JEDIS_MIN_IDLE = "jedis.min.idle";

    // jedis连接池最大的等待连接时间 ms值
    String JEDIS_MAX_WAIT_MILLIS = "jedis.max.wait.millis";

}
