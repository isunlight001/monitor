package com.sunlight.invest.fund.backtest.service;

import com.sunlight.invest.fund.backtest.dto.BacktestRequest;
import com.sunlight.invest.fund.backtest.dto.BacktestResponse;
import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.entity.IndexData;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.mapper.IndexDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private FundNavMapper fundNavMapper;

    @Autowired
    private IndexDataMapper indexDataMapper;

    /**
     * 执行回测
     */
    public BacktestResponse runBacktest(BacktestRequest request) {
        logger.info("开始执行回测业务逻辑...");
        try {
            // 获取基金净值数据
            logger.info("步骤1: 获取基金净值数据...");
            List<FundNav> fundDataList = fundNavMapper.selectByFundCode(request.getFundCode(), 365); // 获取最近一年数据
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
                    .filter(nav -> !nav.getNavDate().isBefore(startDate) && !nav.getNavDate().isAfter(endDate))
                    .sorted(Comparator.comparing(FundNav::getNavDate))
                    .collect(Collectors.toList());
            
            logger.info("筛选后的基金数据: {} 条 (日期范围: {} 至 {})", 
                fundDataList.size(), startDate, endDate);
            
            if (fundDataList.isEmpty()) {
                throw new RuntimeException("没有找到符合条件的基金数据");
            }
            
            // 不再获取指数数据（去掉上证指数的参照）
            logger.info("步骤3: 使用基金数据进行回测，不再使用指数数据");
            
            // 将基金数据转换为Map便于查找
            Map<LocalDate, FundNav> fundDataMap = fundDataList.stream()
                    .collect(Collectors.toMap(FundNav::getNavDate, nav -> nav));
            
            // 获取交易日期并排序
            List<LocalDate> sortedDates = new ArrayList<>(fundDataMap.keySet());
            sortedDates.sort(LocalDate::compareTo);
            
            logger.info("找到 {} 个交易日", sortedDates.size());
            
            // 执行回测
            logger.info("步骤4: 执行回测算法...");
            return performBacktest(request, sortedDates, fundDataMap);
            
        } catch (Exception e) {
            logger.error("回测执行失败", e);
            throw new RuntimeException("回测执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行回测核心逻辑
     */
    private BacktestResponse performBacktest(BacktestRequest request,
                                            List<LocalDate> sortedDates,
                                            Map<LocalDate, FundNav> fundDataMap) {
        
        logger.info("进入回测核心逻辑...");
        
        // 初始化资金和持仓
        double initialCapital = request.getInitialCapital();
        double initialHoldingsValue = initialCapital * (request.getInitialHoldings() / 100.0); // 初始持仓市值
        double initialCash = initialCapital - initialHoldingsValue; // 初始现金
        
        // 根据初始持仓市值和第一天的基金净值计算初始持仓份额
        LocalDate firstDate = sortedDates.get(0);
        FundNav firstFundNav = fundDataMap.get(firstDate);
        if (firstFundNav == null) {
            throw new RuntimeException("第一天没有基金净值数据");
        }
        
        double initialNav = firstFundNav.getUnitNav().doubleValue();
        double initialHoldings = initialHoldingsValue / initialNav; // 初始持仓份额
        double currentCash = initialCash;
        double currentHoldings = initialHoldings; // 持仓份额
        
        // 计算初始总资产
        double initialTotalAssets = currentCash + currentHoldings * initialNav;
        
        // 存储每日交易记录
        List<BacktestResponse.DailyDetail> dailyDetails = new ArrayList<>();
        
        // 统计变量
        int upPositionChanges = 0; // 加仓次数
        int downPositionChanges = 0; // 减仓次数
        double peakHoldings = initialHoldingsValue; // 持仓峰值
        double minTotalAssets = initialTotalAssets; // 最小总资产
        double maxTotalAssets = initialTotalAssets; // 最大总资产
        
        // 遍历交易日期
        for (LocalDate date : sortedDates) {
            FundNav fundNav = fundDataMap.get(date);
            
            if (fundNav == null) {
                continue; // 缺少数据则跳过
            }
            
            // 获取当前净值
            double currentNav = fundNav.getUnitNav().doubleValue();
            
            // 计算当前总资产
            double currentTotalAssets = currentCash + currentHoldings * currentNav;
            
            // 更新峰值和谷值
            if (currentTotalAssets > maxTotalAssets) {
                maxTotalAssets = currentTotalAssets;
            }
            if (currentTotalAssets < minTotalAssets) {
                minTotalAssets = currentTotalAssets;
            }
            
            // 计算持仓市值
            double currentHoldingsValue = currentHoldings * currentNav;
            if (currentHoldingsValue > peakHoldings) {
                peakHoldings = currentHoldingsValue;
            }
            
            // 触发规则判断
            String action = "HOLD"; // 默认持有
            
            // 获取最近几天的数据用于规则判断
            int currentIndex = sortedDates.indexOf(date);
            
            // 规则A：连续5天或以上上涨/下跌（基金净值）
            if (currentIndex >= 4) { // 至少有5天数据
                boolean isConsecutiveUp = true;
                boolean isConsecutiveDown = true;
                
                for (int i = currentIndex - 4; i <= currentIndex; i++) {
                    LocalDate checkDate = sortedDates.get(i);
                    FundNav checkFundNav = fundDataMap.get(checkDate);
                    if (checkFundNav != null) {
                        double checkChange = checkFundNav.getDailyReturn() != null ? 
                            checkFundNav.getDailyReturn().doubleValue() : 0.0;
                        if (checkChange <= 0) {
                            isConsecutiveUp = false;
                        }
                        if (checkChange >= 0) {
                            isConsecutiveDown = false;
                        }
                    } else {
                        isConsecutiveUp = false;
                        isConsecutiveDown = false;
                    }
                }
                
                if (isConsecutiveUp || isConsecutiveDown) {
                    // 连续上涨减仓，连续下跌加仓
                    double positionChangeAmount = currentTotalAssets * (request.getUpPositionChange() / 100.0);
                    if (isConsecutiveUp && currentHoldingsValue >= positionChangeAmount) {
                        // 连续上涨，减仓
                        double sharesToSell = positionChangeAmount / currentNav;
                        if (sharesToSell <= currentHoldings) {
                            currentCash += positionChangeAmount;
                            currentHoldings -= sharesToSell;
                            action = "SELL";
                            downPositionChanges++;
                        }
                    } else if (isConsecutiveDown && currentCash >= positionChangeAmount) {
                        // 连续下跌，加仓
                        double sharesToBuy = positionChangeAmount / currentNav;
                        currentCash -= positionChangeAmount;
                        currentHoldings += sharesToBuy;
                        action = "BUY";
                        upPositionChanges++;
                    }
                }
            }
            
            // 规则B：单日涨跌幅绝对值5%（基金净值）
            FundNav currentFundNav = fundDataMap.get(date);
            double fundChange = currentFundNav.getDailyReturn() != null ? 
                currentFundNav.getDailyReturn().doubleValue() : 0.0;
                
            if (Math.abs(fundChange) >= request.getSingleDayThreshold()) {
                double positionChangeAmount = currentTotalAssets * (request.getUpPositionChange() / 100.0);
                if (fundChange < 0 && currentCash >= positionChangeAmount) {
                    // 跌幅超过阈值，加仓（连续下跌加仓）
                    double sharesToBuy = positionChangeAmount / currentNav;
                    currentCash -= positionChangeAmount;
                    currentHoldings += sharesToBuy;
                    action = "BUY";
                    upPositionChanges++;
                } else if (fundChange > 0 && currentHoldingsValue >= positionChangeAmount) {
                    // 涨幅超过阈值，减仓（连续上涨减仓）
                    double sharesToSell = positionChangeAmount / currentNav;
                    if (sharesToSell <= currentHoldings) {
                        currentCash += positionChangeAmount;
                        currentHoldings -= sharesToSell;
                        action = "SELL";
                        downPositionChanges++;
                    }
                }
            }
            
            // 规则C：连续2天累计涨跌幅绝对值4%（基金净值）
            if (currentIndex >= 1) { // 至少有2天数据
                double cumulativeChange = 0;
                for (int i = Math.max(0, currentIndex - 1); i <= currentIndex; i++) {
                    LocalDate checkDate = sortedDates.get(i);
                    FundNav checkFundNav = fundDataMap.get(checkDate);
                    if (checkFundNav != null) {
                        cumulativeChange += checkFundNav.getDailyReturn() != null ? 
                            checkFundNav.getDailyReturn().doubleValue() : 0.0;
                    }
                }
                
                if (Math.abs(cumulativeChange) >= request.getConsecutive2DaysThreshold()) {
                    double positionChangeAmount = currentTotalAssets * (request.getUpPositionChange() / 100.0);
                    if (cumulativeChange < 0 && currentCash >= positionChangeAmount) {
                        // 累计下跌超过阈值，加仓
                        double sharesToBuy = positionChangeAmount / currentNav;
                        currentCash -= positionChangeAmount;
                        currentHoldings += sharesToBuy;
                        action = "BUY";
                        upPositionChanges++;
                    } else if (cumulativeChange > 0 && currentHoldingsValue >= positionChangeAmount) {
                        // 累计上涨超过阈值，减仓
                        double sharesToSell = positionChangeAmount / currentNav;
                        if (sharesToSell <= currentHoldings) {
                            currentCash += positionChangeAmount;
                            currentHoldings -= sharesToSell;
                            action = "SELL";
                            downPositionChanges++;
                        }
                    }
                }
            }
            
            // 规则D：连续3天累计涨跌幅绝对值5%（基金净值）
            if (currentIndex >= 2) { // 至少有3天数据
                double cumulativeChange = 0;
                for (int i = Math.max(0, currentIndex - 2); i <= currentIndex; i++) {
                    LocalDate checkDate = sortedDates.get(i);
                    FundNav checkFundNav = fundDataMap.get(checkDate);
                    if (checkFundNav != null) {
                        cumulativeChange += checkFundNav.getDailyReturn() != null ? 
                            checkFundNav.getDailyReturn().doubleValue() : 0.0;
                    }
                }
                
                if (Math.abs(cumulativeChange) >= request.getConsecutive3DaysThreshold()) {
                    double positionChangeAmount = currentTotalAssets * (request.getUpPositionChange() / 100.0);
                    if (cumulativeChange < 0 && currentCash >= positionChangeAmount) {
                        // 累计下跌超过阈值，加仓
                        double sharesToBuy = positionChangeAmount / currentNav;
                        currentCash -= positionChangeAmount;
                        currentHoldings += sharesToBuy;
                        action = "BUY";
                        upPositionChanges++;
                    } else if (cumulativeChange > 0 && currentHoldingsValue >= positionChangeAmount) {
                        // 累计上涨超过阈值，减仓
                        double sharesToSell = positionChangeAmount / currentNav;
                        if (sharesToSell <= currentHoldings) {
                            currentCash += positionChangeAmount;
                            currentHoldings -= sharesToSell;
                            action = "SELL";
                            downPositionChanges++;
                        }
                    }
                }
            }
            
            // 规则E：连续4天累计涨跌幅绝对值5%（基金净值）
            if (currentIndex >= 3) { // 至少有4天数据
                double cumulativeChange = 0;
                for (int i = Math.max(0, currentIndex - 3); i <= currentIndex; i++) {
                    LocalDate checkDate = sortedDates.get(i);
                    FundNav checkFundNav = fundDataMap.get(checkDate);
                    if (checkFundNav != null) {
                        cumulativeChange += checkFundNav.getDailyReturn() != null ? 
                            checkFundNav.getDailyReturn().doubleValue() : 0.0;
                    }
                }
                
                if (Math.abs(cumulativeChange) >= request.getConsecutive4DaysThreshold()) {
                    double positionChangeAmount = currentTotalAssets * (request.getUpPositionChange() / 100.0);
                    if (cumulativeChange < 0 && currentCash >= positionChangeAmount) {
                        // 累计下跌超过阈值，加仓
                        double sharesToBuy = positionChangeAmount / currentNav;
                        currentCash -= positionChangeAmount;
                        currentHoldings += sharesToBuy;
                        action = "BUY";
                        upPositionChanges++;
                    } else if (cumulativeChange > 0 && currentHoldingsValue >= positionChangeAmount) {
                        // 累计上涨超过阈值，减仓
                        double sharesToSell = positionChangeAmount / currentNav;
                        if (sharesToSell <= currentHoldings) {
                            currentCash += positionChangeAmount;
                            currentHoldings -= sharesToSell;
                            action = "SELL";
                            downPositionChanges++;
                        }
                    }
                }
            }
            
            // 重新计算总资产
            currentTotalAssets = currentCash + currentHoldings * currentNav;
            
            // 记录当日交易详情
            dailyDetails.add(new BacktestResponse.DailyDetail(
                date.toString(),
                fundChange, // 使用基金涨跌幅而不是指数涨跌幅
                currentNav,
                currentCash,
                currentHoldings,
                currentTotalAssets,
                action
            ));
        }
        
        // 计算最终结果
        LocalDate lastDate = sortedDates.get(sortedDates.size() - 1);
        FundNav lastFundNav = fundDataMap.get(lastDate);
        if (lastFundNav == null) {
            throw new RuntimeException("最后一天没有基金净值数据");
        }
        
        double finalNav = lastFundNav.getUnitNav().doubleValue();
        double finalCash = currentCash;
        double finalHoldings = currentHoldings;
        double finalHoldingsValue = finalHoldings * finalNav;
        double finalTotalAssets = finalCash + finalHoldingsValue;
        
        // 计算收益率
        double returnRate = (finalTotalAssets - initialTotalAssets) / initialTotalAssets;
        
        // 计算最大回撤
        double maxDrawdown = (maxTotalAssets > 0) ? (maxTotalAssets - minTotalAssets) / maxTotalAssets : 0;
        
        // 创建响应对象
        BacktestResponse response = new BacktestResponse();
        response.setInitialCapital(initialCapital);
        response.setInitialHoldings(initialHoldingsValue);
        response.setFinalCapital(finalCash);
        response.setFinalHoldings(finalHoldings);
        response.setFinalNav(finalNav);
        response.setFinalHoldingsValue(finalHoldingsValue);
        response.setTotalAssets(finalTotalAssets);
        response.setReturnRate(returnRate);
        response.setUpPositionChanges(upPositionChanges);
        response.setDownPositionChanges(downPositionChanges);
        response.setMaxDrawdown(maxDrawdown);
        response.setPeakHoldings(peakHoldings);
        response.setTradingDays(sortedDates.size());
        response.setDailyDetails(dailyDetails);
        
        return response;
    }

    /**
     * 生成模拟指数数据（用于测试，实际应从数据库获取）
     */
    private List<IndexData> generateMockIndexData(LocalDate startDate, LocalDate endDate) {
        List<IndexData> indexDataList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        Random random = new Random();
        double currentValue = 3000.0; // 初始指数值
        
        while (!currentDate.isAfter(endDate)) {
            // 生成随机涨跌幅 (-3% 到 +3%)
            double changePercent = (random.nextDouble() - 0.5) * 0.06; // -3% to +3%
            double changeValue = currentValue * changePercent;
            double newValue = currentValue + changeValue;
            
            IndexData indexData = new IndexData();
            indexData.setTradeDate(currentDate);
            indexData.setClosePrice(BigDecimal.valueOf(newValue).setScale(2, RoundingMode.HALF_UP));
            indexData.setDailyReturn(BigDecimal.valueOf(changePercent * 100).setScale(2, RoundingMode.HALF_UP));
            
            indexDataList.add(indexData);
            
            currentValue = newValue;
            currentDate = currentDate.plusDays(1);
        }
        
        return indexDataList;
    }
}