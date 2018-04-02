CREATE DATABASE IF NOT EXISTS ispider;
DROP TABLE IF EXISTS `phone`;
CREATE TABLE `phone` (
  `id` varchar(30) CHARACTER SET armscii8 NOT NULL COMMENT '商品id',
  `source` varchar(30) NOT NULL COMMENT '商品来源，如jd suning gome等',
  `brand` varchar(30) DEFAULT NULL COMMENT '手机品牌',
  `title` varchar(255) DEFAULT NULL COMMENT '商品页面的手机标题',
  `price` float(10,2) DEFAULT NULL COMMENT '手机价格',
  `comment_count` varchar(30) DEFAULT NULL COMMENT '手机评论',
  `url` varchar(500) DEFAULT NULL COMMENT '手机详细信息地址',
  `img_url` varchar(500) DEFAULT NULL COMMENT '图片地址',
  `params` text COMMENT '手机参数，json格式存储',
  PRIMARY KEY (`id`,`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;