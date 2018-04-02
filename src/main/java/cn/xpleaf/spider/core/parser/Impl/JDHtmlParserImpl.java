package cn.xpleaf.spider.core.parser.Impl;

import cn.xpleaf.spider.constants.SpiderConstants;
import cn.xpleaf.spider.core.parser.IParser;
import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.utils.HtmlUtil;
import cn.xpleaf.spider.utils.HttpUtil;
import cn.xpleaf.spider.utils.SpiderUtil;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解析京东商品的实现类
 */
public class JDHtmlParserImpl implements IParser {

    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(JDHtmlParserImpl.class);

    @Override
    public void parser(Page page) {
        HtmlCleaner cleaner = new HtmlCleaner();
        /**
         * cleaner.clean()方法，如果page.getContent为null，那么整个程序就会一直阻塞在这里
         * 所以，在前面的代码中ISpider.start()方法，下载网页后，需要对内容进行判断，如果content为空，则跳过解析
         */
        TagNode rootNode = cleaner.clean(page.getContent());

        long start = System.currentTimeMillis();    // 解析开始时间
        // 进行判断 根据url的类型进行列表解析还是商品解析
        if (page.getUrl().startsWith("https://item.jd.com/")) {  // 解析商品
            parserProduct(page, rootNode);
            logger.info("解析商品页面:{}, 消耗时长:{}ms", page.getUrl(), System.currentTimeMillis() - start);
        } else if (page.getUrl().startsWith("https://list.jd.com/list.html")) {  // 解析列表
            // 当前页面的商品url列表
            List<String> urls = HtmlUtil.getListUrlByXpath(rootNode, "href", "//div[@id='plist']/ul/li/div/div[1]/a");
            // 下一页 获取下一页的url
            String nextUrl = HtmlUtil.getAttrByXpath(rootNode, "href", "//div[@id='J_topPage']/a[2]");
            if (!"javascript:;".equals(nextUrl)) {    // 说明已经到最后一页了，再不能往下解析了，把当前的url进行排除
                nextUrl = "https://list.jd.com" + nextUrl;
                urls.add(nextUrl);
            }
            page.getUrls().addAll(urls);
            /**
             * 需要注意的是，当解析的是列表url时，该分支的代码只会解析当前页面的url，而不会爬取数据
             * url解析完成以后，添加到当前Page对象中的urls列表中，解析结束后，urls会被添加到Spider对象的url仓库中（高优先级队列）
             * 这样来让交给循环继续做解析，直到高把优先级队列的url都解析完成了，后面才会去解析低优先级也就是商品url的数据
             * 也就是说，当走的是解析列表的分支代码时，这时的Page对象的作用就变成了用来保存url的一个暂时的容器了
             */
            logger.info("解析列表页面:{}, 消耗时长:{}ms", page.getUrl(), System.currentTimeMillis() - start);
            if(System.currentTimeMillis() - start == 0) {   // 解析京东数据页码数时，偶尔获取不到下一页，时间就为0ms，这时需要重试
                logger.info("解析列表页面:{}, 消耗时长:{}ms， 尝试将其重新添加到高优先级url队列中", page.getUrl(), System.currentTimeMillis() - start);
                HttpUtil.retryUrl(page.getUrl(), SpiderUtil.getTopDomain(page.getUrl()) + SpiderConstants.SPIDER_DOMAIN_HIGHER_SUFFIX);
            }
        }

    }

    private void parserProduct(Page page, TagNode tagNode) {
        // 1.id; 商品id
        String id = HtmlUtil.getIdByUrl(page.getUrl());
        page.setId(id);

        // 2.source; 商品来源
        String domain = SpiderUtil.getTopDomain(page.getUrl());
        page.setSource(domain);

        // 3.title; 商品标题
        String title = HtmlUtil.getTextByXpath(tagNode, "//div[@class='sku-name']");
        page.setTitle(title);

        // 4.price; 商品价格
        String priceUrl = "https://p.3.cn/prices/mgets?pduid=1504781656858214892980&skuIds=J_" + id;
        /**
         * 上面的价格url中，pduid每隔一段时间都会改变，所以需要定时更新一下，特别是价格无法获取到时则都是这个问题
         * 下面就来解决这个问题吧，如果获取不到价格，会返回jsonp字符串：{"error":"pdos_captcha"}1504781656858214892980
         * 当出现这个情况时，就提示更换pduid
         */
        String priceJson = HttpUtil.getHttpContent(priceUrl);
        if (priceJson != null) {
            // 解析json [{"op":"4899.00","m":"9999.00","id":"J_3133843","p":"4799.00"}] 将该json字符串封装成json对象
            if (priceJson.contains("error")) {   // 返回{"error":"pdos_captcha"}，说明价格url已经不可用，更换pduid再做解析
                logger.info("价格url已经不可用，请及时更换pduid--->" + priceJson);
            } else {
                JSONArray priceJsonArray = new JSONArray(priceJson);
                JSONObject priceJsonObj = priceJsonArray.getJSONObject(0);
                String priceStr = priceJsonObj.getString("p").trim();
                Float price = Float.valueOf(priceStr);
                page.setPrice(price);
            }
        }

        // 5.imgUrl; 商品图片链接
        String imgUrl = HtmlUtil.getAttrByXpath(tagNode, "data-origin", "//img[@id='spec-img']");
        page.setImgUrl("http:" + imgUrl);

        // 6.params; 商品规格参数
        // Map<String, Map<String, String>>
        // {"主体": {"品牌": Apple}, "型号": "IPhone 7 Plus", "基本信息":{"机身颜色":"玫瑰金"}}
        JSONObject paramObj = HtmlUtil.getParams(tagNode, "//*[@id=\"detail\"]/div[2]/div[2]/div[1]/*", "//h3", "//dl");
        if (paramObj.has("主体")) {
            if (paramObj.getJSONObject("主体").has("品牌")) {
                String brand = paramObj.getJSONObject("主体").getString("品牌");
                page.setBrand(brand);
            }
        }
        page.setParams(paramObj.toString());

        // 7.商品评论数
        // 注意JsonObj和JsonArray的不同获取方法
        String commentCountUrl = "https://club.jd.com/comment/productCommentSummaries.action?referenceIds=" + id;
        String commentCountJson = HttpUtil.getHttpContent(commentCountUrl);
        if (commentCountJson != null) {
            JSONArray commentCountJsonArray = new JSONObject(commentCountJson).getJSONArray("CommentsCount");
            JSONObject commentCountJsonObj = commentCountJsonArray.getJSONObject(0);
            int commentCount = commentCountJsonObj.getInt("CommentCount");
            page.setCommentCount(commentCount);
        }
    }
}
