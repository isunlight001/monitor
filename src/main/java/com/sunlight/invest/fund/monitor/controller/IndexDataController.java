package com.sunlight.invest.fund.monitor.controller;

import com.sunlight.invest.fund.monitor.entity.IndexData;
import com.sunlight.invest.fund.monitor.service.IndexDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指数数据控制器
 * <p>
 * 提供指数数据相关的API接口
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
@RestController
@RequestMapping("/api/index")
@CrossOrigin(origins = "*")
public class IndexDataController {

    @Autowired
    private IndexDataService indexDataService;

    /**
     * 获取支持的指数列表
     *
     * @return 指数列表
     */
    @GetMapping("/supported")
    public Map<String, Object> getSupportedIndexes() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> indexCodes = indexDataService.getSupportedIndexCodes();
            
            result.put("success", true);
            result.put("count", indexCodes.size());
            result.put("data", indexCodes);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取支持的指数列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 手动触发获取并保存指数数据
     *
     * @param indexCode 指数代码
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @return 响应结果
     */
    @PostMapping("/fetch")
    public Map<String, Object> fetchIndexData(
            @RequestParam String indexCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        Map<String, Object> result = new HashMap<>();

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            int count = indexDataService.fetchAndSaveIndexData(indexCode, start, end);

            result.put("success", true);
            result.put("message", "数据获取并保存成功");
            result.put("count", count);
            result.put("indexCode", indexCode);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据获取并保存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 手动触发获取并保存所有指数数据
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @return 响应结果
     */
    @PostMapping("/fetch-all")
    public Map<String, Object> fetchAllIndexData(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        Map<String, Object> result = new HashMap<>();

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            int count = indexDataService.fetchAndSaveAllIndexData(start, end);

            result.put("success", true);
            result.put("message", "所有指数数据获取并保存成功");
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "所有指数数据获取并保存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询指数最近的行情数据
     *
     * @param indexCode 指数代码
     * @param days      天数（默认30天）
     * @return 指数数据列表
     */
    @GetMapping("/data")
    public Map<String, Object> getIndexData(
            @RequestParam String indexCode,
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> result = new HashMap<>();

        try {
            List<IndexData> dataList = indexDataService.fetchIndexData(indexCode, 
                    LocalDate.now().minusDays(days), LocalDate.now());

            result.put("success", true);
            result.put("indexCode", indexCode);
            result.put("indexName", indexDataService.getIndexName(indexCode));
            result.put("count", dataList.size());
            result.put("data", dataList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }
}