package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private static final BigDecimal THRESHOLD_5_PERCENT = new BigDecimal("5.0");
    private static final int MONITOR_DAYS = 5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 监控指定基金
     *
     * @param fundCode 基金代码
     */
    public void monitorFund(String fundCode) {
        log.info("开始监控基金: {}", fundCode);

        // 获取最近30天的数据 降序
        List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, MONITOR_DAYS);
        if (navList == null || navList.isEmpty()) {
            log.warn("基金 {} 没有数据", fundCode);
            return;
        }

        // 反转列表，使其按日期升序排列
        java.util.Collections.reverse(navList);

        // 执行三种规则检查
        checkRuleA(navList);
        checkRuleB(navList);
        checkRuleC(navList);
    }

    /**
     * 规则A：检测连续4天或以上上涨/下跌
     * <p>
     * 该方法遍历基金净值列表，检测是否存在连续4天或以上的持续上涨或下跌情况。
     * 连续上涨/下跌的判断基于每日涨跌幅的符号一致性。
     * 当检测到符合条件的情况时，会触发告警邮件通知。
     * </p>
     * <p>
     * 检测逻辑：
     * 1. 遍历净值列表，比较相邻两天的涨跌情况
     * 2. 统计连续上涨或下跌的天数
     * 3. 当连续天数达到4天或以上时，触发规则A告警
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
                if (consecutiveDays >= 4) {
                    sendRuleAAlert(navList.get(i - 1), consecutiveDays, cumulativeReturn, isRising);
                }
                // 重置
                consecutiveDays = 1;
                cumulativeReturn = dailyReturn;
                isRising = currentIsRising;
            }
        }

        // 检查最后的连续序列
        if (consecutiveDays >= 4) {
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
        for (FundNav nav : navList) {
            if (nav.getDailyReturn() != null &&
                    nav.getDailyReturn().abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
                sendRuleBAlert(nav);
            }
        }
    }

    /**
     * 规则C：连续2-3天累计涨跌幅绝对值≥5%
     * <p>
     * 该方法检查基金净值列表中是否存在连续2天或3天的累计涨跌幅绝对值
     * 达到或超过5%的情况。分别对连续2天和连续3天的情况进行检测。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     * <p>
     * 检测逻辑：
     * 1. 检查连续2天的累计涨跌幅是否≥5%
     * 2. 检查连续3天的累计涨跌幅是否≥5%
     * 3. 当发现符合条件的情况时，触发规则C告警
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     */
    private void checkRuleC(List<FundNav> navList) {
        // 检查连续2天
        for (int i = 1; i < navList.size(); i++) {
            FundNav current = navList.get(i);
            FundNav previous = navList.get(i - 1);

            if (current.getDailyReturn() != null && previous.getDailyReturn() != null) {
                BigDecimal sum2Days = current.getDailyReturn().add(previous.getDailyReturn());
                if (sum2Days.abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
                    sendRuleCAlert(current, 2, sum2Days);
                }
            }
        }

        // 检查连续3天
        for (int i = 2; i < navList.size(); i++) {
            FundNav current = navList.get(i);
            FundNav previous1 = navList.get(i - 1);
            FundNav previous2 = navList.get(i - 2);

            if (current.getDailyReturn() != null &&
                    previous1.getDailyReturn() != null &&
                    previous2.getDailyReturn() != null) {
                BigDecimal sum3Days = current.getDailyReturn()
                        .add(previous1.getDailyReturn())
                        .add(previous2.getDailyReturn());
                if (sum3Days.abs().compareTo(THRESHOLD_5_PERCENT) >= 0) {
                    sendRuleCAlert(current, 3, sum3Days);
                }
            }
        }
    }

    /**
     * 发送规则A告警邮件
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
        } catch (Exception e) {
            log.error("发送规则A告警邮件失败", e);
        }
    }

    /**
     * 发送规则B告警邮件
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
        } catch (Exception e) {
            log.error("发送规则B告警邮件失败", e);
        }
    }

    /**
     * 发送规则C告警邮件
     */
    private void sendRuleCAlert(FundNav nav, int days, BigDecimal cumulativeReturn) {
        String subject = String.format("【基金预警-规则C】%s 连续%d天累计波动",
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
            log.info("规则C告警邮件已发送: fundCode={}, days={}, return={}",
                    nav.getFundCode(), days, cumulativeReturn);
        } catch (Exception e) {
            log.error("发送规则C告警邮件失败", e);
        }
    }
}
