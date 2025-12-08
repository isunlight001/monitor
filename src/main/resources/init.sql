-- ----------------------------
-- Records of email_recipient
-- ----------------------------
INSERT INTO `email_recipient` VALUES (1, 'admin', '903635811@qq.com', 1, '2025-12-04 11:02:09', '2025-12-04 11:02:09');
INSERT INTO `email_recipient` VALUES (2, '薛雄伟', '329045062@qq.com', 1, '2025-12-04 11:02:41', '2025-12-04 15:23:04');

-- ----------------------------
-- Table structure for fund_monitor
-- ----------------------------

-- ----------------------------
-- Records of fund_monitor
-- ----------------------------
INSERT INTO `fund_monitor` VALUES (1, '010500', '中银创新医疗混合C', 1, '2025-12-03 11:23:14', '2025-12-03 11:23:14');
INSERT INTO `fund_monitor` VALUES (2, '016858', '国金量化多因子C', 1, '2025-12-03 11:23:50', '2025-12-03 11:23:50');
INSERT INTO `fund_monitor` VALUES (3, '007950', '招商量化精选C', 1, '2025-12-03 11:24:10', '2025-12-03 11:24:10');
INSERT INTO `fund_monitor` VALUES (4, '014198', '华夏智胜先锋股票C', 1, '2025-12-03 11:24:43', '2025-12-03 11:24:43');
INSERT INTO `fund_monitor` VALUES (5, '021526', '南华丰汇混合C', 1, '2025-12-03 11:25:04', '2025-12-03 11:25:04');
INSERT INTO `fund_monitor` VALUES (6, '018561', '中信保诚多策略灵活配置混合C', 1, '2025-12-03 11:25:33', '2025-12-03 11:25:33');
INSERT INTO `fund_monitor` VALUES (7, '002834', '华夏新锦绣灵活配置混合C', 1, '2025-12-03 11:25:52', '2025-12-03 11:25:52');
INSERT INTO `fund_monitor` VALUES (8, '012920', '易方达全球成长精选混合', 1, '2025-12-03 11:26:16', '2025-12-03 11:26:16');
INSERT INTO `fund_monitor` VALUES (9, '002170', '东吴移动互联网灵活配置混合C', 1, '2025-12-03 11:26:42', '2025-12-03 11:26:42');
INSERT INTO `fund_monitor` VALUES (10, '023918', '华夏国证自由现金流ETF连接C', 1, '2025-12-04 15:06:24', '2025-12-04 15:06:24');
INSERT INTO `fund_monitor` VALUES (11, '007721', '天弘标普500A', 1, '2025-12-08 14:15:21', '2025-12-08 14:15:21');
INSERT INTO `fund_monitor` VALUES (12, '017428', '易方达成长混合动力C', 1, '2025-12-08 14:16:40', '2025-12-08 14:16:40');

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'threshold_5_percent', '5.0', '5%阈值', 1, '2025-12-04 15:04:28', '2025-12-04 15:11:50');
INSERT INTO `system_config` VALUES (2, 'threshold_4_percent', '4.0', '4%阈值', 1, '2025-12-04 15:04:28', '2025-12-04 15:04:28');
INSERT INTO `system_config` VALUES (3, 'monitor_days', '5', '监控天数', 1, '2025-12-04 15:04:28', '2025-12-04 15:08:54');
INSERT INTO `system_config` VALUES (4, 'schedule_cron', '0 0 14 * * ?', '定时任务cron表达式', 1, '2025-12-04 15:04:28', '2025-12-04 15:12:29');

INSERT INTO `user` VALUES (1, 'admin', 'Test#x86', 'admin@example.com', '系统管理员', 1, '2025-12-06 09:48:28', '2025-12-06 09:50:42');
INSERT INTO `user` VALUES (2, 'test', 'Test#x86', 'admin@example.com', '系统管理员', 1, '2025-12-06 09:48:28', '2025-12-06 09:50:42');


