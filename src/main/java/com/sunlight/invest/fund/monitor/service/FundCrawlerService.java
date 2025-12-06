package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基金数据爬取服务
 * <p>
 * 从http://www.dayfund.cn获取基金净值数据
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@Service
public class FundCrawlerService {

    private static final Logger log = LoggerFactory.getLogger(FundCrawlerService.class);

    private static final String URL_TEMPLATE = "http://www.dayfund.cn/fundvalue/{fund_code}.html?sdate={startDate}&edate={endDate}";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private FundNavMapper fundNavMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    /**
     * 爬取指定基金的净值数据
     *
     * @param fundCode  基金代码
     * @param fundName  基金名称
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 基金净值列表
     */
    public List<FundNav> crawlFundNav(String fundCode, String fundName, LocalDate startDate, LocalDate endDate) {
        log.info("开始爬取基金数据: fundCode={}, startDate={}, endDate={}", fundCode, startDate, endDate);

        String url = URL_TEMPLATE
                .replace("{fund_code}", fundCode)
                .replace("{startDate}", startDate.format(DATE_FORMATTER))
                .replace("{endDate}", endDate.format(DATE_FORMATTER));

        try {
            String html = fetchHtml(url);
            List<FundNav> navList = parseHtml(html, fundCode, fundName);
            log.info("成功爬取基金数据: fundCode={}, count={}", fundCode, navList.size());
            return navList;
        } catch (Exception e) {
            log.error("爬取基金数据失败: fundCode={}, error={}", fundCode, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取HTML内容
     */
    private String fetchHtml(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP请求失败: " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * 解析HTML获取净值数据
     */
    private List<FundNav> parseHtml(String html, String fundCode, String fundName) {
        List<FundNav> navList = new ArrayList<>();

        try {
            // 使用正则表达式匹配表格行
            Pattern rowPattern = Pattern.compile("<tr[^>]*class=\"row[12]\"[^>]*>(.*?)</tr>", Pattern.DOTALL);
            Matcher rowMatcher = rowPattern.matcher(html);
            
            BigDecimal lastNav = null;
            List<FundNav> tempList = new ArrayList<>();
            
            while (rowMatcher.find()) {
                String rowContent = rowMatcher.group(1);
                
                // 匹配所有<td>标签
                Pattern cellPattern = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.DOTALL);
                Matcher cellMatcher = cellPattern.matcher(rowContent);
                
                List<String> cells = new ArrayList<>();
                while (cellMatcher.find()) {
                    String cellContent = cellMatcher.group(1).trim();
                    // 清理HTML标签和特殊字符
                    cellContent = cellContent.replaceAll("<[\\s\\S]*?>", "").trim();
                    cellContent = cellContent.replace("&nbsp;", " ");
                    cells.add(cellContent);
                }
                
                // 确保有足够的列 (至少9列)
                if (cells.size() >= 9) {
                    String dateStr = cells.get(0);      // 净值日期
                    String navStr = cells.get(3);       // 最新单位净值
                    String growthStr = cells.get(8);    // 当日增长率
                    
                    // 数据验证
                    if (!dateStr.isEmpty() && dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        try {
                            LocalDate navDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                            BigDecimal unitNav = new BigDecimal(navStr);
                            
                            // 处理增长率，去除百分号
                            BigDecimal dailyReturn = null;
                            if (growthStr != null && !growthStr.isEmpty()) {
                                growthStr = growthStr.replace("%", "").trim();
                                if (!growthStr.isEmpty() && !"--".equals(growthStr) && !"null".equals(growthStr.toLowerCase())) {
                                    try {
                                        dailyReturn = new BigDecimal(growthStr);
                                    } catch (NumberFormatException e) {
                                        log.warn("无法解析增长率: {}", growthStr);
                                    }
                                }
                            }
                            
                            FundNav fundNav = new FundNav(fundCode, fundName, navDate, unitNav, dailyReturn);
                            tempList.add(fundNav);
                            
                            lastNav = unitNav;
                        } catch (Exception e) {
                            log.warn("解析行数据失败: date={}, nav={}, growth={}", dateStr, navStr, growthStr, e);
                        }
                    }
                }
            }
            
            // 反转列表，使其按日期升序排列
            for (int i = tempList.size() - 1; i >= 0; i--) {
                navList.add(tempList.get(i));
            }
            
            // 如果没有解析到增长率，则重新计算日涨跌幅
            boolean hasDailyReturn = navList.stream()
                    .anyMatch(nav -> nav.getDailyReturn() != null && 
                           nav.getDailyReturn().compareTo(BigDecimal.ZERO) != 0);
            
            if (!hasDailyReturn && navList.size() > 1) {
                recalculateDailyReturn(navList);
            }
            
        } catch (Exception e) {
            log.error("解析HTML失败", e);
        }

        return navList;
    }

    /**
     * 重新计算日涨跌幅
     */
    private void recalculateDailyReturn(List<FundNav> navList) {
        for (int i = 0; i < navList.size(); i++) {
            if (i == 0) {
                navList.get(i).setDailyReturn(BigDecimal.ZERO);
            } else {
                FundNav current = navList.get(i);
                FundNav previous = navList.get(i - 1);

                if (previous.getUnitNav().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal dailyReturn = current.getUnitNav()
                            .subtract(previous.getUnitNav())
                            .divide(previous.getUnitNav(), 6, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    current.setDailyReturn(dailyReturn);
                } else {
                    current.setDailyReturn(BigDecimal.ZERO);
                }
            }
        }
    }

    /**
     * 检查是否需要爬取数据
     * 
     * @param fundCode 基金代码
     * @param endDate  结束日期
     * @return true表示需要爬取，false表示不需要爬取
     */
    public boolean shouldCrawl(String fundCode, LocalDate endDate) {
        // 首先检查是否有最新的数据
        FundNav latestNav = fundNavMapper.selectLatest(fundCode);
        
        // 如果没有最新数据，则需要爬取
        if (latestNav == null) {
            return true;
        }
        
        // 如果有最新数据，检查数据是否足够新
        LocalDate latestDate = latestNav.getNavDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 如果最新数据是今天或昨天，则不需要重新爬取
        if (latestDate.equals(today) || latestDate.equals(yesterday)) {
            log.info("基金{}已有最新数据，无需重新爬取: latestDate={}", fundCode, latestDate);
            return false;
        }
        
        // 如果最新数据是前天或更早，但结束日期早于最新数据日期，则也不需要爬取
        if (endDate.isBefore(latestDate)) {
            log.info("基金{}的结束日期早于最新数据日期，无需爬取: endDate={}, latestDate={}", 
                     fundCode, endDate, latestDate);
            return false;
        }
        
        // 其他情况下需要爬取
        return true;
    }

    /**
     * 爬取并保存基金数据到数据库
     *
     * @param fundCode  基金代码
     * @param fundName  基金名称
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 保存成功的记录数
     */
    public int crawlAndSave(String fundCode, String fundName, LocalDate startDate, LocalDate endDate) {
        // 检查是否需要爬取数据
        if (!shouldCrawl(fundCode, endDate)) {
            return 0;
        }
        
        // 如果需要爬取，则进行爬取
        List<FundNav> navList = crawlFundNav(fundCode, fundName, startDate, endDate);
        if (navList.isEmpty()) {
            log.warn("未爬取到数据: fundCode={}", fundCode);
            return 0;
        }

        int count = fundNavMapper.batchInsert(navList);
        log.info("保存基金数据成功: fundCode={}, count={}", fundCode, count);
        return count;
    }

    /**
     * 增量更新基金数据（最近1个月）
     *
     * @param fundCode 基金代码
     * @param fundName 基金名称
     * @return 更新记录数
     */
    public int incrementalUpdate(String fundCode, String fundName) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        log.info("开始增量更新基金数据: fundCode={}, startDate={}, endDate={}",
                fundCode, startDate, endDate);

        return crawlAndSave(fundCode, fundName, startDate, endDate);
    }
}
