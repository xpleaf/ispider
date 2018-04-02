package cn.xpleaf.spider.core.repository.impl;

import cn.xpleaf.spider.constants.SpiderConstants;
import cn.xpleaf.spider.core.repository.IRepository;
import cn.xpleaf.spider.utils.JedisUtil;
import cn.xpleaf.spider.utils.SpiderUtil;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * 基于Redis的全网爬虫，随机获取爬虫url：
 *
 * Redis中用来保存url的数据结构如下：
 * 1.需要爬取的域名集合（存储数据类型为set，这个需要先在Redis中添加）
 *      key
 *          spider.website.domains
 *      value(set)
 *          jd.com  suning.com  gome.com
 *      key由常量对象SpiderConstants.SPIDER_WEBSITE_DOMAINS_KEY 获得
 * 2.各个域名所对应的高低优先url队列（存储数据类型为list，这个由爬虫程序解析种子url后动态添加）
 *      key
 *          jd.com.higher
 *          jd.com.lower
 *          suning.com.higher
 *          suning.com.lower
 *          gome.com.higher
 *          gome.come.lower
 *      value(list)
 *          相对应需要解析的url列表
 *      key由随机的域名 + 常量 SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX或者SpiderConstants.SPIDER_DOMAIN_LOWER_SUFFIX获得
 * 3.种子url列表
 *      key
 *          spider.seed.urls
 *      value(list)
 *          需要爬取的数据的种子url
 *       key由常量SpiderConstants.SPIDER_SEED_URLS_KEY获得
 *
 *       种子url列表中的url会由url调度器定时向高低优先url队列中
 */
public class RandomRedisRepositoryImpl implements IRepository {

    /**
     * 构造方法
     */
    public RandomRedisRepositoryImpl() {
        init();
    }

    /**
     * 初始化方法，初始化时，先将redis中存在的高低优先级url队列全部删除
     * 否则上一次url队列中的url没有消耗完时，再停止启动跑下一次，就会导致url仓库中有重复的url
     */
    public void init() {
        Jedis jedis = JedisUtil.getJedis();
        Set<String> domains = jedis.smembers(SpiderConstants.SPIDER_WEBSITE_DOMAINS_KEY);
        String higherUrlKey;
        String lowerUrlKey;
        for(String domain : domains) {
            higherUrlKey = domain + SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX;
            lowerUrlKey = domain + SpiderConstants.SPIDER_DOMAIN_LOWER_SUFFIX;
            jedis.del(higherUrlKey, lowerUrlKey);
        }
        JedisUtil.returnJedis(jedis);
    }

    /**
     * 从队列中获取url，目前的策略是：
     *      1.先从高优先级url队列中获取
     *      2.再从低优先级url队列中获取
     *  对应我们的实际场景，应该是先解析完列表url再解析商品url
     *  但是需要注意的是，在分布式多线程的环境下，肯定是不能完全保证的，因为在某个时刻高优先级url队列中
     *  的url消耗完了，但实际上程序还在解析下一个高优先级url，此时，其它线程去获取高优先级队列url肯定获取不到
     *  这时就会去获取低优先级队列中的url，在实际考虑分析时，这点尤其需要注意
     * @return
     */
    @Override
    public String poll() {
        // 从set中随机获取一个顶级域名
        Jedis jedis = JedisUtil.getJedis();
        String randomDomain = jedis.srandmember(SpiderConstants.SPIDER_WEBSITE_DOMAINS_KEY);    // jd.com
        String key = randomDomain + SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX;                // jd.com.higher
        String url = jedis.lpop(key);
        if(url == null) {   // 如果为null，则从低优先级中获取
            key = randomDomain + SpiderConstants.SPIDER_DOMAIN_LOWER_SUFFIX;    // jd.com.lower
            url = jedis.lpop(key);
        }
        JedisUtil.returnJedis(jedis);
        return url;
    }

    /**
     * 向高优先级url队列中添加url
     * @param highUrl
     */
    @Override
    public void offerHigher(String highUrl) {
        offerUrl(highUrl, SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX);
    }

    /**
     * 向低优先url队列中添加url
     * @param lowUrl
     */
    @Override
    public void offerLower(String lowUrl) {
        offerUrl(lowUrl, SpiderConstants.SPIDER_DOMAIN_LOWER_SUFFIX);
    }

    /**
     * 添加url的通用方法，通过offerHigher和offerLower抽象而来
     * @param url   需要添加的url
     * @param urlTypeSuffix  url类型后缀.higher或.lower
     */
    public void offerUrl(String url, String urlTypeSuffix) {
        Jedis jedis = JedisUtil.getJedis();
        String domain = SpiderUtil.getTopDomain(url);   // 获取url对应的顶级域名，如jd.com
        String key = domain + urlTypeSuffix;            // 拼接url队列的key，如jd.com.higher
        jedis.lpush(key, url);                          // 向url队列中添加url
        JedisUtil.returnJedis(jedis);
    }
}
