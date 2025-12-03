package com.sunlight.invest.fund.backtest.controller;

import com.sunlight.invest.fund.backtest.dto.BacktestRequest;
import com.sunlight.invest.fund.backtest.dto.BacktestResponse;
import com.sunlight.invest.fund.backtest.service.FundBacktestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 基金回测接口
 * 
 * @author system
 * @date 2025-12-01
 */
@RestController
@RequestMapping("/api/fund/backtest")
@CrossOrigin
public class FundBacktestController {
    
    private static final Logger logger = LoggerFactory.getLogger(FundBacktestController.class);
    
    @Autowired
    private FundBacktestService backtestService;

    /**
     * 执行回测
     */
    @PostMapping("/run")
    public Map<String, Object> runBacktest(@RequestBody BacktestRequest request) {
        logger.info("========== 接收到回测请求 ==========");
        logger.info("请求参数: 初始资金={}, 初始持仓={}, 回测月数={}", 
            request.getInitialCapital(), request.getInitialHoldings(), request.getBacktestMonths());
        logger.info("策略参数: 涨幅阈值={}%, 跌幅阈值={}%, 加仓={}, 减仓={}",
            request.getUpThreshold(), request.getDownThreshold(), 
            request.getUpPositionChange(), request.getDownPositionChange());
        
        Map<String, Object> result = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            logger.info("开始执行回测...");
            
            BacktestResponse response = backtestService.runBacktest(request);
            
            long endTime = System.currentTimeMillis();
            logger.info("回测执行成功, 耗时: {}ms", (endTime - startTime));
            logger.info("回测结果: 总资产={}, 收益率={}%, 最大回撤={}%",
                response.getTotalAssets(), response.getReturnRate(), response.getMaxDrawdown());
            logger.info("交易统计: 加仓{}+次, 减仓{}次, 交易日{}天",
                response.getUpPositionChanges(), response.getDownPositionChanges(), response.getTradingDays());
            
            result.put("success", true);
            result.put("data", response);
            result.put("message", "回测执行成功");
            
            logger.info("========== 请求处理完成 ==========\n");
        } catch (Exception e) {
            logger.error("回测执行失败", e);
            result.put("success", false);
            result.put("message", "回测执行失败: " + e.getMessage());
            logger.info("========== 请求处理失败 ==========\n");
        }
        return result;
    }

    /**
     * 获取默认参数
     */
    @GetMapping("/default-params")
    public Map<String, Object> getDefaultParams() {
        logger.info("请求获取默认参数");
        Map<String, Object> result = new HashMap<>();
        BacktestRequest defaultRequest = new BacktestRequest();
        result.put("success", true);
        result.put("data", defaultRequest);
        return result;
    }
}
