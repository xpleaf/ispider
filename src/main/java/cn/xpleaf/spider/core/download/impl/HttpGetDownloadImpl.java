package cn.xpleaf.spider.core.download.impl;

import cn.xpleaf.spider.core.download.IDownload;
import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.utils.HttpUtil;

/**
 * 数据下载实现类
 */
public class HttpGetDownloadImpl implements IDownload {

    @Override
    public Page download(String url) {
        Page page = new Page();
        String content = HttpUtil.getHttpContent(url);  // 获取网页数据
        page.setUrl(url);
        page.setContent(content);
        return page;
    }
}
