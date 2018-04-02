package cn.xpleaf.spider.core.download;

import cn.xpleaf.spider.core.pojo.Page;

/**
 * 网页数据下载
 */
public interface IDownload {
    /**
     * 下载给定url的网页数据
     * @param url
     * @return
     */
    public Page download(String url);
}
