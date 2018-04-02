package cn.xpleaf.spider.core.parser.Impl;

import cn.xpleaf.spider.core.download.IDownload;
import cn.xpleaf.spider.core.download.impl.HttpGetDownloadImpl;
import cn.xpleaf.spider.core.parser.IParser;
import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.utils.HtmlUtil;
import cn.xpleaf.spider.utils.HttpUtil;
import cn.xpleaf.spider.utils.SpiderUtil;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 苏宁易购网页解析
 */
public class SNHtmlParserImpl implements IParser {

    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(SNHtmlParserImpl.class);

    // 是否已经获取完所有的列表页面
    private boolean ifGetAll = false;

    /**
     * 苏宁的下一页按钮的url似乎也是动态加载的，所以没有办法像京东一样获取
     */

    @Override
    public void parser(Page page) {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode rootNode = cleaner.clean(page.getContent());
        long start = System.currentTimeMillis();    // 解析开始时间

        if (page.getUrl().startsWith("https://product.suning.com")) {    // 解析商品
            parserProduct(page, rootNode);
            logger.info("解析商品页面:{}, 消耗时长:{}ms", page.getUrl(), System.currentTimeMillis() - start);
        } else if (page.getUrl().startsWith("https://list.suning.com")) {    // 解析列表
            // 当前页面的商品url列表
            List<String> urls = HtmlUtil.getListUrlByXpath(rootNode, "href", "//div[@id='filter-results']/ul/li/div/div/div/div[1]/div[1]/a");
            page.getUrls().addAll(urls);
            // 获取所有的列表页面url
            if (!ifGetAll) {
                Integer totalPage = null;
                try {
                    // 获取总页码数
                    Object[] objects = rootNode.evaluateXPath("//div[@id='second-filter']/div[2]/div/span");
                    TagNode tagNode = (TagNode) objects[0];
                    String text = tagNode.getText().toString(); // "\n\n1\n/100\n"
                    Pattern pattern = Pattern.compile("[0-9]{2,3}");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        totalPage = Integer.valueOf(matcher.group()); // 获得页码总数
                    }
                } catch (XPatherException e) {
                    e.printStackTrace();
                }
                if (totalPage != null) {
                    // 从url中获取当前页码
                    String currentPageStr = page.getUrl().split("0-20006-")[1].split("\\.")[0];    // url: https://list.suning.com/0-20006-0.html
                    int currentPage = Integer.valueOf(currentPageStr);
                    for (int i = currentPage + 1; i < totalPage; i++) {
                        String url = "https://list.suning.com/0-20006-" + i + ".html";
                        page.getUrls().add(url);
                    }
                }
                ifGetAll = true;    // 解析完列表后记得设置为true
            }
            logger.info("解析列表页面:{}, 消耗时长:{}ms", page.getUrl(), System.currentTimeMillis() - start);
        }
    }

    /**
     * 解析商品
     */
    private void parserProduct(Page page, TagNode tagNode) {
        // 1.id; 商品id
        String id = HtmlUtil.getIdByUrl(page.getUrl()); // url--->https://product.suning.com/0070156122/655027353.html
        page.setId(id);

        // 1.1 第二id，获取第二id只是为了获取价格和评论数
        String url = page.getUrl();
        String subUrl = url.substring(url.indexOf(".") + 1, url.length());  // subUrl--->suning.com/0070156122/655027353.html
        String sid = subUrl.substring(subUrl.indexOf("/") + 1, subUrl.lastIndexOf("/"));

        // 2.source; 商品来源
        String domain = SpiderUtil.getTopDomain(page.getUrl());
        page.setSource(domain);

        // 3.title; 商品标题
        String title = HtmlUtil.getTextByXpath(tagNode, "//h1[@id='itemDisplayName']");
        page.setTitle(title);

        // 4.price; 商品价格
        String priceUrl = "https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000" +
                id + "_020_0200101_" + sid + "_1_getClusterPrice.vhtm?callback=getClusterPrice";
        String beforePriceJson = HttpUtil.getHttpContent(priceUrl); // 获得的数据为getClusterPrice(json数据);
        try {
            String priceJson = beforePriceJson.substring(beforePriceJson.indexOf("(") + 1, beforePriceJson.lastIndexOf(")"));   // 处理为json字符串
            // 开始解析json数据--->[{}]
            JSONArray priceJsonArray = new JSONArray(priceJson);
            JSONObject priceJsonObj = priceJsonArray.getJSONObject(0);
            String priceStr = priceJsonObj.getString("price").trim();
            if (priceStr != null) { // 如果不为null，才进行转换，否则会有异常
                Float price = Float.valueOf(priceStr);
                page.setPrice(price);
            }
        } catch (Exception e) {
            logger.error("获取商品价格错误，商品url为: {}", page.getUrl());
        }

        // 5.imgUrl; 商品图片链接
        String imgUrl = HtmlUtil.getAttrByXpath(tagNode, "src", "//a[@id='bigImg']/img");
        page.setImgUrl("http:" + imgUrl);

        // 6.params; 商品规格参数
        /**
         * 代码不公开，有兴趣的朋友可以参考京东的代码进行开发
         */
        page.setParams("代码不公开，有兴趣的朋友可以参考京东的代码进行开发");

        // 7.商品评论数
        String commentCountUrl = "https://review.suning.com/ajax/review_satisfy/general-000000000" + id + "-" + sid + "-----satisfy.htm";
        String beforeCommentCountJson = HttpUtil.getHttpContent(commentCountUrl);   // 获得的数据为satisfy(json数据);
        try {
            String commentCountJson = beforeCommentCountJson.substring(beforeCommentCountJson.indexOf("(") + 1, beforeCommentCountJson.lastIndexOf(")"));   // 处理为json字符串
            JSONArray commentCountJsonArray = new JSONObject(commentCountJson).getJSONArray("reviewCounts");
            JSONObject commentCountJsonObj = commentCountJsonArray.getJSONObject(0);
            int commentCount = commentCountJsonObj.getInt("totalCount");
            page.setCommentCount(commentCount);
        } catch (Exception e) {
            logger.error("获取商品评论数错误，商品url为: {}", page.getUrl());
        }

        // System.out.println(page.getCommentCount());
    }

    public static void main(String[] args) {
        IDownload download = new HttpGetDownloadImpl();
        IParser parser = new SNHtmlParserImpl();
        Page page = download.download("https://product.suning.com/0070067595/191502823.html");
        parser.parser(page);
    }
}


