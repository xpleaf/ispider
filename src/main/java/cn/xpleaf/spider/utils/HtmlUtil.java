package cn.xpleaf.spider.utils;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析页面的工具类
 */
public class HtmlUtil {

    /**
     * 通过url获取商品ID
     * https://item.jd.com/3133843.html
     * ==> 3133843
     *
     * @param url
     * @return
     */
    public static String getIdByUrl(String url) {
        String id = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        return id;
    }

    /**
     * 根据指定的xpath，从tagNode中选择具体的标签Text
     *
     * @param tagNode
     * @param xpath
     * @return
     */
    public static String getTextByXpath(TagNode tagNode, String xpath) {
        Object[] objs = null;
        try {
            objs = tagNode.evaluateXPath(xpath);
            if (objs != null && objs.length > 0) {
                TagNode titleNode = (TagNode) objs[0];
                return titleNode.getText().toString().trim();
            }
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据xpath和属性获取对应标签的属性值
     *
     * @param tagNode
     * @param attr
     * @param xpath
     * @return
     */
    public static String getAttrByXpath(TagNode tagNode, String attr, String xpath) {
        try {
            Object[] objs = tagNode.evaluateXPath(xpath);
            if (objs != null && objs.length > 0) {
                TagNode node = (TagNode) objs[0];
                return node.getAttributeByName(attr);
            }
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到url列表
     * @param tagNode
     * @param attr
     * @param xpath
     * @return
     */
    public static List<String> getListUrlByXpath(TagNode tagNode, String attr, String xpath) {
        List<String> urls = new ArrayList<>();
        try {
            Object[] objs = tagNode.evaluateXPath(xpath);
            if (objs != null && objs.length > 0) {
                for (Object obj : objs) {
                    TagNode aTagNode = (TagNode) obj;
                    String url = aTagNode.getAttributeByName(attr);
                    urls.add("https:" + url);
                }
            }
            return urls;
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据xpath来获取对应商品的规格参数
     *
     * @param tagNode
     * @param xpath
     * @return
     */
    public static JSONObject getParams(TagNode tagNode, String xpath, String paramTitleXpath, String paramValueXpath) {
        Object[] objs;
        JSONObject paramJSONObj = new JSONObject();
        try {
            // 获取规格参数中所有的[规格类目]
            objs = tagNode.evaluateXPath(xpath);
            if (objs != null && objs.length > 0) {
                // 遍历每一个[规格类目] div，其中每一个都包含[左侧大标题 + 右侧细节信息]
                for (Object obj : objs) {
                    TagNode paramNode = (TagNode) obj;
                    // 左侧大的标题
                    objs = paramNode.evaluateXPath(paramTitleXpath);
                    String paramTitle = null;
                    if (objs != null && objs.length > 0) {
                        TagNode paramTitleNode = (TagNode) objs[0];
                        paramTitle = paramTitleNode.getText().toString();
                    }
                    // 右侧细节信息
                    objs = paramNode.evaluateXPath(paramValueXpath);
                    JSONObject dlJsonObject = null;
                    if (objs != null && objs.length > 0) {
                        dlJsonObject = new JSONObject();
                        TagNode dlNode = (TagNode) objs[0];
                        List<TagNode> childTagList = dlNode.getChildTagList();
                        for (int i = 0; i < childTagList.size(); i = i + 2) {
                            TagNode childTagTitle = childTagList.get(i);    // i = 0
                            TagNode chileTagValue = childTagList.get(i + 1); // 1
                            if (chileTagValue.getAttributeByName("class") != null) { // 处理表格中的?提示单元格，它多了class="Ptable-tips"的属性
                                i++;    // 2
                                // 此时上面的i自增，此时的i指向该?提示单元格，i+1之后的节点才是真正的value节点
                                chileTagValue = childTagList.get(i + 1);    // 3
                            }
                            dlJsonObject.put(childTagTitle.getText().toString().trim(), chileTagValue.getText().toString().trim());
                        }
                    }
                    paramJSONObj.put(paramTitle, dlJsonObject);
                }
            }
            return paramJSONObj;
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getIdByUrl("https://item.jd.com/3133843.html"));
    }
}
