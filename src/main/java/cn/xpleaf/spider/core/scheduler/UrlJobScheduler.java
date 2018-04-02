package cn.xpleaf.spider.core.scheduler;

import cn.xpleaf.spider.core.scheduler.job.UrlJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;

/**
 * url定时调度器，定时向url对应仓库中存放种子url
 *
 * 业务规定：每天凌晨1点10分向仓库中存放种子url
 */
public class UrlJobScheduler {

    public UrlJobScheduler() {
        init();
    }

    /**
     * 初始化调度器
     */
    public void init() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // 如果没有以下start方法的执行，则是不会开启任务的调度
            scheduler.start();

            String name = "URL_SCHEDULER_JOB";
            String group = "URL_SCHEDULER_JOB_GROUP";
            JobDetail jobDetail = new JobDetail(name, group, UrlJob.class);
            String cronExpression = "0 10 1 * * ?";
//            String cronExpression = "*/2 * * * * ?";    // 每2秒执行
            Trigger trigger = new CronTrigger(name, group, cronExpression);

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UrlJobScheduler urlJobScheduler = new UrlJobScheduler();
        urlJobScheduler.start();
    }

    /**
     * 定时调度任务
     * 因为我们每天要定时从指定的仓库中获取种子url，并存放到高优先级的url列表中
     * 所以是一个不间断的程序，所以不能停止
     */
    private void start() {
        while (true) {

        }
    }
}
