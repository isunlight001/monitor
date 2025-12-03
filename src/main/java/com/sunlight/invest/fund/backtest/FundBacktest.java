package com.sunlight.invest.fund.backtest;

import com.sunlight.invest.fund.export.GsNavHtmlToExcel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基金回测示例（基于上证指数阈值调整仓位）
 */
public class FundBacktest {

    // 初始资金
    private static final double INITIAL_CAPITAL = 100000.0; // 10万元现金
    // 初始持仓
    private static final double INITIAL_HOLDINGS = 100000.0; // 10万元持仓
    // 每次调整仓位的金额
    private static final double UP_POSITION_CHANGE = 10000.0; // 1万元
    private static final double DOWN_POSITION_CHANGE = 10000.0; // 1万元
    // 触发条件的涨跌幅阈值
    private static final double UP_THRESHOLD = 2; // 1%
    private static final double DOWN_THRESHOLD = 0.5; // 1%

    public static void main(String[] args) throws Exception {
        FundBacktest backtest = new FundBacktest();
        backtest.runBacktest();
    }

    public void runBacktest() throws Exception {
        // 获取最近一年的上证指数数据
        List<IndexData> indexDataList = loadIndexDataFromCSV("上证指数历史数据.csv");
        
        // 获取基金净值数据
        List<GsNavHtmlToExcel.Nav> fundDataList = GsNavHtmlToExcel.fetchTable();
        
        // 过滤出最近一年的基金数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);
        
        fundDataList = fundDataList.stream()
                .filter(nav -> !nav.getDate().isBefore(startDate) && !nav.getDate().isAfter(endDate))
                .sorted(Comparator.comparing(nav -> nav.getDate()))
                .collect(Collectors.toList());
        
        // 执行回测
        BacktestResult result = performBacktest(indexDataList, fundDataList);
        
        // 输出结果
        System.out.println("=== 回测结果 ===");
        System.out.println("初始资金: " + INITIAL_CAPITAL);
        System.out.println("初始持仓: " + INITIAL_HOLDINGS);
        System.out.println("最终资金: " + result.finalCapital);
        System.out.println("最终持仓: " + (result.finalHoldings * result.finalNav));
        System.out.println("总资产收益率: " + String.format("%.2f%%", ((result.finalCapital + result.finalHoldings * result.finalNav) / (INITIAL_CAPITAL + INITIAL_HOLDINGS) - 1) * 100));
        System.out.println("加仓次数: " + result.upPositionChanges);
        System.out.println("减仓次数: " + result.downPositionChanges);
        System.out.println("最大回撤: " + String.format("%.2f%%", result.maxDrawdown * 100));
        System.out.println("持仓峰值: " + result.peakHoldings);
    }

