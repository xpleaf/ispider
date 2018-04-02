package cn.xpleaf.spider.utils;

import cn.xpleaf.spider.core.pojo.Page;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseUtil {
    public static void store(Page page) {
        Configuration conf = HBaseConfiguration.create();
        Connection connection = null;
        Table table = null;
        try {
            connection = ConnectionFactory.createConnection(conf);
            TableName tableName = TableName.valueOf("phone");
            table = connection.getTable(tableName);
            List<Put> puts = new ArrayList<>();
            byte[] rowKey = (page.getId() + "_" + page.getSource()).getBytes(); // 商品的id和source作为行键
            byte[] cf1 = "cf1".getBytes();
            byte[] cf2 = "cf2".getBytes();
            // cf1:price
            Put pricePut = new Put(rowKey);
            // 必须要做是否为null判断，否则会有空指针异常
            pricePut.addColumn(cf1, "price".getBytes(), page.getPrice() != null ? String.valueOf(page.getPrice()).getBytes() : "".getBytes());
            puts.add(pricePut);
            // cf1:comment
            Put commentPut = new Put(rowKey);
            commentPut.addColumn(cf1, "comment".getBytes(), page.getCommentCount() != null ? String.valueOf(page.getCommentCount()).getBytes() : "".getBytes());
            puts.add(commentPut);
            // cf1:brand
            Put brandPut = new Put(rowKey);
            brandPut.addColumn(cf1, "brand".getBytes(), page.getBrand() != null ? page.getBrand().getBytes() : "".getBytes());
            puts.add(brandPut);
            // cf1:url
            Put urlPut = new Put(rowKey);
            urlPut.addColumn(cf1, "url".getBytes(), page.getUrl() != null ? page.getUrl().getBytes() : "".getBytes());
            puts.add(urlPut);
            // cf2:title
            Put titlePut = new Put(rowKey);
            titlePut.addColumn(cf2, "title".getBytes(), page.getTitle() != null ? page.getTitle().getBytes() : "".getBytes());
            puts.add(titlePut);
            // cf2:params
            Put paramsPut = new Put(rowKey);
            paramsPut.addColumn(cf2, "params".getBytes(), page.getParams() != null ? page.getParams().getBytes() : "".getBytes());
            puts.add(paramsPut);
            // cf2:imgUrl
            Put imgUrlPut = new Put(rowKey);
            imgUrlPut.addColumn(cf2, "imgUrl".getBytes(), page.getImgUrl() != null ? page.getImgUrl().getBytes() : "".getBytes());
            puts.add(imgUrlPut);

            // 添加数据到表中
            table.put(puts);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(table != null) {
                    table.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