/*商品价格api和评论数api演算过程
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000624990015,000000000624989998_020_0200101_0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000_1_getClusterPrice.vhtm

https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000624989983,000000000624990007,000000000624989992,000000000624989985,000000000624990015,000000000624989996,000000000624989984,000000000624990013,000000000624989994,000000000624989982,000000000624989990,000000000624989988,000000000624989998_020_0200101_0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice


商品评论数：
https://review.suning.com/ajax/review_satisfy/general-000000000690105195-0000000000-----satisfy.htm

上面是最精简的，然后得到的信息如下：
satisfy({"reviewCounts":[{"oneStarCount":1120,"twoStarCount":123,"threeStarCount":353,"fourStarCount":488,"fiveStarCount":193227,"againCount":392,"bestCount":0,"picFlagCount":9622,"totalCount":195311,"qualityStar":5.0,"installCount":0,"smallVideoCount":179}],"returnCode":"1","returnMsg":"成功获取评价个数"})

totalCount就是总的评论数，但是在获取数据前需要先去掉satisfy()才能转换为json对象
更正：
对于商品：https://product.suning.com/0070156122/655027353.html
它的评论数api为：https://review.suning.com/ajax/review_satisfy/general-000000000655027353-0070156122-----satisfy.htm
也就是像价格api一样，需要多加前置序号号即0070156122



荣耀9：https://product.suning.com/0000000000/624990015.html
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000624989983,000000000624990007,000000000624989992,000000000624989985,000000000624990015,000000000624989996,000000000624989984,000000000624990013,000000000624989994,000000000624989982,000000000624989990,000000000624989988,000000000624989998_020_0200101_0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice

iPhone 8:https://product.suning.com/0000000000/690105195.html
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000690105206,000000000690098240,000000000690105196,000000000690098238,000000000690105195,000000000690098237_020_0200101_0000000000,0000000000,0000000000,0000000000,0000000000,0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice

精简1：去掉=getClusterPrice

精简2：为两个商品的时候
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000690105195,000000000690098237_020_0200101_0000000000,0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice

精简3：为1个商品的时候
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000690105195,000000000690098237_020_0200101_0000000000,0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice

https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000690105195_020_0200101_0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice

测试一个商品：
https://product.suning.com/0070079092/624395335.html
推测价格api应该为：
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000624395335_020_0200101_0000000000_1_getClusterPrice.vhtm?callback=getClusterPrice
结果不是

完整的是：
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000619243750,000000000645198735,000000000694003478,000000000619817164,000000000637794787,000000010329184611,000000000652850671,000000000645198826,000000000639022530,000000010224357612,000000010270382948,000000000619241632,000000010224357593,000000010329184488,000000010255926106,000000000619817163,000000010129532570,000000000624395335,000000010270382946,000000000688539150_020_0200101_0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092,0070079092_1_getClusterPrice.vhtm?callback=getClusterPrice

推测：
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000624395335_020_0200101_0070079092_1_getClusterPrice.vhtm?callback=getClusterPrice
正确！

所以结论：
手机产品的价格api应该为：
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000 + 商品id + _020_0200101_ + 商品id前置序号 + _1_getClusterPrice.vhtm?callback=getClusterPrice

测试：
https://product.suning.com/0070156122/655027353.html
手机产品的价格api应该为：
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000000655027353_020_0200101_0070156122_1_getClusterPrice.vhtm?callback=getClusterPrice
确实如此！

得到的数据为：
getClusterPrice([{"cmmdtyCode":"000000000655027353","cmmdtyType":"1","bizCode":"0070156122","refPrice":"","maPrice":"","snPrice":"3888.00","mpsPrice":"2368.00","proPrice":"2388.00","price":"2368.00","priceType":"4-1","bizCount":"1","govPrice":"","nmpsStartTime":"2018-03-29 00:00:00","nmpsEndTime":"2018-04-01 23:59:00","mpsId":"8638670","supplierCode":"0070156122","bizType":"3","status":"1","invStatus":"1","bookPrice":"","bookPriceSwell":"","finalPayment":"","vipPrice":"","limitedPrice":"","warrantyPriceList":null,"pgPrice":"","pgNum":"","pgActionId":"","singlePrice":""}]);

所以需要先将getClusterPrice();去掉才能再转换为json对象

综上所述，苏宁易购的商品需要加上两个id才能够获取完整的商品信息

https://product.suning.com/0070079092/10049960724.html
https://icps.suning.com/icps-web/getVarnishAllPrice014/000000010049960724_020_0200101_0070079092_1_getClusterPrice.vhtm?callback=getClusterPrice
进一步测试，商品id前面的数字还有可能为7个0的，前面的是9个0


 */