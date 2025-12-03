package com.sunlight.invest.fund.backtest.service;

import com.sunlight.invest.fund.backtest.dto.BacktestRequest;
import com.sunlight.invest.fund.backtest.dto.BacktestResponse;
import com.sunlight.invest.fund.export.GsNavHtmlToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基金回测服务
 * 
 * @author system
 * @date 2025-12-01
 */
@Service
public class FundBacktestService {
    
    private static final Logger logger = LoggerFactory.getLogger(FundBacktestService.class);

    /**
     * 执行回测
     */
    public BacktestResponse runBacktest(BacktestRequest request) {
        logger.info("开始执行回测业务逻辑...");
        try {
            // 获取基金净值数据
            logger.info("步骤1: 获取基金净值数据...");
            List<GsNavHtmlToExcel.Nav> fundDataList = GsNavHtmlToExcel.fetchTable();
            logger.info("获取到基金数据 {} 条", fundDataList.size());
            
            // 过滤出指定月数或日期区间的基金数据
            LocalDate endDate;
            LocalDate startDate;
            
            // 优先使用日期区间，其次使用月数
            if (request.getStartDate() != null && request.getEndDate() != null) {
                startDate = request.getStartDate();
                endDate = request.getEndDate();
                logger.info("步骤2: 使用日期区间 {} 至 {}", startDate, endDate);
            } else {
                endDate = LocalDate.now();
                startDate = endDate.minusMonths(request.getBacktestMonths());
                logger.info("步骤2: 过滤最近 {} 个月的数据 ({} 至 {})", 
                    request.getBacktestMonths(), startDate, endDate);
            }
            
            fundDataList = fundDataList.stream()
                    .filter(nav -> !nav.getDate().isBefore(startDate) && !nav.getDate().isAfter(endDate))
                    .sorted(Comparator.comparing(GsNavHtmlToExcel.Nav::getDate))
                    .collect(Collectors.toList());
            
            logger.info("筛选后的基金数据: {} 条 (日期范围: {} 至 {})", 
                fundDataList.size(), startDate, endDate);
            
            if (fundDataList.isEmpty()) {
                throw new RuntimeException("没有找到符合条件的基金数据");
            }
            
            // 模拟指数数据（实际应从数据源获取）
            logger.info("步骤3: 生成模拟指数数据...");
            List<IndexData> indexDataList = generateMockIndexData(startDate, endDate);
            logger.info("生成指数数据: {} 条", indexDataList.size());
            
            // 执行回测
            logger.info("步骤4: 执行回测算法...");
            return performBacktest(request, indexDataList, fundDataList);
            
        } catch (Exception e) {
            logger.error("回测执行失败", e);
            throw new RuntimeException("回测执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行回测核心逻辑
     */
    private BacktestResponse performBacktest(BacktestRequest request,
                                            List<IndexData> indexDataList,
                                            List<GsNavHtmlToExcel.Nav> fundDataList) {
        
        logger.info("进入回测核心逻辑...");
        
        // 将指数数据转换为Map便于查找
        Map<LocalDate, IndexData> indexDataMap = indexDataList.stream()
                .collect(Collectors.toMap(IndexData::getDate, data -> data));
        
        // 将基金数据转换为Map便于查找
        Map<LocalDate, GsNavHtmlToExcel.Nav> fundDataMap = fundDataList.stream()
                .collect(Collectors.toMap(GsNavHtmlToExcel.Nav::getDate, nav -> nav));
        
        // 获取共同的交易日期并排序
        Set<LocalDate> commonDates = new HashSet<>(indexDataMap.keySet());
        commonDates.retainAll(fundDataMap.keySet());
        List<LocalDate> sortedDates = new ArrayList<>(commonDates);
        sortedDates.sort(LocalDate::compareTo);
        
        if (sortedDates.isEmpty()) {
            logger.error("没有找到共同的交易日期");
            throw new RuntimeException("没有找到共同的交易日期");
        }
        
        logger.info("找到 {} 个共同交易日", sortedDates.size());
        
        double capital = request.getInitialCapital();
        double holdings = request.getInitialHoldings() / fundDataMap.get(sortedDates.get(0)).getNav();
        int upPositionChanges = 0;
        int downPositionChanges = 0;
        double peakCapital = request.getInitialCapital() + request.getInitialHoldings();
        double maxDrawdown = 0;
        double peakHoldings = holdings;
        double finalNav = 0;
        
        List<BacktestResponse.DailyDetail> dailyDetails = new ArrayList<>();
        
        // 遍历每个交易日
        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            LocalDate previousDate = sortedDates.get(i - 1);
            
            IndexData currentIndex = indexDataMap.get(currentDate);
            GsNavHtmlToExcel.Nav currentNav = fundDataMap.get(currentDate);
            IndexData previousIndex = indexDataMap.get(previousDate);
            
            if (currentIndex == null || currentNav == null || previousIndex == null) {
                continue;
            }
            
            double previousChange = previousIndex.getChangePercent();
            String action = "持有";
            
            // 根据前一天上证指数涨跌幅调整仓位
            if (previousChange > request.getUpThreshold()) {
                // 减仓
                double sellShares = Math.min(request.getDownPositionChange() / currentNav.getNav(), holdings);
                if (sellShares > 0) {
                    holdings -= sellShares;
                    capital += sellShares * currentNav.getNav();
                    downPositionChanges++;
                    action = "减仓";
                }
            } else if (previousChange < -request.getDownThreshold()) {
                // 加仓
                double purchaseAmount = Math.min(request.getUpPositionChange(), capital);
                if (purchaseAmount > 0) {
                    double newShares = purchaseAmount / currentNav.getNav();
                    holdings += newShares;
                    capital -= purchaseAmount;
                    upPositionChanges++;
                    action = "加仓";
                }
            }
            
            // 计算当前总资产
            double totalAsset = capital + holdings * currentNav.getNav();
            finalNav = currentNav.getNav();
            
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
            
            // 记录每日明细
            dailyDetails.add(new BacktestResponse.DailyDetail(
                currentDate.toString(),
                previousChange,
                currentNav.getNav(),
                capital,
                holdings,
                totalAsset,
                action
            ));
        }
        
        // 构建响应
        BacktestResponse response = new BacktestResponse();
        response.setInitialCapital(request.getInitialCapital());
        response.setInitialHoldings(request.getInitialHoldings());
        response.setFinalCapital(capital);
        response.setFinalHoldings(holdings);
        response.setFinalNav(finalNav);
        response.setFinalHoldingsValue(holdings * finalNav);
        response.setTotalAssets(capital + holdings * finalNav);
        response.setReturnRate(((capital + holdings * finalNav) / (request.getInitialCapital() + request.getInitialHoldings()) - 1) * 100);
        response.setUpPositionChanges(upPositionChanges);
        response.setDownPositionChanges(downPositionChanges);
        response.setMaxDrawdown(maxDrawdown * 100);
        response.setPeakHoldings(peakHoldings);
        response.setTradingDays(sortedDates.size());
        response.setDailyDetails(dailyDetails);
        
        return response;
    }

    /**
     * 生成模拟指数数据（实际应从API获取）
     */
    private List<IndexData> generateMockIndexData(LocalDate startDate, LocalDate endDate) {
        List<IndexData> dataList = new ArrayList<>();
        Random random = new Random(42);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                double changePercent = random.nextGaussian() * 1.5;
                dataList.add(new IndexData(currentDate, changePercent));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return dataList;
    }

    /**
     * 指数数据内部类
     */
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
}
