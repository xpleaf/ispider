package cn.xpleaf.spider.monitor;

import cn.xpleaf.spider.utils.MailUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

/**
 * 使用zookeeper来做spider的节点发现服务
 *
 * 所以该task是独立于Spider的进程
 * 要想去监控爬虫，则需要不断的监听/zk的目录变化
 *
 * 实际上要想让zk监听到有新的spider服务增加进来或者有新的spider服务丢失，则需要我们去监控zk中对应的目录的变化
 * 所以我们这里需要zk的监听器Watcher
 *      专门监听zk的目录
 * 使用CuratorFramework向zk中进行节点的注册（向zk中增删节点），用watcher监听该目录的变化
 *
 * 得需要完成实现监听器，在监听器中完成对应的操作的变化
 */
public class SpiderMonitorTask implements Watcher {

    private List<String> previousNodes;
    private CuratorFramework curator;
    private Logger logger = LoggerFactory.getLogger(SpiderMonitorTask.class);

    /**
     * 因为要监控，所以我们得要知道监控的目录，要拿到监控目录下面的东西
     * 以便我们当节点发生变化之后，知道是由谁引起的变化
     * 所以要获取初始的节点状态
     */
    public SpiderMonitorTask() {
        String zkStr = "uplooking01:2181,uplooking02:2181,uplooking03:2181";
        int baseSleepTimeMs = 1000;
        int maxRetries = 3;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        curator = CuratorFrameworkFactory.newClient(zkStr, retryPolicy);
        curator.start();
        try {
            previousNodes = curator.getChildren().usingWatcher(this).forPath("/ispider");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 这个方法，当监控的zk对应的目录一旦有变动，就会被调用
     * 得到当前最新的节点状态，将最新的节点状态和初始或者上一次的节点状态作比较，那我们就知道了是由谁引起的节点变化
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        try {
            List<String> currentNodes = curator.getChildren().usingWatcher(this).forPath("/ispider");
//            HashSet<String> previousNodesSet = new HashSet<>(previousNodes);
            if(currentNodes.size() > previousNodes.size()) { // 最新的节点服务，超过之前的节点服务个数，有新的节点增加进来
                for(String node : currentNodes) {
                    if(!previousNodes.contains(node)) {
                        // 当前节点就是新增节点
                        logger.info("----有新的爬虫节点{}新增进来", node);
                    }
                }
            } else if(currentNodes.size() < previousNodes.size()) {  // 有节点挂了    发送告警邮件或者短信
                for(String node : previousNodes) {
                    if(!currentNodes.contains(node)) {
                        // 当前节点挂掉了 得需要发邮件
                        logger.info("----有爬虫节点{}挂掉了", node);
                        MailUtil.sendMail("有爬虫节点挂掉了，请人工查看爬虫节点的情况，节点信息为：", node);
                    }
                }
            } // 挂掉和新增的数目一模一样，上面是不包括这种情况的，有兴趣的朋友可以直接实现包括这种特殊情况的监控
            previousNodes = currentNodes;   // 更新上一次的节点列表，成为最新的节点列表
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 在原生的API需要再做一次监控，因为每一次监控只会生效一次，所以当上面发现变化后，需要再监听一次，这样下一次才能监听到
        // 但是在使用curator的API时则不需要这样做
    }

    public static void main(String[] args) {
        new SpiderMonitorTask().start();
    }

    /**
     * 因为我们的监听服务不能停止，所以必须持续不断的运行，所以死循环
     */
    public void start() {
        while (true) {

        }
    }

}
