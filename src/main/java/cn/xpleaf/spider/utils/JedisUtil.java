package cn.xpleaf.spider.utils;

import cn.xpleaf.spider.constants.JedisConstants;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Properties;

/**
 * Redis Java API 操作的工具类
 * 主要为我们提供 Java操作Redis的对象Jedis 模仿类似的数据库连接池
 *
 * JedisPool
 */
public class JedisUtil {


    private JedisUtil() {}
    private static JedisPool jedisPool;
    static {
        Properties prop = new Properties();
        try {
            prop.load(JedisUtil.class.getClassLoader().getResourceAsStream("redis.properties"));
            JedisPoolConfig poolConfig = new JedisPoolConfig();

            //jedis连接池中最大的连接个数
            poolConfig.setMaxTotal(Integer.valueOf(prop.getProperty(JedisConstants.JEDIS_MAX_TOTAL)));
            //jedis连接池中最大的空闲连接个数
            poolConfig.setMaxIdle(Integer.valueOf(prop.getProperty(JedisConstants.JEDIS_MAX_IDLE)));
            //jedis连接池中最小的空闲连接个数
            poolConfig.setMinIdle(Integer.valueOf(prop.getProperty(JedisConstants.JEDIS_MIN_IDLE)));
            //jedis连接池最大的等待连接时间 ms值
            poolConfig.setMaxWaitMillis(Long.valueOf(prop.getProperty(JedisConstants.JEDIS_MAX_WAIT_MILLIS)));

            //表示jedis的服务器主机名
            String host = prop.getProperty(JedisConstants.JEDIS_HOST);
            String JEDIS_PORT = "jedis.port";
            int port = Integer.valueOf(prop.getProperty(JedisConstants.JEDIS_PORT));
            //表示jedis的服务密码
            String password = prop.getProperty(JedisConstants.JEDIS_PASSWORD);

//            jedisPool = new JedisPool(poolConfig, host, port, 10000, password);
            jedisPool = new JedisPool(poolConfig, host, port, 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提供了Jedis的对象
     * @return
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 资源释放
     * @param jedis
     */
    public static void returnJedis(Jedis jedis) {
        jedis.close();
    }
}
