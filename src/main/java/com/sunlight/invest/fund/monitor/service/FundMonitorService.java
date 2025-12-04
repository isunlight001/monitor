package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.AlarmRecord;
import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.entity.MonitorFund;
import com.sunlight.invest.fund.monitor.mapper.AlarmRecordMapper;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.mapper.MonitorFundMapper;
import com.sunlight.invest.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金监控服务
 * <p>
 * 实现五种监控规则：
 * - 规则A：连续5天或以上上涨/下跌
 * - 规则B：单日涨跌幅绝对值≥5%
 * - 规则C：连续2天累计涨跌幅绝对值≥4%
 * - 规则D：连续3天累计涨跌幅绝对值≥5%
 * - 规则E：连续4天累计涨跌幅绝对值≥5%
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@Service
public class FundMonitorService {

    private static final Logger log = LoggerFactory.getLogger(FundMonitorService.class);

    @Autowired
    private FundNavMapper fundNavMapper;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @Autowired
    private FundCrawlerService fundCrawlerService;
    @Autowired
    private MonitorFundMapper monitorFundMapper;

    private static final BigDecimal THRESHOLD_5_PERCENT = new BigDecimal("2.0");
    private static final BigDecimal THRESHOLD_4_PERCENT = new BigDecimal("4.0");
    private static final int MONITOR_DAYS = 5; // 增加到7天以满足规则E的需求
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 内部类用于存储预警信息
    private static class AlertInfo {
        private String subject;
        private String content;
        private AlarmRecord alarmRecord;

        public AlertInfo(String subject, String content, AlarmRecord alarmRecord) {
            this.subject = subject;
            this.content = content;
            this.alarmRecord = alarmRecord;
        }

        // Getters
        public String getSubject() { return subject; }
        public String getContent() { return content; }
        public AlarmRecord getAlarmRecord() { return alarmRecord; }
    }

    public void scheduledMonitorTask() {
        // 从数据库获取所有启用的监控基金
        List<MonitorFund> monitorFunds = monitorFundMapper.selectAllEnabled();
        log.info("获取到 {} 个启用的监控基金", monitorFunds.size());

        // 收集所有基金的预警信息
        List<AlertInfo> allAlerts = new ArrayList<>();

        for (MonitorFund monitorFund : monitorFunds) {
            String fundCode = monitorFund.getFundCode();
            String fundName = monitorFund.getFundName();

            try {
                // 1. 增量更新基金数据
                log.info("开始更新基金数据: {} - {}", fundCode, fundName);
                int updateCount = fundCrawlerService.incrementalUpdate(fundCode, fundName);
                log.info("基金数据更新完成: {} - {}, 更新记录数: {}", fundCode, fundName, updateCount);

                // 2. 执行监控检查
                log.info("开始监控基金: {} - {}", fundCode, fundName);
                monitorFund(fundCode, allAlerts); // 传递全局预警列表
                log.info("基金监控完成: {} - {}", fundCode, fundName);

            } catch (Exception e) {
                log.error("处理基金失败: {} - {}", fundCode, fundName, e);
            }
        }

        // 如果有预警信息，则集中发送
        if (!allAlerts.isEmpty()) {
            sendGlobalCombinedAlerts(allAlerts);
        }

        log.info("========== 基金监控定时任务执行完成 ==========");
    }

    /**
     * 监控指定基金，并将预警信息添加到全局列表中
     *
     * @param fundCode 基金代码
     * @param allAlerts 全局预警信息列表
     */
    public void monitorFund(String fundCode, List<AlertInfo> allAlerts) {
        log.info("开始监控基金: {}", fundCode);

        // 获取最近7天的数据 降序
        List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, MONITOR_DAYS);
        if (navList == null || navList.isEmpty()) {
            log.warn("基金 {} 没有数据", fundCode);
            return;
        }

        // 收集当前基金的预警信息
        List<AlertInfo> fundAlerts = new ArrayList<>();

