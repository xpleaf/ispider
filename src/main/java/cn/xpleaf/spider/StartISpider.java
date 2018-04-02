package cn.xpleaf.spider;

import cn.xpleaf.spider.core.download.impl.HttpGetDownloadImpl;
import cn.xpleaf.spider.core.parser.Impl.JDHtmlParserImpl;
import cn.xpleaf.spider.core.parser.Impl.SNHtmlParserImpl;
import cn.xpleaf.spider.core.repository.impl.RandomRedisRepositoryImpl;
import cn.xpleaf.spider.core.store.impl.ConsoleStore;
import cn.xpleaf.spider.core.store.impl.HBaseStoreImpl;
import cn.xpleaf.spider.core.store.impl.MySQLStoreImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动爬虫的类
 */
public class StartISpider {
    public static void main(String[] args) {
        ISpider iSpider = new ISpider();
        // 1.注入下载器
        iSpider.setDownload(new HttpGetDownloadImpl());

        // 2.注入解析器
        // 2.1 添加需要爬取的域名列表
        List<String> domains = new ArrayList<>();
        domains.add("jd.com");
        domains.add("suning.com");
        iSpider.setDomains(domains);
        // 2.2 注入解析器
        iSpider.setParsers("jd.com", new JDHtmlParserImpl());
        iSpider.setParsers("suning.com", new SNHtmlParserImpl());
        // 2.2 设置高低优先级url标识器
        Map<String, String> jdMarker = new HashMap<>();
        jdMarker.put("higher", "https://list.jd.com/");
        jdMarker.put("lower", "https://item.jd.com");
        Map<String, String> snMarker = new HashMap<>();
        snMarker.put("higher", "https://list.suning.com");
        snMarker.put("lower", "https://product.suning.com");
        iSpider.setUrlLevelMarker("jd.com", jdMarker);
        iSpider.setUrlLevelMarker("suning.com", snMarker);

        // 3.注入存储器
        iSpider.setStore(new ConsoleStore());

        // 4.设置种子url
        iSpider.setSeedUrls("https://list.jd.com/list.html?cat=9987,653,655&page=1");
        iSpider.setSeedUrls("https://list.suning.com/0-20006-0.html");

        // 5.设置url仓库
        iSpider.setRepository(new RandomRedisRepositoryImpl()); // 设置url仓库

        // 6.启动爬虫
        iSpider.start();
//        iSpider.startSingle();
    }
}

