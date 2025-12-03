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
import java.util.List;

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

    private static final BigDecimal THRESHOLD_5_PERCENT = new BigDecimal("5.0");
    private static final BigDecimal THRESHOLD_4_PERCENT = new BigDecimal("4.0");
    private static final int MONITOR_DAYS = 5; // 增加到7天以满足规则E的需求
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public void scheduledMonitorTask() {

        // 从数据库获取所有启用的监控基金
        List<MonitorFund> monitorFunds = monitorFundMapper.selectAllEnabled();
        log.info("获取到 {} 个启用的监控基金", monitorFunds.size());

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
                monitorFund(fundCode);
                log.info("基金监控完成: {} - {}", fundCode, fundName);

            } catch (Exception e) {
                log.error("处理基金失败: {} - {}", fundCode, fundName, e);
            }
        }

        log.info("========== 基金监控定时任务执行完成 ==========");
    }

    /**
     * 监控指定基金
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

        // 反转列表，使其按日期升序排列
//        java.util.Collections.reverse(navList);

        // 执行五种规则检查
        checkRuleA(navList);
        checkRuleB(navList);
        checkRuleC(navList);
        checkRuleD(navList);
        checkRuleE(navList);
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
     */
    private void checkRuleA(List<FundNav> navList) {
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
                    sendRuleAAlert(navList.get(i - 1), consecutiveDays, cumulativeReturn, isRising);
                }
                // 重置
                consecutiveDays = 1;
                cumulativeReturn = dailyReturn;
                isRising = currentIsRising;
            }
        }

        // 检查最后的连续序列
        if (consecutiveDays >= 5) { // 修改为5天
            sendRuleAAlert(navList.get(navList.size() - 1), consecutiveDays, cumulativeReturn, isRising);
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
     */
    private void checkRuleB(List<FundNav> navList) {
        if (navList.get(0).getDailyReturn() != null &&
                navList.get(0).getDailyReturn().abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
            sendRuleBAlert(navList.get(0));
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
     */
    private void checkRuleC(List<FundNav> navList) {
        // 检查连续2天
//        for (int i = 0; i < navList.size(); i++) {
        FundNav current = navList.get(0);
        FundNav previous = navList.get(1);

        if (current.getDailyReturn() != null && previous.getDailyReturn() != null) {
            BigDecimal sum2Days = current.getDailyReturn().add(previous.getDailyReturn());
            if (sum2Days.abs().compareTo(THRESHOLD_4_PERCENT) >= 0) { // 修改为4%
                sendRuleCAlert(current, 2, sum2Days);
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
     */
    private void checkRuleD(List<FundNav> navList) {
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
                sendRuleDAlert(current, 3, sum3Days);
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
     */
    private void checkRuleE(List<FundNav> navList) {
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
                sendRuleEAlert(current, 4, sum4Days);
            }
//            }
        }
    }

    /**
     * 发送规则A告警邮件并保存记录
     */
    private void sendRuleAAlert(FundNav nav, int days, BigDecimal cumulativeReturn, boolean isRising) {
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

        try {
            emailNotificationService.sendEmail(subject, content);
            log.info("规则A告警邮件已发送: fundCode={}, days={}, return={}",
                    nav.getFundCode(), days, cumulativeReturn);

            // 保存告警记录
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
            alarmRecordMapper.insert(alarmRecord);
        } catch (Exception e) {
            log.error("发送规则A告警邮件失败", e);
        }
    }

    /**
     * 发送规则B告警邮件并保存记录
     */
    private void sendRuleBAlert(FundNav nav) {
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

        try {
            emailNotificationService.sendEmail(subject, content);
            log.info("规则B告警邮件已发送: fundCode={}, return={}",
                    nav.getFundCode(), nav.getDailyReturn());

            // 保存告警记录
            AlarmRecord alarmRecord = new AlarmRecord();
            alarmRecord.setFundCode(nav.getFundCode());
            alarmRecord.setFundName(nav.getFundName());
            alarmRecord.setRuleCode("B");
            alarmRecord.setRuleDescription("单日涨跌幅绝对值≥5%");
            alarmRecord.setDailyReturn(nav.getDailyReturn());
            alarmRecord.setNavDate(nav.getNavDate());
            alarmRecord.setUnitNav(nav.getUnitNav());
            alarmRecord.setAlarmContent(content);
            alarmRecordMapper.insert(alarmRecord);
        } catch (Exception e) {
            log.error("发送规则B告警邮件失败", e);
        }
    }

    /**
     * 发送规则C告警邮件并保存记录
     */
    private void sendRuleCAlert(FundNav nav, int days, BigDecimal cumulativeReturn) {
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

        try {
            emailNotificationService.sendEmail(subject, content);
            log.info("规则C告警邮件已发送: fundCode={}, days={}, return={}",
                    nav.getFundCode(), days, cumulativeReturn);

            // 保存告警记录
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
            alarmRecordMapper.insert(alarmRecord);
        } catch (Exception e) {
            log.error("发送规则C告警邮件失败", e);
        }
    }

    /**
     * 发送规则D告警邮件并保存记录
     */
    private void sendRuleDAlert(FundNav nav, int days, BigDecimal cumulativeReturn) {
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

        try {
            emailNotificationService.sendEmail(subject, content);
            log.info("规则D告警邮件已发送: fundCode={}, days={}, return={}",
                    nav.getFundCode(), days, cumulativeReturn);

            // 保存告警记录
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
            alarmRecordMapper.insert(alarmRecord);
        } catch (Exception e) {
            log.error("发送规则D告警邮件失败", e);
        }
    }

    /**
     * 发送规则E告警邮件并保存记录
     */
    private void sendRuleEAlert(FundNav nav, int days, BigDecimal cumulativeReturn) {
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

        try {
            emailNotificationService.sendEmail(subject, content);
            log.info("规则E告警邮件已发送: fundCode={}, days={}, return={}",
                    nav.getFundCode(), days, cumulativeReturn);

            // 保存告警记录
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
            alarmRecordMapper.insert(alarmRecord);
        } catch (Exception e) {
            log.error("发送规则E告警邮件失败", e);
        }
    }
}