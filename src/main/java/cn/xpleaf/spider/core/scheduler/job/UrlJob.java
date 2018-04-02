package cn.xpleaf.spider.core.scheduler.job;

import cn.xpleaf.spider.constants.SpiderConstants;
import cn.xpleaf.spider.utils.JedisUtil;
import cn.xpleaf.spider.utils.SpiderUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * 每天定时从url仓库中获取种子url，添加进高优先级列表
 */
public class UrlJob implements Job {

    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(UrlJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        /**
         * 1.从指定url种子仓库获取种子url
         * 2.将种子url添加进高优先级列表
         */
        Jedis jedis = JedisUtil.getJedis();
        Set<String> seedUrls = jedis.smembers(SpiderConstants.SPIDER_SEED_URLS_KEY);  // spider.seed.urls Redis数据类型为set，防止重复添加种子url
        for(String seedUrl : seedUrls) {
            String domain = SpiderUtil.getTopDomain(seedUrl);   // 种子url的顶级域名
            jedis.sadd(domain + SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX, seedUrl);
            logger.info("获取种子:{}", seedUrl);
        }
        JedisUtil.returnJedis(jedis);
//        System.out.println("Scheduler Job Test...");
    }

}
