package cn.xpleaf.spider.utils;

import cn.xpleaf.spider.constants.SpiderConstants;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 操作http的工具类
 */
public class HttpUtil {

    // log4j日志记录，这里主要用于记录网页下载时间的信息
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    // IP地址代理库Map
    private static Map<String, Integer> IPProxyRepository = new HashMap<>();
    private static String[] keysArray = null;   // keysArray是为了方便生成随机的代理对象

    /**
     * 初次使用时使用静态代码块将IP代理库加载进set中
     */
    static {
        InputStream in = HttpUtil.class.getClassLoader().getResourceAsStream("IPProxyRepository.txt");  // 加载包含代理IP的文本
        // 构建缓冲流对象
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(isr);
        String line = null;
        try {
            // 循环读每一行，添加进map中
            while ((line = bfr.readLine()) != null) {
                String[] split = line.split(":");   // 以:作为分隔符，即文本中的数据格式应为192.168.1.1:4893
                String host = split[0];
                int port = Integer.valueOf(split[1]);
                IPProxyRepository.put(host, port);
            }
            Set<String> keys = IPProxyRepository.keySet();
            keysArray = keys.toArray(new String[keys.size()]);  // keysArray是为了方便生成随机的代理对象
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据url下载网页内容
     *
     * @param url
     * @return
     */
    public static String getHttpContent(String url) {

        CloseableHttpClient httpClient = null;
        HttpHost proxy = null;
        if (IPProxyRepository.size() > 0) {  // 如果ip代理地址库不为空，则设置代理
            proxy = getRandomProxy();
            httpClient = HttpClients.custom().setProxy(proxy).build();  // 创建httpclient对象
        } else {
            httpClient = HttpClients.custom().build();  // 创建httpclient对象
        }
        HttpGet request = new HttpGet(url); // 构建htttp get请求
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.221 Safari/537.36 SE 2.X MetaSr 1.0");
        /*
        HttpHost proxy = null;
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpGet request = new HttpGet(url); // 构建htttp get请求
        */
        /**
         * 设置超时时间
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();
        request.setConfig(requestConfig);
        String host = null;
        Integer port = null;
        if(proxy != null) {
            host = proxy.getHostName();
            port = proxy.getPort();
        }
        try {
            long start = System.currentTimeMillis();    // 开始时间
            CloseableHttpResponse response = httpClient.execute(request);
            long end = System.currentTimeMillis();      // 结束时间
            logger.info("下载网页：{}，消耗时长：{} ms，代理信息：{}", url, end - start, host + ":" + port);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            logger.error("下载网页：{}出错，代理信息：{}，", url, host + ":" + port);
            // 如果该url为列表url，则将其添加到高优先级队列中
            if (url.contains("list.jd.com") || url.contains("list.suning.com")) {   // 这里为硬编码
                String domain = SpiderUtil.getTopDomain(url);   // jd.com
                retryUrl(url, domain + SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX);    // 添加url到jd.com.higher中
            }
            /**
             * 为什么要加入到高优先级队列中？
             * 如果该url为第一个种子url，但是解析却失败了，那么url仓库中会一直没有url，虽然爬虫程序还在执行，
             * 但是会一直提示没有url，这时就没有意义了，还需要尝试的另外一个原因是，下载网页失败很大可能是
             * 因为：
             *      1.此刻网络突然阻塞
             *      2.代理地址被限制，也就是被封了
             * 所以将其重新添加到高优先级队列中，再进行解析目前来说是比较不错的解决方案
             */
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 随机返回一个代理对象
     *
     * @return
     */
    public static HttpHost getRandomProxy() {
        // 随机获取host:port，并构建代理对象
        Random random = new Random();
        String host = keysArray[random.nextInt(keysArray.length)];
        int port = IPProxyRepository.get(host);
        HttpHost proxy = new HttpHost(host, port);  // 设置http代理
        return proxy;
    }

    /**
     * 将url重新添加到高优先级队列中
     *
     * @param url
     */
    public static void retryUrl(String url, String key) {
        Jedis jedis = JedisUtil.getJedis();
        jedis.lpush(key, url);
    }

    public static void main(String[] args) {
        System.out.println(getRandomProxy());
    }
}
