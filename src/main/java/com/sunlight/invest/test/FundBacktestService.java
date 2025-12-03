package com.sunlight.invest.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基金回测服务类
 */
public class FundBacktestService {
    
    // 指数代码映射
    private static final Map<String, String> INDEX_MAP = new HashMap<>();
    
    // 基金代码
    private static final String FUND_CODE = "501047"; // 国金量化多因子
    
    // 起始日期
    private static final LocalDate START_DATE = LocalDate.of(2000, 1, 1);
    
    // 回测起始日期
    private static final LocalDate BACKTEST_START = LocalDate.of(2020, 1, 1);
    
    // 初始资金
    private static final double INIT_CASH = 1000000;
    
    // 交易单位
    private static final double TRADE_UNIT = 10000;
    
    static {
        INDEX_MAP.put("上证指数", "sh000001");
        INDEX_MAP.put("深证成指", "sz399001");
        INDEX_MAP.put("创业板指", "sz399006");
    }
    
    /**
     * 获取指数数据（模拟实现）
     * 在实际应用中，这里应该接入真实的金融数据API
     * 
     * @param indexName 指数名称
     * @return 指数数据列表
     */
    public List<IndexData> getIndexData(String indexName) {
        System.out.println("正在获取" + indexName + "数据...");
        
        // 模拟数据获取，实际应从API获取
        List<IndexData> dataList = generateMockIndexData(indexName);
        
        System.out.println("获取到" + dataList.size() + "条" + indexName + "数据");
        return dataList;
    }
    
    /**
     * 获取基金净值数据（模拟实现）
     * 在实际应用中，这里应该接入真实的基金数据API
     * 
     * @param fundCode 基金代码
     * @return 基金数据列表
     */
    public List<Fund> getFundData(String fundCode) {
        System.out.println("正在获取基金" + fundCode + "的净值数据...");
        
        // 模拟数据获取，实际应从API获取
        List<Fund> fundList = generateMockFundData(fundCode);
        
        System.out.println("获取到" + fundList.size() + "条基金数据");
        return fundList;
    }
    
    /**
     * 执行回测
     * 
     * @param indexDataList 指数数据列表
     * @param fundDataList 基金数据列表
     * @return 回测结果列表
     */
    public List<BacktestResult> backtest(List<IndexData> indexDataList, List<Fund> fundDataList) {
        System.out.println("开始执行回测...");
        
        // 合并数据
        Map<LocalDate, IndexData> indexMap = indexDataList.stream()
                .collect(Collectors.toMap(IndexData::getTradeDate, data -> data));
        
        Map<LocalDate, Fund> fundMap = fundDataList.stream()
                .collect(Collectors.toMap(Fund::getTradeDate, fund -> fund));
        
        // 获取共同的交易日期
        Set<LocalDate> commonDates = new HashSet<>(indexMap.keySet());
        commonDates.retainAll(fundMap.keySet());
        
        // 筛选出回测起始日期之后的数据
        List<LocalDate> backtestDates = commonDates.stream()
                .filter(date -> !date.isBefore(BACKTEST_START))
                .sorted()
                .collect(Collectors.toList());
        
        // 执行回测逻辑
        double fundAmount = INIT_CASH;
        List<BacktestResult> results = new ArrayList<>();
        
        for (LocalDate date : backtestDates) {
            IndexData indexData = indexMap.get(date);
            Fund fund = fundMap.get(date);
            
            // 获取上证指数涨跌幅
            Double shPercent = indexData.getShPercent();
            double nav = fund.getNav();
            
            String action = "持有";
            double change = 0;
            
            if (shPercent != null) {
                if (shPercent > 1) {
                    // 上证涨幅>1%加仓1W
                    fundAmount += TRADE_UNIT;
                    action = "加仓" + (int)(TRADE_UNIT/10000) + "万";
                    change = TRADE_UNIT;
                } else if (shPercent < -1) {
                    // 上证跌幅<-1%减仓1W
                    fundAmount -= TRADE_UNIT;
                    fundAmount = Math.max(fundAmount, 0); // 基金总额不能为负
                    action = "减仓" + (int)(TRADE_UNIT/10000) + "万";
                    change = -TRADE_UNIT;
                }
            }
            
            results.add(new BacktestResult(date, nav, fundAmount, action, change));
        }
        
        System.out.println("回测完成，共生成" + results.size() + "条记录");
        return results;
    }
    
    /**
     * 将回测结果输出到CSV文件
     * 
     * @param results 回测结果列表
     * @param filePath 输出文件路径
     * @throws IOException 文件写入异常
     */
    public void outputToCsv(List<BacktestResult> results, String filePath) throws IOException {
        System.out.println("正在将结果写入文件: " + filePath);
        
        // 确保目录存在
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // 写入表头
            writer.println("date,nav,fund_amt,action,change");
            
            // 写入数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (BacktestResult result : results) {
                writer.printf("%s,%.4f,%.2f,%s,%.2f%n",
                        result.getDate().format(formatter),
                        result.getNav(),
                        result.getFundAmount(),
                        result.getAction(),
                        result.getChange());
            }
        }
        
        System.out.println("结果已写入文件，共" + results.size() + "条记录");
    }
    
    /**
     * 生成模拟指数数据（用于演示）
     * 
     * @param indexName 指数名称
     * @return 模拟的指数数据
     */
    private List<IndexData> generateMockIndexData(String indexName) {
        List<IndexData> dataList = new ArrayList<>();
        
        // 生成从2000年至今的模拟数据（只生成工作日）
        LocalDate currentDate = START_DATE;
        LocalDate endDate = LocalDate.now();
        Random random = new Random(42); // 固定种子以保证结果可重现
        
        while (!currentDate.isAfter(endDate)) {
            // 只在工作日生成数据
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                IndexData data = new IndexData(currentDate);
                
                // 生成模拟涨跌幅数据（正态分布）
                double shPercent = (random.nextGaussian() * 1.5);
                double szPercent = (random.nextGaussian() * 1.5);
                double chuangyePercent = (random.nextGaussian() * 2.0);
                
                data.setShPercent(shPercent);
                data.setSzPercent(szPercent);
                data.setChuangyePercent(chuangyePercent);
                
                dataList.add(data);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return dataList;
    }
    
    /**
     * 生成模拟基金数据（用于演示）
     * 
     * @param fundCode 基金代码
     * @return 模拟的基金数据
     */
    private List<Fund> generateMockFundData(String fundCode) {
        List<Fund> fundList = new ArrayList<>();
        
        // 生成从2000年至今的模拟数据（只生成工作日）
        LocalDate currentDate = START_DATE;
        LocalDate endDate = LocalDate.now();
        Random random = new Random(123); // 固定种子以保证结果可重现
        
        double nav = 1.0; // 初始净值
        
        while (!currentDate.isAfter(endDate)) {
            // 只在工作日生成数据
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                // 模拟净值变化（基于随机波动）
                double change = (random.nextGaussian() * 0.01);
                nav = nav * (1 + change);
                nav = Math.max(nav, 0.1); // 确保净值不会过低
                
                fundList.add(new Fund(currentDate, nav));
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return fundList;
    }
}