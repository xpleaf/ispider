package cn.xpleaf.spider.core.store.impl;

import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.core.store.IStore;
import cn.xpleaf.spider.utils.HBaseUtil;

/**
 * 将爬虫解析之后的数据存储到HBase对应的表product中
  cf1 存储 id source price comment brand url
  cf2 存储 title params imgUrl
 */
public class HBaseStoreImpl implements IStore {
    @Override
    public void store(Page page) {
        HBaseUtil.store(page);
    }
}
