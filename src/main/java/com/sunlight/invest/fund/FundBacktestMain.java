package com.sunlight.invest.fund;

import java.util.List;

/**
 * 基金回测主程序入口
 */
public class FundBacktestMain {
    
    public static void main(String[] args) {
        try {
            FundBacktestService service = new FundBacktestService();
            
            // a) 下载2000年至今上证、深证、创业板每日涨跌幅数据
            System.out.println("步骤a: 下载指数数据...");
            // 注意：当前是模拟数据，在实际应用中需要接入真实的数据源
            List<IndexData> shIndexData = service.getIndexData("上证指数");
            List<IndexData> szIndexData = service.getIndexData("深证成指");
            List<IndexData> cyIndexData = service.getIndexData("创业板指");
            
            // b) 下载国金量化多因子基金每日净值数据
            System.out.println("步骤b: 下载基金净值数据...");
            // 注意：当前是模拟数据，在实际应用中需要接入真实的基金数据源
            List<Fund> fundDataList = service.getFundData("501047");
            
            // c) 用100W资金回测：2020年后，上证涨幅>1%加仓1W，跌幅<-1%减仓1W，基金总额不能为负
            System.out.println("步骤c: 执行回测策略...");
            List<IndexData> indexDataList = mergeIndexData(shIndexData, szIndexData, cyIndexData);
            List<BacktestResult> backtestResults = service.backtest(indexDataList, fundDataList);
            
            // d) 输出每日基金市值和操作记录到 backtest_result.csv
            System.out.println("步骤d: 输出结果到CSV文件...");
            String outputPath = "D:/python/zhipu/fund/backtest_result.csv";
            service.outputToCsv(backtestResults, outputPath);
            
            System.out.println("基金回测已完成，结果保存至: " + outputPath);
            
        } catch (Exception e) {
            System.err.println("执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 合并多个指数数据
     * 
     * @param shData 上证指数数据
     * @param szData 深证成指数据
     * @param cyData 创业板指数据
     * @return 合并后的指数数据
     */
    private static List<IndexData> mergeIndexData(List<IndexData> shData, 
                                                  List<IndexData> szData, 
                                                  List<IndexData> cyData) {
        // 这里简化处理，只返回上证数据
        // 在实际应用中，需要按日期合并所有指数的数据
        return shData;
    }
}