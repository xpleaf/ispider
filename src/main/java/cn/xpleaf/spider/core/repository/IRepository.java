package cn.xpleaf.spider.core.repository;

/**
 * url 仓库
 * 主要功能：
 *      向仓库中添加url（高优先级的列表，低优先级的商品url）
 *      从仓库中获取url（优先获取高优先级的url，如果没有，再获取低优先级的url）
 *
 */
public interface IRepository {

    /**
     * 获取url的方法
     * 从仓库中获取url（优先获取高优先级的url，如果没有，再获取低优先级的url）
     * @return
     */
    public String poll();

    /**
     * 向高优先级列表中添加商品列表url
     * @param highUrl
     */
    public void offerHigher(String highUrl);

    /**
     * 向低优先级列表中添加商品url
     * @param lowUrl
     */
    public void offerLower(String lowUrl);

}
