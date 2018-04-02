package cn.xpleaf.spider.constants;

public interface SpiderConstants {

    // url种子仓库的key，redis中存储数据类型为set，防止重复添加种子url
    public String SPIDER_SEED_URLS_KEY = "spider.seed.urls";

    // 获取的网站顶级域名集合，redis中存储数据类型为set
    public String SPIDER_WEBSITE_DOMAINS_KEY = "spider.website.domains";

    // 获取网站高优先级url的后缀，redis中存储数据类型为list
    String SPIDER_DOMAIN_HIGHER_SUFFIX = ".higher";

    // 获取网站低优先级url的后缀，redis中存储数据类型为list
    String SPIDER_DOMAIN_LOWER_SUFFIX = ".lower";
}
