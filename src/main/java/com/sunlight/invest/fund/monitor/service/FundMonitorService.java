package com.sunlight.invest.fund.monitor.service;

import com.sunlight.ai.service.DeepSeekService;
import com.sunlight.invest.fund.monitor.entity.AlarmRecord;
import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.entity.MonitorFund;
import com.sunlight.invest.fund.monitor.mapper.AlarmRecordMapper;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.mapper.MonitorFundMapper;
import com.sunlight.invest.notification.service.EmailNotificationService;
import com.sunlight.invest.system.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金监控服务类
 * <p>
 * 提供基金监控相关的业务逻辑处理，包括：
 * - 规则A：检测连续5天或以上上涨/下跌
 * - 规则B：单日涨跌幅绝对值≥5%
 * - 规则C：连续2天累计涨跌幅绝对值≥4%
 * - 规则D：连续3天累计涨跌幅绝对值≥3%
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
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private DeepSeekService deepSeekService;
    
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
    
    // 添加日期格式化器作为成员变量
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Scheduled(cron = "#{systemConfigService.scheduleCron}")
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

        // 从配置服务获取监控天数
        int monitorDays = systemConfigService.getMonitorDays();
        
        // 获取最近配置天数的数据 降序
        List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, monitorDays);
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

        // 从配置服务获取监控天数
        int monitorDays = systemConfigService.getMonitorDays();
        
        // 获取最近配置天数的数据 降序
        List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, monitorDays);
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
     * 3. 当连续天数达到配置的天数时，触发规则A告警
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
        
        // 从配置服务获取阈值
        BigDecimal threshold5Percent = systemConfigService.getThreshold5Percent();
        int monitorDays = systemConfigService.getMonitorDays();

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
                if (consecutiveDays >= monitorDays) { // 使用配置的监控天数
                    alerts.add(createRuleAAlertInfo(navList.get(i - 1), consecutiveDays, cumulativeReturn, isRising));
                }
                // 重置
                consecutiveDays = 1;
                cumulativeReturn = dailyReturn;
                isRising = currentIsRising;
            }
        }

        // 检查最后的连续序列
        if (consecutiveDays >= monitorDays) { // 使用配置的监控天数
            alerts.add(createRuleAAlertInfo(navList.get(navList.size() - 1), consecutiveDays, cumulativeReturn, isRising));
        }
    }

    /**
     * 规则B：单日涨跌幅绝对值≥配置的阈值
     * <p>
     * 该方法检查基金净值列表中是否存在单日涨跌幅绝对值达到或超过配置阈值的情况。
     * 当发现符合条件的记录时，会立即触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleB(List<FundNav> navList, List<AlertInfo> alerts) {
        // 从配置服务获取阈值
        BigDecimal threshold5Percent = systemConfigService.getThreshold5Percent();
        
        if (navList.get(0).getDailyReturn() != null &&
                navList.get(0).getDailyReturn().abs().compareTo(threshold5Percent) >= 0) {
            alerts.add(createRuleBAlertInfo(navList.get(0)));
        }
    }

    /**
     * 规则C：连续2天累计涨跌幅绝对值≥配置的阈值
     * <p>
     * 该方法检查基金净值列表中是否存在连续2天的累计涨跌幅绝对值
     * 达到或超过配置阈值的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleC(List<FundNav> navList, List<AlertInfo> alerts) {
        // 从配置服务获取阈值
        BigDecimal threshold4Percent = systemConfigService.getThreshold4Percent();
        
        // 检查连续2天
//        for (int i = 0; i < navList.size(); i++) {
        FundNav current = navList.get(0);
        FundNav previous = navList.get(1);

        if (current.getDailyReturn() != null && previous.getDailyReturn() != null) {
            BigDecimal sum2Days = current.getDailyReturn().add(previous.getDailyReturn());
            if (sum2Days.abs().compareTo(threshold4Percent) >= 0) { // 使用配置的4%阈值
                alerts.add(createRuleCAlertInfo(current, 2, sum2Days));
            }
        }
    }

    /**
     * 规则D：连续3天累计涨跌幅绝对值≥3%
     * <p>
     * 该方法检查基金净值列表中是否存在连续3天的累计涨跌幅绝对值
     * 达到或超过3%的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleD(List<FundNav> navList, List<AlertInfo> alerts) {
        // 检查连续3天
        FundNav current = navList.get(0);
        FundNav previous = navList.get(1);
        FundNav previous2 = navList.get(2);

        if (current.getDailyReturn() != null && previous.getDailyReturn() != null && previous2.getDailyReturn() != null) {
            BigDecimal sum3Days = current.getDailyReturn().add(previous.getDailyReturn()).add(previous2.getDailyReturn());
            if (sum3Days.abs().compareTo(new BigDecimal("3.0")) >= 0) {
                alerts.add(createRuleDAlertInfo(current, 3, sum3Days));
            }
        }
    }

    /**
     * 规则E：连续4天累计涨跌幅绝对值≥配置的阈值
     * <p>
     * 该方法检查基金净值列表中是否存在连续4天的累计涨跌幅绝对值
     * 达到或超过配置阈值的情况。
     * 当发现符合条件的记录时，会触发告警邮件通知。
     * </p>
     *
     * @param navList 基金净值列表，按日期升序排列
     * @param alerts  预警信息收集列表
     */
    private void checkRuleE(List<FundNav> navList, List<AlertInfo> alerts) {
        // 从配置服务获取阈值
        BigDecimal threshold5Percent = systemConfigService.getThreshold5Percent();
        
        // 检查连续4天
        FundNav current = navList.get(0);
        FundNav previous = navList.get(1);
        FundNav previous2 = navList.get(2);
        FundNav previous3 = navList.get(3);

        if (current.getDailyReturn() != null && previous.getDailyReturn() != null && 
                previous2.getDailyReturn() != null && previous3.getDailyReturn() != null) {
            BigDecimal sum4Days = current.getDailyReturn().add(previous.getDailyReturn())
                    .add(previous2.getDailyReturn()).add(previous3.getDailyReturn());
            if (sum4Days.abs().compareTo(threshold5Percent) >= 0) { // 使用配置的5%阈值
                alerts.add(createRuleEAlertInfo(current, 4, sum4Days));
            }
        }
    }

    /**
     * 创建规则A的预警信息
     */
    private AlertInfo createRuleAAlertInfo(FundNav nav, int consecutiveDays, BigDecimal cumulativeReturn, boolean isRising) {
        String subject = String.format("【基金预警-规则A】%s 连续%d天%s",
                nav.getFundName(), consecutiveDays, isRising ? "上涨" : "下跌");

        String content = String.format(
                "基金名称: %s\n" +
                        "基金代码: %s\n" +
                        "预警规则: 连续%d天持续%s\n" +
                        "连续天数: %d天\n" +
                        "累计涨跌幅: %.2f%%\n" +
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "%s趋势持续，请关注后续走势。",
                nav.getFundName(),
                nav.getFundCode(),
                consecutiveDays,
                isRising ? "上涨" : "下跌",
                consecutiveDays,
                cumulativeReturn.doubleValue(),
                nav.getNavDate().format(DATE_FORMATTER),
                nav.getUnitNav().doubleValue(),
                isRising ? "上涨" : "下跌"
        );

        // 创建告警记录
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setFundCode(nav.getFundCode());
        alarmRecord.setFundName(nav.getFundName());
        alarmRecord.setRuleCode("A");
        alarmRecord.setRuleDescription("连续" + consecutiveDays + "天持续" + (isRising ? "上涨" : "下跌"));
        alarmRecord.setConsecutiveDays(consecutiveDays);
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
                        "最新净值日期: %s\n" +
                        "最新单位净值: %.4f\n" +
                        "\n" +
                        "单日波动较大，请关注基金走势。",
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
                        "预警规则: 连续%d天累计涨跌幅绝对值≥4%%\n" +
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
                        "预警规则: 连续%d天累计涨跌幅绝对值≥3%%\n" +
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
        alarmRecord.setRuleDescription("连续3天累计涨跌幅绝对值≥3%");
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
        
        // 构建美化后的HTML邮件内容
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<title>基金预警汇总</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }");
        htmlBuilder.append(".container { max-width: 800px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        htmlBuilder.append("h1 { color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 10px; }");
        htmlBuilder.append("h2 { color: #1976d2; margin-top: 30px; }");
        htmlBuilder.append(".fund-info { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
        htmlBuilder.append(".alert-summary { background-color: #fff3e0; padding: 15px; border-radius: 5px; margin-bottom: 20px; font-weight: bold; }");
        htmlBuilder.append(".alert-item { background-color: #fafafa; border-left: 4px solid #1976d2; padding: 15px; margin-bottom: 15px; border-radius: 5px; }");
        htmlBuilder.append(".alert-item.warning { border-left-color: #f57c00; }");
        htmlBuilder.append(".alert-item.critical { border-left-color: #d32f2f; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 30px; color: #757575; font-size: 14px; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class='container'>");
        htmlBuilder.append("<h1>基金预警汇总报告</h1>");
        
        // 基金基本信息
        htmlBuilder.append("<div class='fund-info'>");
        htmlBuilder.append("<strong>基金名称:</strong> ").append(fundName).append("<br>");
        htmlBuilder.append("<strong>基金代码:</strong> ").append(fundCode);
        htmlBuilder.append("</div>");
        
        // 预警摘要
        htmlBuilder.append("<div class='alert-summary'>");
        htmlBuilder.append("共发现 <span style='color: #d32f2f; font-size: 18px;'>").append(alerts.size()).append("</span> 个预警事件");
        htmlBuilder.append("</div>");
        
        // 添加每个预警的详细信息
        for (int i = 0; i < alerts.size(); i++) {
            AlertInfo alert = alerts.get(i);
            String alertClass = "";
            if (alert.getSubject().contains("规则A")) {
                alertClass = " critical";
            } else if (alert.getSubject().contains("规则B") || alert.getSubject().contains("规则C")) {
                alertClass = " warning";
            }
            
            htmlBuilder.append("<div class='alert-item").append(alertClass).append("'>");
            htmlBuilder.append("<h2>预警 #").append(i + 1).append("</h2>");
            
            // 解析原始内容并转换为HTML格式
            String[] lines = alert.getContent().split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    htmlBuilder.append("<br>");
                } else if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    htmlBuilder.append("<strong>").append(parts[0].trim()).append(":</strong> ").append(parts.length > 1 ? parts[1].trim() : "").append("<br>");
                } else {
                    htmlBuilder.append(line).append("<br>");
                }
            }
            
            htmlBuilder.append("</div>");
        }
        
        htmlBuilder.append("<div class='footer'>");
        htmlBuilder.append("请关注基金波动情况。<br>");
        htmlBuilder.append("本邮件由基金监控系统自动发送，请勿直接回复。");
        htmlBuilder.append("</div>");
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        String htmlContent = htmlBuilder.toString();

        try {
            // 发送集中预警HTML邮件给所有接收人
            emailNotificationService.sendHtmlEmailToAllRecipients(subject, htmlContent);
            log.info("基金 {} 预警汇总HTML邮件已发送，共 {} 个预警", fundCode, alerts.size());

            // 保存所有告警记录
            for (AlertInfo alert : alerts) {
                alarmRecordMapper.insert(alert.getAlarmRecord());
            }
        } catch (Exception e) {
            log.error("发送基金 {} 预警汇总HTML邮件失败", fundCode, e);
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
        
        // 构建美化后的HTML邮件内容
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<title>基金预警汇总</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }");
        htmlBuilder.append(".container { max-width: 1000px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        htmlBuilder.append("h1 { color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 10px; }");
        htmlBuilder.append("h2 { color: #1976d2; margin-top: 30px; border-bottom: 1px dashed #1976d2; padding-bottom: 5px; }");
        htmlBuilder.append("h3 { color: #388e3c; margin-top: 20px; }");
        htmlBuilder.append(".summary { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin-bottom: 20px; text-align: center; }");
        htmlBuilder.append(".alert-summary { background-color: #fff3e0; padding: 15px; border-radius: 5px; margin-bottom: 20px; font-weight: bold; text-align: center; }");
        htmlBuilder.append(".fund-section { background-color: #fafafa; padding: 20px; margin-bottom: 30px; border-radius: 8px; border-left: 5px solid #1976d2; }");
        htmlBuilder.append(".alert-item { background-color: #ffffff; border: 1px solid #e0e0e0; border-left: 4px solid #1976d2; padding: 15px; margin-bottom: 15px; border-radius: 5px; }");
        htmlBuilder.append(".alert-item.warning { border-left-color: #f57c00; }");
        htmlBuilder.append(".alert-item.critical { border-left-color: #d32f2f; }");
        htmlBuilder.append(".ai-analysis { background-color: #f1f8e9; padding: 20px; border-radius: 5px; margin-top: 20px; border-left: 4px solid #388e3c; }");
        htmlBuilder.append(".ai-title { color: #388e3c; font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
        htmlBuilder.append(".ai-content { line-height: 1.6; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 30px; color: #757575; font-size: 14px; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class='container'>");
        htmlBuilder.append("<h1>全局基金预警汇总报告</h1>");
        
        // 摘要信息
        htmlBuilder.append("<div class='summary'>");
        htmlBuilder.append("<h2>报告摘要</h2>");
        htmlBuilder.append("<p>本次监控共扫描了所有启用的基金，发现了需要关注的预警信号。</p>");
        htmlBuilder.append("</div>");
        
        // 预警摘要
        htmlBuilder.append("<div class='alert-summary'>");
        htmlBuilder.append("共发现 <span style='color: #d32f2f; font-size: 24px;'>").append(allAlerts.size()).append("</span> 个预警事件");
        htmlBuilder.append("</div>");
        
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
                // 获取基金名称
                String fundName = fundAlerts.get(0).getAlarmRecord().getFundName();
                
                htmlBuilder.append("<div class='fund-section'>");
                htmlBuilder.append("<h2>基金代码: ").append(fundCode).append(" (").append(fundName).append(")</h2>");
                
                for (int i = 0; i < fundAlerts.size(); i++) {
                    AlertInfo alert = fundAlerts.get(i);
                    String alertClass = "";
                    if (alert.getSubject().contains("规则A")) {
                        alertClass = " critical";
                    } else if (alert.getSubject().contains("规则B") || alert.getSubject().contains("规则C")) {
                        alertClass = " warning";
                    }
                    
                    htmlBuilder.append("<div class='alert-item").append(alertClass).append("'>");
                    htmlBuilder.append("<h3>预警 #").append(i + 1).append("</h3>");
                    
                    // 解析原始内容并转换为HTML格式
                    String[] lines = alert.getContent().split("\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) {
                            htmlBuilder.append("<br>");
                        } else if (line.contains(":")) {
                            String[] parts = line.split(":", 2);
                            htmlBuilder.append("<strong>").append(parts[0].trim()).append(":</strong> ").append(parts.length > 1 ? parts[1].trim() : "").append("<br>");
                        } else {
                            htmlBuilder.append(line).append("<br>");
                        }
                    }
                    
                    htmlBuilder.append("</div>");
                }
                
                // 为每个基金添加AI智能分析报告
                try {
                    List<FundNav> fundNavList = fundNavMapper.selectRecentDays(fundCode, 30);
                    String aiAnalysis = generateFundAIAnalysis(fundNavList, fundCode, fundName);
                    
                    htmlBuilder.append("<div class='ai-analysis'>");
                    htmlBuilder.append("<div class='ai-title'>🤖 AI智能分析报告</div>");
                    htmlBuilder.append("<div class='ai-content'>");
                    htmlBuilder.append("<pre>").append(aiAnalysis.replace("<", "&lt;").replace(">", "&gt;")).append("</pre>");
                    htmlBuilder.append("</div>");
                    htmlBuilder.append("</div>");
                } catch (Exception e) {
                    log.error("生成基金AI分析报告失败，基金代码: {}", fundCode, e);
                    htmlBuilder.append("<div class='ai-analysis'>");
                    htmlBuilder.append("<div class='ai-title'>🤖 AI智能分析报告</div>");
                    htmlBuilder.append("<div class='ai-content'>");
                    htmlBuilder.append("<pre>生成AI分析报告时出现错误：" + e.getMessage() + "</pre>");
                    htmlBuilder.append("</div>");
                    htmlBuilder.append("</div>");
                }
                
                htmlBuilder.append("</div>");
            }
        }
        
        htmlBuilder.append("<div class='footer'>");
        htmlBuilder.append("请关注基金波动情况。<br>");
        htmlBuilder.append("本邮件由基金监控系统自动发送，请勿直接回复。");
        htmlBuilder.append("</div>");
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        String htmlContent = htmlBuilder.toString();

        try {
            // 发送全局预警HTML邮件给所有接收人
            emailNotificationService.sendHtmlEmailToAllRecipients(subject, htmlContent);
            log.info("全局基金预警汇总HTML邮件已发送，共 {} 个预警", allAlerts.size());

            // 保存所有告警记录
            for (AlertInfo alert : allAlerts) {
                alarmRecordMapper.insert(alert.getAlarmRecord());
            }
        } catch (Exception e) {
            log.error("发送全局基金预警汇总HTML邮件失败", e);
        }
    }
    
    /**
     * 生成基金AI分析报告
     * 
     * @param fundNavList 基金净值数据列表
     * @param fundCode 基金代码
     * @param fundName 基金名称
     * @return AI分析报告
     */
    public String generateFundAIAnalysis(List<FundNav> fundNavList, String fundCode, String fundName) {
        if (fundNavList == null || fundNavList.isEmpty()) {
            return "无法生成分析报告：没有基金数据";
        }
        
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("角色：你是一名经验丰富的基金投资分析专家，擅长技术面和基本面结合分析。\n");
        prompt.append("任务：请分析以下基金的历史数据，基金代码：").append(fundCode).append("，基金名称：").append(fundName).append("。\n");
        prompt.append("数据格式为：每行包含\"净值日期、单位净值、日涨跌幅\"。\n");
        prompt.append("数据：\n");
        
        // 添加数据（最多取最近30天的数据）
        int count = 0;
        for (FundNav nav : fundNavList) {
            if (count >= 30) break;
            prompt.append(nav.getNavDate().toString()).append("、")
                  .append(nav.getUnitNav().toString()).append("、")
                  .append(nav.getDailyReturn() != null ? nav.getDailyReturn().toString() : "0").append("\n");
            count++;
        }
        
        prompt.append("要求：请按以下结构输出分析报告：\n");
        prompt.append("趋势判断：当前处于上升、下降还是震荡趋势？\n");
        prompt.append("波动特征：近期基金的波动性如何？\n");
        prompt.append("风险评估：当前基金的主要风险点是什么？\n");
        prompt.append("投资建议：给出短期（1-2周）的投资策略建议（如持有、加仓、减仓）及理由。\n");
        
        try {
            // 调用AI服务
            return deepSeekService.getAIResponse(prompt.toString());
        } catch (Exception e) {
            log.error("生成基金AI分析报告失败，基金代码: {}, 错误: {}", fundCode, e.getMessage(), e);
            return "生成分析报告失败：" + e.getMessage();
        }
    }
}