    private List<IndexData> loadIndexDataFromCSV(String csvFile) throws Exception {
        List<IndexData> indexDataList = new ArrayList<>();
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-M-d");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(",");
                    // 解析日期
                    String dateStr = values[0].replace("\"", "");
                    LocalDate date;
                    
                    // 尝试使用不同的日期格式解析
                    try {
                        date = LocalDate.parse(dateStr, formatter1);
                    } catch (DateTimeParseException e) {
                        date = LocalDate.parse(dateStr, formatter2);
                    }
                    
                    // 解析涨跌幅（第6列）
                    String changePercentStr = values[10].replace("\"", "").replace("%", "");
                    double changePercent = Double.parseDouble(changePercentStr);
                    
                    indexDataList.add(new IndexData(date, changePercent));
                } catch (Exception e) {
                    // 忽略解析错误的行
                    System.err.println("无法解析行: " + line + ", 错误: " + e.getMessage());
                }
            }
        }
        
        return indexDataList;
    }

    private BacktestResult performBacktest(List<IndexData> indexDataList, 
                                          List<GsNavHtmlToExcel.Nav> fundDataList) {
        // 将指数数据转换为Map便于查找
        Map<LocalDate, IndexData> indexDataMap = new HashMap<>();
        for (IndexData indexData : indexDataList) {
            indexDataMap.put(indexData.getDate(), indexData);
        }
        
        // 将基金数据转换为Map便于查找
        Map<LocalDate, GsNavHtmlToExcel.Nav> fundDataMap = new HashMap<>();
        for (GsNavHtmlToExcel.Nav nav : fundDataList) {
            fundDataMap.put(nav.getDate(), nav);
        }
        
        // 获取共同的交易日期并排序
        Set<LocalDate> indexDates = indexDataMap.keySet();
        Set<LocalDate> fundDates = fundDataMap.keySet();
        Set<LocalDate> commonDates = new HashSet<>(indexDates);
        commonDates.retainAll(fundDates);
        List<LocalDate> sortedDates = new ArrayList<>(commonDates);
        sortedDates.sort(LocalDate::compareTo);
        
        double capital = INITIAL_CAPITAL; // 当前资金
        double holdings = INITIAL_HOLDINGS / fundDataMap.get(sortedDates.get(0)).getNav(); // 初始持仓份额
        int upPositionChanges = 0; // 调整仓位次数
        int downPositionChanges = 0; // 调整仓位次数
        double peakCapital = INITIAL_CAPITAL + INITIAL_HOLDINGS; // 历史最高资产净值
        double maxDrawdown = 0; // 最大回撤
        double peakHoldings = holdings; // 持仓峰值
        double finalNav = 0; // 最终净值
        
        System.out.println("开始回测，交易日数量: " + sortedDates.size());
        System.out.println("初始资金: " + capital+holdings + "，初始持仓份额: " + holdings);
        
        // 遍历每个交易日
        for (int i = 1; i < sortedDates.size(); i++) { // 从第二个交易日开始，因为需要前一天的数据做判断
            LocalDate currentDate = sortedDates.get(i);
            LocalDate previousDate = sortedDates.get(i - 1);
            
            // 获取当前日期的数据
            IndexData currentIndex = indexDataMap.get(currentDate);
            GsNavHtmlToExcel.Nav currentNav = fundDataMap.get(currentDate);
            IndexData previousIndex = indexDataMap.get(previousDate);
            
            if (currentIndex == null || currentNav == null || previousIndex == null) {
                continue;
            }
            
            // 计算前一天的涨跌幅
            double previousChange = previousIndex.getChangePercent();
            
            // 根据前一天上证指数涨跌幅调整仓位
            if (previousChange > UP_THRESHOLD) {
                // 上证指数涨幅大于1%，减仓
                double sellShares = Math.min(DOWN_POSITION_CHANGE / currentNav.getNav(), holdings);
                if (sellShares > 0) {
                    holdings -= sellShares;
                    capital += sellShares * currentNav.getNav();
                    downPositionChanges++;
                    System.out.println(currentDate + ": 上证指数涨" + String.format("%.2f", previousChange) + 
                            "%，基金净值:" + currentNav.getNav() + "，减仓" + (sellShares * currentNav.getNav()) + "元，持仓份额:" + String.format("%.2f", holdings));
                }
            } else if (previousChange < -DOWN_THRESHOLD) {
                // 上证指数跌幅大于1%，加仓
                double purchaseAmount = Math.min(UP_POSITION_CHANGE, capital);
                if (purchaseAmount > 0) {
                    double newShares = purchaseAmount / currentNav.getNav();
                    holdings += newShares;
                    capital -= purchaseAmount;
                    upPositionChanges++;
                    System.out.println(currentDate + ": 上证指数跌" + String.format("%.2f", previousChange) + 
                            "%，基金净值:" + currentNav.getNav() + "，加仓" + purchaseAmount + "元，持仓份额:" + String.format("%.2f", holdings));
                }
            }
            
            // 计算当前总资产
            double totalAsset = capital + holdings * currentNav.getNav();
            finalNav = currentNav.getNav(); // 记录最终净值
            
            // 更新最大回撤
            if (totalAsset > peakCapital) {
                peakCapital = totalAsset;
            }
            double drawdown = (peakCapital - totalAsset) / peakCapital;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
            
            // 更新持仓峰值
            if (holdings > peakHoldings) {
                peakHoldings = holdings;
            }
            
//            System.out.println(currentDate + ": 上证指数涨跌:" + String.format("%.2f", currentIndex.getChangePercent()) +
//                    "%，基金净值:" + currentNav.getNav() + "，总资产=" + String.format("%.2f", totalAsset) +
//                    ", 资金=" + String.format("%.2f", capital) + ", 持仓=" + String.format("%.2f", holdings * currentNav.getNav()));
        }
        
        // 最后一天计算总资产
        if (!sortedDates.isEmpty()) {
            LocalDate lastDate = sortedDates.get(sortedDates.size() - 1);
            GsNavHtmlToExcel.Nav lastNav = fundDataMap.get(lastDate);
            if (lastNav != null) {
                double finalCapital = capital + holdings * lastNav.getNav();
                return new BacktestResult(finalCapital, holdings, lastNav.getNav(), upPositionChanges,downPositionChanges, maxDrawdown, peakHoldings);
            }
        }
        
        return new BacktestResult(capital + holdings * finalNav, holdings, finalNav, upPositionChanges,downPositionChanges, maxDrawdown, peakHoldings);
    }

    static class IndexData {
        private LocalDate date;
        private double changePercent;
        
        public IndexData(LocalDate date, double changePercent) {
            this.date = date;
            this.changePercent = changePercent;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public double getChangePercent() {
            return changePercent;
        }
    }

    static class BacktestResult {
        double finalCapital;
        double finalHoldings;
        double finalNav;
        int downPositionChanges;
        int upPositionChanges;
        double maxDrawdown;
        double peakHoldings;

        public BacktestResult(double finalCapital, double finalHoldings, double finalNav, int upPositionChanges,int downPositionChanges, double maxDrawdown, double peakHoldings) {
            this.finalCapital = finalCapital;
            this.finalHoldings = finalHoldings;
            this.finalNav = finalNav;
            this.upPositionChanges = upPositionChanges;
            this.downPositionChanges = downPositionChanges;
            this.maxDrawdown = maxDrawdown;
            this.peakHoldings = peakHoldings;
        }
    }
}