        // 执行五种规则检查并收集预警信息
        checkRuleA(navList, fundAlerts);
        checkRuleB(navList, fundAlerts);
        checkRuleC(navList, fundAlerts);
        checkRuleD(navList, fundAlerts);
        checkRuleE(navList, fundAlerts);

        // 将当前基金的预警信息添加到全局列表
        allAlerts.addAll(fundAlerts);
    }

    /**
     * 监控指定基金（保持原有方法签名以兼容其他调用）
     *
     * @param fundCode 基金代码
     */
    public void monitorFund(String fundCode) {
        log.info("开始监控基金: {}", fundCode);

        // 获取最近7天的数据 降序
        List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, MONITOR_DAYS);
        if (navList == null || navList.isEmpty()) {
            log.warn("基金 {} 没有数据", fundCode);
            return;
        }

        // 收集所有预警信息
        List<AlertInfo> alerts = new ArrayList<>();

        // 执行五种规则检查并收集预警信息
        checkRuleA(navList, alerts);
        checkRuleB(navList, alerts);
        checkRuleC(navList, alerts);
        checkRuleD(navList, alerts);
        checkRuleE(navList, alerts);

        // 如果有预警信息，则集中发送
        if (!alerts.isEmpty()) {
            sendCombinedAlerts(navList.get(0).getFundName(), navList.get(0).getFundCode(), alerts);
        }
    }

    /**
     * 规则A：检测连续5天或以上上涨/下跌
     * <p>
     * 该方法遍历基金净值列表，检测是否存在连续5天或以上的持续上涨或下跌情况。
     * 连续上涨/下跌的判断基于每日涨跌幅的符号一致性。
     * 当检测到符合条件的情况时，会触发告警邮件通知。
     * </p>
     * <p>
     * 检测逻辑：
     * 1. 遍历净值列表，比较相邻两天的涨跌情况
     * 2. 统计连续上涨或下跌的天数
     * 3. 当连续天数达到5天或以上时，触发规则A告警
     * 4. 当涨跌趋势发生变化时，重置计数器
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleA(List<FundNav> navList, List<AlertInfo> alerts) {
        int consecutiveDays = 1;
        boolean isRising = false;
        BigDecimal cumulativeReturn = BigDecimal.ZERO;

        for (int i = 1; i < navList.size(); i++) {
            FundNav current = navList.get(i);
            BigDecimal dailyReturn = current.getDailyReturn();

            if (dailyReturn == null || dailyReturn.compareTo(BigDecimal.ZERO) == 0) {
                // 重置
                consecutiveDays = 1;
                cumulativeReturn = BigDecimal.ZERO;
                continue;
            }

            boolean currentIsRising = dailyReturn.compareTo(BigDecimal.ZERO) > 0;

            if (consecutiveDays == 1) {
                // 开始新的连续序列
                isRising = currentIsRising;
                cumulativeReturn = dailyReturn;
                consecutiveDays = 1;
            } else if (currentIsRising == isRising) {
                // 继续连续
                consecutiveDays++;
                cumulativeReturn = cumulativeReturn.add(dailyReturn);
            } else {
                // 中断，检查是否需要告警
                if (consecutiveDays >= 5) { // 修改为5天
                    alerts.add(createRuleAAlertInfo(navList.get(i - 1), consecutiveDays, cumulativeReturn, isRising));
                }
                // 重置
                consecutiveDays = 1;
                cumulativeReturn = dailyReturn;
                isRising = currentIsRising;
            }
        }

        // 检查最后的连续序列
        if (consecutiveDays >= 5) { // 修改为5天
            alerts.add(createRuleAAlertInfo(navList.get(navList.size() - 1), consecutiveDays, cumulativeReturn, isRising));
        }
    }

    /**
     * 规则B：单日涨跌幅绝对值≥5%
     * <p>
     * 该方法检查基金净值列表中是否存在单日涨跌幅绝对值达到或超过5%的情况。
     * 当发现符合条件的记录时，会立即触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleB(List<FundNav> navList, List<AlertInfo> alerts) {
        if (navList.get(0).getDailyReturn() != null &&
                navList.get(0).getDailyReturn().abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
            alerts.add(createRuleBAlertInfo(navList.get(0)));
        }
    }

    /**
     * 规则C：连续2天累计涨跌幅绝对值≥4%
     * <p>
     * 该方法检查基金净值列表中是否存在连续2天的累计涨跌幅绝对值
     * 达到或超过4%的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleC(List<FundNav> navList, List<AlertInfo> alerts) {
        // 检查连续2天
//        for (int i = 0; i < navList.size(); i++) {
        FundNav current = navList.get(0);
        FundNav previous = navList.get(1);

        if (current.getDailyReturn() != null && previous.getDailyReturn() != null) {
            BigDecimal sum2Days = current.getDailyReturn().add(previous.getDailyReturn());
            if (sum2Days.abs().compareTo(THRESHOLD_4_PERCENT) >= 0) { // 修改为4%
                alerts.add(createRuleCAlertInfo(current, 2, sum2Days));
            }
        }
//        }
    }

    /**
     * 规则D：连续3天累计涨跌幅绝对值≥5%
     * <p>
     * 该方法检查基金净值列表中是否存在连续3天的累计涨跌幅绝对值
     * 达到或超过5%的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleD(List<FundNav> navList, List<AlertInfo> alerts) {
        // 检查连续3天
//        for (int i = 2; i < navList.size(); i++) {
        FundNav current = navList.get(0);
        FundNav previous1 = navList.get(1);
        FundNav previous2 = navList.get(2);

        if (current.getDailyReturn() != null &&
                previous1.getDailyReturn() != null &&
                previous2.getDailyReturn() != null) {
            BigDecimal sum3Days = current.getDailyReturn()
                    .add(previous1.getDailyReturn())
                    .add(previous2.getDailyReturn());
            if (sum3Days.abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
                alerts.add(createRuleDAlertInfo(current, 3, sum3Days));
            }
        }
//        }
    }

    /**
     * 规则E：连续4天累计涨跌幅绝对值≥5%
     * <p>
     * 该方法检查基金净值列表中是否存在连续4天的累计涨跌幅绝对值
     * 达到或超过5%的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleE(List<FundNav> navList, List<AlertInfo> alerts) {
        // 检查连续4天
//        for (int i = 3; i < navList.size(); i++) {
        FundNav current = navList.get(0);
        FundNav previous1 = navList.get(1);
        FundNav previous2 = navList.get(2);
        FundNav previous3 = navList.get(3);

        if (current.getDailyReturn() != null &&
                previous1.getDailyReturn() != null &&
                previous2.getDailyReturn() != null &&
                previous3.getDailyReturn() != null) {
            BigDecimal sum4Days = current.getDailyReturn()
                    .add(previous1.getDailyReturn())
                    .add(previous2.getDailyReturn())
                    .add(previous3.getDailyReturn());
            if (sum4Days.abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
                alerts.add(createRuleEAlertInfo(current, 4, sum4Days));
            }
//            }
        }
    }

    /**
     * 创建规则A的预警信息
     */
    private AlertInfo createRuleAAlertInfo(FundNav nav, int days, BigDecimal cumulativeReturn, boolean isRising) {
        String subject = String.format("【基金预警-规则A】%s 连续%d天%s",
                nav.getFundName(),
                days,
                isRising ? "上涨" : "下跌");

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 连续%d天或以上%s\n" +
                        "连续天数: %d天\n" +
                        "累计涨跌幅: %.2f%%\n" +
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "请关注基金波动情况。",
                nav.getFundName(),
                nav.getFundCode(),
                days,
                isRising ? "上涨" : "下跌",
                days,
                cumulativeReturn.doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue()
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("A");
        alarmRecord.setRuleDescription("连续5天或以上上涨/下跌");
        alarmRecord.setConsecutiveDays(days);
        alarmRecord.setCumulativeReturn(cumulativeReturn);
        alarmRecord.setNavDate(nav.getNavDate());
        alarmRecord.setUnitNav(nav.getUnitNav());
        alarmRecord.setAlarmContent(content);

        return new AlertInfo(subject, content, alarmRecord);
    }

    /**
     * 创建规则B的预警信息
     */
    private AlertInfo createRuleBAlertInfo(FundNav nav) {
        String subject = String.format("【基金预警-规则B】%s 单日大幅波动",
                nav.getFundName());

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 单日涨跌幅绝对值≥5%%\n" +
                        "单日涨跌幅: %.2f%%\n" +
                        "净值日期: %s\n" +
                        "单位净值: %.4f\n" +
                        "\n" +
                        "单日波动较大，请关注市场变化。",
                nav.getFundName(),
                nav.getFundCode(),
                nav.getDailyReturn().doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue()
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("B");
        alarmRecord.setRuleDescription("单日涨跌幅绝对值≥5%");
        alarmRecord.setDailyReturn(nav.getDailyReturn());
        alarmRecord.setNavDate(nav.getNavDate());
        alarmRecord.setUnitNav(nav.getUnitNav());
        alarmRecord.setAlarmContent(content);

        return new AlertInfo(subject, content, alarmRecord);
    }

    /**
     * 创建规则C的预警信息
     */
    private AlertInfo createRuleCAlertInfo(FundNav nav, int days, BigDecimal cumulativeReturn) {
        String subject = String.format("【基金预警-规则C】%s 连续%d天累计波动",
                nav.getFundName(), days);

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 连续%d天累计涨跌幅绝对值≥4%%\n" + // 修改为4%
                        "连续天数: %d天\n" +
                        "累计涨跌幅: %.2f%%\n" +
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "短期累计波动较大，请关注基金走势。",
                nav.getFundName(),
                nav.getFundCode(),
                days,
                days,
                cumulativeReturn.doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue()
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("C");
        alarmRecord.setRuleDescription("连续2天累计涨跌幅绝对值≥4%");
        alarmRecord.setConsecutiveDays(days);
        alarmRecord.setCumulativeReturn(cumulativeReturn);
        alarmRecord.setNavDate(nav.getNavDate());
        alarmRecord.setUnitNav(nav.getUnitNav());
        alarmRecord.setAlarmContent(content);

        return new AlertInfo(subject, content, alarmRecord);
    }

    /**
     * 创建规则D的预警信息
     */
    private AlertInfo createRuleDAlertInfo(FundNav nav, int days, BigDecimal cumulativeReturn) {
        String subject = String.format("【基金预警-规则D】%s 连续%d天累计波动",
                nav.getFundName(), days);

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 连续%d天累计涨跌幅绝对值≥5%%\n" +
                        "连续天数: %d天\n" +
                        "累计涨跌幅: %.2f%%\n" +
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "短期累计波动较大，请关注基金走势。",
                nav.getFundName(),
                nav.getFundCode(),
                days,
                days,
                cumulativeReturn.doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue()
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("D");
        alarmRecord.setRuleDescription("连续3天累计涨跌幅绝对值≥5%");
        alarmRecord.setConsecutiveDays(days);
        alarmRecord.setCumulativeReturn(cumulativeReturn);
        alarmRecord.setNavDate(nav.getNavDate());
        alarmRecord.setUnitNav(nav.getUnitNav());
        alarmRecord.setAlarmContent(content);

        return new AlertInfo(subject, content, alarmRecord);
    }

    /**
     * 创建规则E的预警信息
     */
    private AlertInfo createRuleEAlertInfo(FundNav nav, int days, BigDecimal cumulativeReturn) {
        String subject = String.format("【基金预警-规则E】%s 连续%d天累计波动",
                nav.getFundName(), days);

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 连续%d天累计涨跌幅绝对值≥5%%\n" +
                        "连续天数: %d天\n" +
                        "累计涨跌幅: %.2f%%\n" +
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "短期累计波动较大，请关注基金走势。",
                nav.getFundName(),
                nav.getFundCode(),
                days,
                days,
                cumulativeReturn.doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue()
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("E");
        alarmRecord.setRuleDescription("连续4天累计涨跌幅绝对值≥5%");
        alarmRecord.setConsecutiveDays(days);
        alarmRecord.setCumulativeReturn(cumulativeReturn);
        alarmRecord.setNavDate(nav.getNavDate());
        alarmRecord.setUnitNav(nav.getUnitNav());
        alarmRecord.setAlarmContent(content);

        return new AlertInfo(subject, content, alarmRecord);
    }

    /**
     * 集中发送所有预警信息
     */
    private void sendCombinedAlerts(String fundName, String fundCode, List<AlertInfo> alerts) {
        if (alerts.isEmpty()) {
            return;
        }

        // 构建集中邮件主题
        String subject = String.format("【基金预警汇总】%s - %s", fundName, fundCode);
        
        // 构建集中邮件内容
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(String.format("基金名称: %s\n", fundName));
        contentBuilder.append(String.format("基金代码: %s\n", fundCode));
        contentBuilder.append("========================================\n");
        contentBuilder.append(String.format("共发现 %d 个预警事件:\n\n", alerts.size()));

        // 添加每个预警的详细信息
        for (int i = 0; i < alerts.size(); i++) {
            AlertInfo alert = alerts.get(i);
            contentBuilder.append(String.format("--- 预警 #%d ---\n", i + 1));
            contentBuilder.append(alert.getContent());
            contentBuilder.append("\n\n");
        }

        contentBuilder.append("请关注基金波动情况。");

        String content = contentBuilder.toString();

        try {
            // 发送集中预警邮件给所有接收人
            emailNotificationService.sendEmailToAllRecipients(subject, content);
            log.info("基金 {} 预警汇总邮件已发送，共 {} 个预警", fundCode, alerts.size());

            // 保存所有告警记录
            for (AlertInfo alert : alerts) {
                alarmRecordMapper.insert(alert.getAlarmRecord());
            }
        } catch (Exception e) {
            log.error("发送基金 {} 预警汇总邮件失败", fundCode, e);
        }
    }

    /**
     * 全局集中发送所有预警信息
     */
    private void sendGlobalCombinedAlerts(List<AlertInfo> allAlerts) {
        if (allAlerts.isEmpty()) {
            return;
        }

        // 构建集中邮件主题
        String subject = String.format("【基金预警汇总】%s", "多基金预警通知");
        
        // 构建集中邮件内容
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("基金预警汇总报告\n");
        contentBuilder.append("========================================\n");
        contentBuilder.append(String.format("共发现 %d 个预警事件:\n\n", allAlerts.size()));

        // 按基金分组显示预警信息
        Map<String, List<AlertInfo>> alertsByFund = new HashMap<>();
        for (AlertInfo alert : allAlerts) {
            String fundCode = alert.getAlarmRecord().getFundCode();
            alertsByFund.computeIfAbsent(fundCode, k -> new ArrayList<>()).add(alert);
        }

        // 添加每个基金的预警信息
        for (Map.Entry<String, List<AlertInfo>> entry : alertsByFund.entrySet()) {
            String fundCode = entry.getKey();
            List<AlertInfo> fundAlerts = entry.getValue();
            
            if (!fundAlerts.isEmpty()) {
                contentBuilder.append(String.format("--- 基金 %s ---\n", fundCode));
                for (int i = 0; i < fundAlerts.size(); i++) {
                    AlertInfo alert = fundAlerts.get(i);
                    contentBuilder.append(String.format("预警 #%d:\n", i + 1));
                    contentBuilder.append(alert.getContent());
                    contentBuilder.append("\n");
                }
                contentBuilder.append("\n");
            }
        }

        contentBuilder.append("请关注基金波动情况。");

        String content = contentBuilder.toString();

        try {
            // 发送集中预警邮件给所有接收人
            emailNotificationService.sendEmailToAllRecipients(subject, content);
            log.error("全局基金预警汇总邮件已发送，共 {} 个预警", allAlerts.size());

            // 保存所有告警记录
            for (AlertInfo alert : allAlerts) {
                alarmRecordMapper.insert(alert.getAlarmRecord());
            }
        } catch (Exception e) {
            log.error("发送全局基金预警汇总邮件失败", e);
        }
    }
}