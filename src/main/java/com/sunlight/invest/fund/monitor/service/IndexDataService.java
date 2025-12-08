package com.sunlight.invest.fund.monitor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunlight.invest.fund.monitor.entity.IndexData;
import com.sunlight.invest.fund.monitor.mapper.IndexDataMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 指数数据服务类
 * <p>
 * 提供各类指数数据的获取、解析和存储功能
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
@Service
public class IndexDataService {

    private static final Logger log = LoggerFactory.getLogger(IndexDataService.class);

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 指数代码映射
    private static final Map<String, String> INDEX_CODE_MAP = new HashMap<>();
    private static final Map<String, String> INDEX_NAME_MAP = new HashMap<>();

    static {
        // 上证指数系列
        INDEX_CODE_MAP.put("000001", "zs_000001"); // 上证指数
        INDEX_CODE_MAP.put("000016", "zs_000016"); // 上证50
        INDEX_CODE_MAP.put("000300", "zs_000300"); // 沪深300
        INDEX_CODE_MAP.put("000905", "zs_000905"); // 中证500
        INDEX_CODE_MAP.put("000852", "zs_000852"); // 中证1000
        
        // 深证指数系列
        INDEX_CODE_MAP.put("399001", "sz_399001"); // 深证成指
        INDEX_CODE_MAP.put("399005", "sz_399005"); // 中小板指
        INDEX_CODE_MAP.put("399006", "sz_399006"); // 创业板指
        INDEX_CODE_MAP.put("399673", "sz_399673"); // 创业板50
        
        // 科创板指数
        INDEX_CODE_MAP.put("000688", "zs_000688"); // 科创50
        
        // 北交所指数
        INDEX_CODE_MAP.put("899050", "bj_899050"); // 北证50

        // 指数名称映射
        INDEX_NAME_MAP.put("000001", "上证指数");
        INDEX_NAME_MAP.put("000016", "上证50");
        INDEX_NAME_MAP.put("000300", "沪深300");
        INDEX_NAME_MAP.put("000905", "中证500");
        INDEX_NAME_MAP.put("000852", "中证1000");
        INDEX_NAME_MAP.put("399001", "深证成指");
        INDEX_NAME_MAP.put("399005", "中小板指");
        INDEX_NAME_MAP.put("399006", "创业板指");
        INDEX_NAME_MAP.put("399673", "创业板50");
        INDEX_NAME_MAP.put("000688", "科创50");
        INDEX_NAME_MAP.put("899050", "北证50");
    }

