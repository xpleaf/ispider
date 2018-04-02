package cn.xpleaf.spider;

import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.core.store.IStore;
import cn.xpleaf.spider.core.store.impl.HBaseStoreImpl;
import cn.xpleaf.spider.core.store.impl.MySQLStoreImpl;
import org.junit.Test;

public class DBTest {

    @Test
    public void mysqlInsert() {
        Page page = new Page();

        page.setId("568329488324");
        page.setSource("jd.com");

        IStore store = new MySQLStoreImpl();

        store.store(page);
    }

    @Test
    public void hbaseInsert() {
        Page page = new Page();

        page.setId("568329488324");
        page.setSource("jd.com");

        page.setBrand("xxx");

        IStore store = new HBaseStoreImpl();

        store.store(page);
    }
}
