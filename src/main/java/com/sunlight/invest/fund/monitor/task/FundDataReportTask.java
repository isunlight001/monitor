package com.sunlight.invest.fund.monitor.task;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.entity.MonitorFund;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.mapper.MonitorFundMapper;
import com.sunlight.invest.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 基金数据报告定时任务
 * 每日发送维护基金的近5日数据
 *
 * @author System
 * @since 2024-12-06
 */
@Component
public class FundDataReportTask {

    private static final Logger log = LoggerFactory.getLogger(FundDataReportTask.class);
    
    @Autowired
    private MonitorFundMapper monitorFundMapper;
    
    @Autowired
    private FundNavMapper fundNavMapper;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每日8:30发送基金数据报告
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void sendFundDataReport() {
        try {
            log.info("开始执行基金数据报告任务");
            
            // 获取所有启用的监控基金
            List<MonitorFund> monitorFunds = monitorFundMapper.selectAllEnabled();
            if (monitorFunds.isEmpty()) {
                log.info("没有启用的监控基金，跳过发送报告");
                return;
            }
            
            // 构建HTML邮件内容
            String htmlContent = buildFundDataReport(monitorFunds);
            
            // 发送邮件
            String subject = String.format("【基金数据日报】%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            emailNotificationService.sendHtmlEmailToAllRecipients(subject, htmlContent);
            
            log.info("基金数据报告发送完成，共处理 {} 个基金", monitorFunds.size());
        } catch (Exception e) {
            log.error("发送基金数据报告失败", e);
        }
    }
    
    /**
     * 构建基金数据报告HTML内容
     */
    private String buildFundDataReport(List<MonitorFund> monitorFunds) {
        StringBuilder htmlBuilder = new StringBuilder();
        
        // HTML头部
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<title>基金数据日报</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }");
        htmlBuilder.append(".container { max-width: 1000px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        htmlBuilder.append("h1 { color: #1976d2; text-align: center; border-bottom: 2px solid #1976d2; padding-bottom: 10px; }");
        htmlBuilder.append("h2 { color: #388e3c; margin-top: 30px; border-bottom: 1px dashed #388e3c; padding-bottom: 5px; }");
        htmlBuilder.append(".summary { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin-bottom: 20px; text-align: center; }");
        htmlBuilder.append(".fund-section { background-color: #fafafa; padding: 20px; margin-bottom: 30px; border-radius: 8px; border-left: 5px solid #1976d2; }");
        htmlBuilder.append(".data-table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        htmlBuilder.append(".data-table th, .data-table td { border: 1px solid #ddd; padding: 12px; text-align: center; }");
        htmlBuilder.append(".data-table th { background-color: #1976d2; color: white; }");
        htmlBuilder.append(".data-table tr:nth-child(even) { background-color: #f2f2f2; }");
        htmlBuilder.append(".positive { color: #d32f2f; font-weight: bold; }");
        htmlBuilder.append(".negative { color: #388e3c; font-weight: bold; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 30px; color: #757575; font-size: 14px; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class='container'>");
        htmlBuilder.append("<h1>基金数据日报</h1>");
        
        // 报告摘要
        htmlBuilder.append("<div class='summary'>");
        htmlBuilder.append("<h2>报告摘要</h2>");
        htmlBuilder.append("<p>报告日期: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))).append("</p>");
        htmlBuilder.append("<p>监控基金数量: ").append(monitorFunds.size()).append(" 只</p>");
        htmlBuilder.append("</div>");
        
        // 每个基金的数据
        for (MonitorFund fund : monitorFunds) {
            htmlBuilder.append("<div class='fund-section'>");
            htmlBuilder.append("<h2>").append(fund.getFundName()).append(" (").append(fund.getFundCode()).append(")</h2>");
            
            // 获取近5日数据
            List<FundNav> recentNavs = fundNavMapper.selectRecentDays(fund.getFundCode(), 5);
            
            if (recentNavs == null || recentNavs.isEmpty()) {
                htmlBuilder.append("<p>暂无数据</p>");
            } else {
                // 构建数据表格
                htmlBuilder.append("<table class='data-table'>");
                htmlBuilder.append("<thead>");
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<th>日期</th>");
                htmlBuilder.append("<th>单位净值</th>");
                htmlBuilder.append("<th>日涨跌幅</th>");
                htmlBuilder.append("</tr>");
                htmlBuilder.append("</thead>");
                htmlBuilder.append("<tbody>");
                
                // 按日期升序排列显示
                for (int i = recentNavs.size() - 1; i >= 0; i--) {
                    FundNav nav = recentNavs.get(i);
                    htmlBuilder.append("<tr>");
                    htmlBuilder.append("<td>").append(nav.getNavDate().format(DATE_FORMATTER)).append("</td>");
                    htmlBuilder.append("<td>").append(String.format("%.4f", nav.getUnitNav())).append("</td>");
                    
                    if (nav.getDailyReturn() != null) {
                        String returnStr = String.format("%.2f%%", nav.getDailyReturn());
                        String cssClass = nav.getDailyReturn().compareTo(BigDecimal.ZERO) > 0 ? "positive" : 
                                         nav.getDailyReturn().compareTo(BigDecimal.ZERO) < 0 ? "negative" : "";
                        htmlBuilder.append("<td class='").append(cssClass).append("'>").append(returnStr).append("</td>");
                    } else {
                        htmlBuilder.append("<td>--</td>");
                    }
                    
                    htmlBuilder.append("</tr>");
                }
                
                htmlBuilder.append("</tbody>");
                htmlBuilder.append("</table>");
            }
            
            htmlBuilder.append("</div>");
        }
        
        htmlBuilder.append("<div class='footer'>");
        htmlBuilder.append("本报告由基金监控系统自动生成，请勿直接回复。<br>");
        htmlBuilder.append("数据来源：天天基金网");
        htmlBuilder.append("</div>");
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");
        
        return htmlBuilder.toString();
    }
}