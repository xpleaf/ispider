package cn.xpleaf.spider.core.store.impl;

import cn.xpleaf.spider.core.pojo.Page;
import cn.xpleaf.spider.core.store.IStore;
import cn.xpleaf.spider.utils.DBCPUtil;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

/**
 * 使用dbc数据库连接池将数据写入mysql表中
 */
public class MySQLStoreImpl implements IStore {
    private QueryRunner queryRunner = new QueryRunner(DBCPUtil.getDataSource());

    @Override
    public void store(Page page) {
        String sql = "insert into phone(id, source, brand, title, price, comment_count, url, img_url, params) values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            queryRunner.update(sql, page.getId(),
                    page.getSource(),
                    page.getBrand(),
                    page.getTitle(),
                    page.getPrice(),
                    page.getCommentCount(),
                    page.getUrl(),
                    page.getImgUrl(),
                    page.getParams());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
