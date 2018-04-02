package cn.xpleaf.spider.core.store.impl;

import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.core.store.IStore;

/**
 * 将商品数据输出到控制台，作为调试使用
 */
public class ConsoleStore implements IStore {
    @Override
    public void store(Page page) {
        System.out.println("ThreadID--->" + Thread.currentThread().getId() + ":" +
                page.getSource() + ":" + page.getUrl() + "--->" + page.getPrice());
    }
}
