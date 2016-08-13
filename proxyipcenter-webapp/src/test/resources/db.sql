CREATE TABLE `proxy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ip` char(20) NOT NULL COMMENT 'IP地址',
  `proxy_ip` char(20) DEFAULT NULL COMMENT '代理IP，也就是目标网站最终看到的IP（多级代理的情况ip和proxy_ip不会相同）',
  `port` int(4) DEFAULT NULL COMMENT '端口号',
  `ip_value` bigint(20) DEFAULT NULL COMMENT 'ip的数字表示，用于过滤连续IP问题',
  `country` varchar(255) DEFAULT NULL COMMENT '国家',
  `area` varchar(255) DEFAULT NULL COMMENT '地区',
  `region` varchar(255) DEFAULT NULL COMMENT '省',
  `city` varchar(255) DEFAULT NULL COMMENT '市',
  `isp` varchar(20) DEFAULT NULL COMMENT '运营商',
  `country_id` int(11) DEFAULT NULL COMMENT '国家代码',
  `area_id` int(11) DEFAULT NULL COMMENT '地区代码',
  `region_id` int(11) DEFAULT NULL COMMENT '省级代码',
  `city_id` int(11) DEFAULT NULL COMMENT '城市代码',
  `isp_id` int(11) DEFAULT NULL COMMENT 'isp代码',
  `address_id` bigint(20) DEFAULT NULL COMMENT '地理位置ID，融合各个地理位置获取的一个数字，数值约接近表示实际地理位置约接近',
  `transperent` tinyint(4) DEFAULT NULL COMMENT '透明度(高匿，普通，透明)',
  `speed` bigint(20) DEFAULT NULL COMMENT '连接时间（越小速度越快）',
  `type` tinyint(4) DEFAULT NULL COMMENT '类型（http，https,httpAndHttps,socket,qq）',
  `connection_score` bigint(20) NOT NULL DEFAULT '0' COMMENT '连接性打分',
  `availbel_score` bigint(20) NOT NULL DEFAULT '0' COMMENT '可用性打分',
  `connection_score_date` date DEFAULT NULL COMMENT '连接性打分时间',
  `availbel_score_date` date DEFAULT NULL COMMENT '可用性打分时间',
  `createtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '收录时间',
  `support_gfw` tinyint(1) DEFAULT NULL COMMENT '是否支持翻墙',
  `gfw_speed` bigint(20) DEFAULT NULL COMMENT '翻墙访问速度',
  `source` varchar(255) DEFAULT NULL COMMENT '资源来源url',
  `crawler_key` varchar(255) DEFAULT NULL COMMENT '爬虫key，用户统计爬虫收集分布',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `domainqueue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(255) NOT NULL COMMENT '域名',
  `proxy_id` bigint(20) DEFAULT NULL COMMENT '代理IP的ID',
  `ip` char(20) NOT NULL COMMENT 'IP地址',
  `port` int(4) DEFAULT NULL COMMENT '端口号',
  `domain_score` bigint(20) DEFAULT '0' COMMENT '域名下打分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