    @Autowired
    private IndexDataMapper indexDataMapper;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        // 初始化HTTP客户端
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // 创建数据表
//        try {
//            indexDataMapper.createTable();
//            log.info("指数数据表创建成功");
//        } catch (Exception e) {
//            log.warn("指数数据表创建失败: {}", e.getMessage());
//        }
    }

    /**
     * 清洗并解析BigDecimal值
     *
     * @param value 字符串值
     * @return BigDecimal值
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 移除百分号和其他非数字字符（保留数字、小数点、负号）
        String cleanedValue = value.trim().replaceAll("[^0-9.\\-]", "");
        if (cleanedValue.isEmpty() || "-".equals(cleanedValue)) {
            return BigDecimal.ZERO;
        }
        
        try {
            return new BigDecimal(cleanedValue);
        } catch (NumberFormatException e) {
            log.warn("解析BigDecimal失败，原始值: {}, 清洗后值: {}, 使用默认值0", value, cleanedValue);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 清洗并解析Long值
     *
     * @param value 字符串值
     * @return Long值
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        
        // 移除非数字字符
        String cleanedValue = value.trim().replaceAll("[^0-9]", "");
        if (cleanedValue.isEmpty()) {
            return 0L;
        }
        
        try {
            return Long.parseLong(cleanedValue);
        } catch (NumberFormatException e) {
            log.warn("解析Long失败，原始值: {}, 清洗后值: {}, 使用默认值0", value, cleanedValue);
            return 0L;
        }
    }
    
    /**
     * 获取所有支持的指数代码列表
     *
     * @return 指数代码列表
     */
    public List<String> getSupportedIndexCodes() {
        return new ArrayList<>(INDEX_CODE_MAP.keySet());
    }

    /**
     * 根据指数代码获取指数名称
     *
     * @param indexCode 指数代码
     * @return 指数名称
     */
    public String getIndexName(String indexCode) {
        return INDEX_NAME_MAP.getOrDefault(indexCode, "未知指数");
    }

    /**
     * 获取指定指数在日期范围内的数据
     *
     * @param indexCode 指数代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 指数数据列表
     */
    public List<IndexData> fetchIndexData(String indexCode, LocalDate startDate, LocalDate endDate) {
        String sinaCode = INDEX_CODE_MAP.get(indexCode);
        if (sinaCode == null) {
            throw new IllegalArgumentException("不支持的指数代码: " + indexCode);
        }

        String startStr = startDate.format(DATE_FORMATTER);
        String endStr = endDate.format(DATE_FORMATTER);
        
        // 使用东方财富API获取数据，更稳定可靠
        String url = "http://push2his.eastmoney.com/api/qt/stock/kline/get?" +
                    "secid=" + sinaCode + "&" +
                    "klt=101&" +  // 日线
                    "fqt=1&" +    // 前复权
                    "beg=" + startStr + "&" +
                    "end=" + endStr + "&" +
                    "lmt=1000";   // 最大返回条数

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Referer", "http://quote.eastmoney.com/")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HTTP请求失败: " + response.code());
                }

                String jsonResponse = response.body().string();
                Map<String, Object> resultMap = MAPPER.readValue(jsonResponse, 
                        new TypeReference<Map<String, Object>>() {});
                
                // 检查API返回状态
                Object rc = resultMap.get("rc");
                if (rc == null || !"0".equals(String.valueOf(rc))) {
                    throw new RuntimeException("API返回错误: " + resultMap.get("rt"));
                }
                
                // 解析数据
                Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
                if (data == null) {
                    return new ArrayList<>();
                }
                
                List<List<Object>> klines = (List<List<Object>>) data.get("klines");
                if (klines == null || klines.isEmpty()) {
                    return new ArrayList<>();
                }
                
                String indexName = INDEX_NAME_MAP.getOrDefault(indexCode, "未知指数");
                
                return klines.stream().map(kline -> {
                    try {
                        IndexData dataObj = new IndexData();
                        dataObj.setIndexCode(indexCode);
                        dataObj.setIndexName(indexName);
                        
                        // 解析日期 (格式: "2023-12-01")
                        String dateStr = String.valueOf(kline.get(0));
                        dataObj.setTradeDate(LocalDate.parse(dateStr, ISO_FORMATTER));
                        
                        // 解析开盘价、收盘价、最高价、最低价
                        BigDecimal open = new BigDecimal(String.valueOf(kline.get(1)));
                        BigDecimal close = new BigDecimal(String.valueOf(kline.get(2)));
                        BigDecimal high = new BigDecimal(String.valueOf(kline.get(3)));
                        BigDecimal low = new BigDecimal(String.valueOf(kline.get(4)));
                        
                        dataObj.setOpenPrice(open);
                        dataObj.setClosePrice(close);
                        dataObj.setHighPrice(high);
                        dataObj.setLowPrice(low);
                        
                        // 成交量和成交额
                        Long volume = Long.valueOf(String.valueOf(kline.get(5)));
                        BigDecimal amount = new BigDecimal(String.valueOf(kline.get(6)));
                        dataObj.setVolume(volume);
                        dataObj.setAmount(amount);
                        
                        // 计算涨跌幅百分比
                        BigDecimal change = close.subtract(open);
                        BigDecimal pct = open.compareTo(BigDecimal.ZERO) != 0 ? 
                                change.divide(open, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)) :
                                BigDecimal.ZERO;
                        dataObj.setDailyReturn(pct);
                        
                        return dataObj;
                    } catch (Exception e) {
                        log.warn("解析单条数据失败: {}", e.getMessage());
                        return null;
                    }
                }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取指数数据失败，指数代码: {}, 错误: {}", indexCode, e.getMessage(), e);
            // 如果东方财富API失败，尝试回退到搜狐API
            return fetchIndexDataFromSohu(indexCode, startDate, endDate);
        }
    }
    
    /**
     * 从搜狐API获取指数数据（备用方案）
     *
     * @param indexCode 指数代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 指数数据列表
     */
    private List<IndexData> fetchIndexDataFromSohu(String indexCode, LocalDate startDate, LocalDate endDate) {
        String sinaCode = INDEX_CODE_MAP.get(indexCode);
        if (sinaCode == null) {
            throw new IllegalArgumentException("不支持的指数代码: " + indexCode);
        }

        String startStr = startDate.format(DATE_FORMATTER);
        String endStr = endDate.format(DATE_FORMATTER);
        
        String url = "https://q.stock.sohu.com/hisHq?code=" + sinaCode + 
                    "&start=" + startStr + "&end=" + endStr +
                    "&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp";

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", USER_AGENT)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HTTP请求失败: " + response.code());
                }

                String jsonp = response.body().string().trim();
                String json = jsonp.replaceAll("^historySearchHandler\\((.*)\\);?$", "$1");
                
                List<Map<String, Object>> tmp = MAPPER.readValue(json,
                        new TypeReference<List<Map<String, Object>>>() {});
                
                if (tmp.isEmpty()) {
                    return new ArrayList<>();
                }
                
                List<List<String>> hq = (List<List<String>>) tmp.get(0).get("hq");
                String indexName = INDEX_NAME_MAP.getOrDefault(indexCode, "未知指数");
                
                return hq.stream().map(arr -> {
                    IndexData data = new IndexData();
                    data.setIndexCode(indexCode);
                    data.setIndexName(indexName);
                    data.setTradeDate(LocalDate.parse(arr.get(0), ISO_FORMATTER));
                    
                    // 解析数据，清洗非数字字符
                    BigDecimal open = parseBigDecimal(arr.get(1));
                    BigDecimal close = parseBigDecimal(arr.get(2));
                    BigDecimal change = parseBigDecimal(arr.get(3)); // 涨跌点数
                    
                    data.setOpenPrice(open);
                    data.setClosePrice(close);
                    data.setHighPrice(parseBigDecimal(arr.get(4))); // 最高价
                    data.setLowPrice(parseBigDecimal(arr.get(5)));  // 最低价
                    data.setVolume(parseLong(arr.get(7)));     // 成交量
                    data.setAmount(parseBigDecimal(arr.get(8)));     // 成交额
                    
                    // 计算涨跌幅百分比
                    BigDecimal pct = open.compareTo(BigDecimal.ZERO) != 0 ?
                            change.divide(open, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;
                    data.setDailyReturn(pct);
                    
                    return data;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从搜狐API获取指数数据失败，指数代码: {}, 错误: {}", indexCode, e.getMessage(), e);
            throw new RuntimeException("获取指数数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存指数数据到数据库
     *
     * @param indexDataList 指数数据列表
     * @return 保存的记录数
     */
    public int saveIndexData(List<IndexData> indexDataList) {
        if (indexDataList == null || indexDataList.isEmpty()) {
            return 0;
        }

        try {
            int count = indexDataMapper.batchInsert(indexDataList);
            log.info("保存指数数据成功，记录数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("保存指数数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存指数数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取并保存指定指数在日期范围内的数据
     *
     * @param indexCode 指数代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 保存的记录数
     */
    public int fetchAndSaveIndexData(String indexCode, LocalDate startDate, LocalDate endDate) {
        List<IndexData> dataList = fetchIndexData(indexCode, startDate, endDate);
        return saveIndexData(dataList);
    }

    /**
     * 获取并保存所有支持指数在日期范围内的数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 保存的总记录数
     */
    public int fetchAndSaveAllIndexData(LocalDate startDate, LocalDate endDate) {
        int totalCount = 0;
        
        for (String indexCode : INDEX_CODE_MAP.keySet()) {
            try {
                int count = fetchAndSaveIndexData(indexCode, startDate, endDate);
                totalCount += count;
                log.info("指数 {} 数据获取并保存完成，记录数: {}", indexCode, count);
            } catch (Exception e) {
                log.error("获取并保存指数 {} 数据失败: {}", indexCode, e.getMessage(), e);
            }
        }
        
        log.info("所有指数数据获取并保存完成，总记录数: {}", totalCount);
        return totalCount;
    }
}