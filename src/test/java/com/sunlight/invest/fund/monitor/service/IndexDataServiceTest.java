package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.IndexData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 指数数据服务测试类
 */
@SpringBootTest
class IndexDataServiceTest {

    private final IndexDataService indexDataService = new IndexDataService();

    @Test
    void testGetSupportedIndexCodes() {
        List<String> supportedCodes = indexDataService.getSupportedIndexCodes();
        assertNotNull(supportedCodes);
        assertFalse(supportedCodes.isEmpty());
        assertTrue(supportedCodes.contains("000001")); // 上证指数
        assertTrue(supportedCodes.contains("399006")); // 创业板指
        assertTrue(supportedCodes.contains("000688")); // 科创50
        assertTrue(supportedCodes.contains("899050")); // 北证50
    }

    @Test
    void testGetIndexName() {
        assertEquals("上证指数", indexDataService.getIndexName("000001"));
        assertEquals("创业板指", indexDataService.getIndexName("399006"));
        assertEquals("科创50", indexDataService.getIndexName("000688"));
        assertEquals("北证50", indexDataService.getIndexName("899050"));
        assertEquals("未知指数", indexDataService.getIndexName("999999")); // 不支持的代码
    }

    // 注意：以下测试需要网络连接，且可能受网络环境影响
    // @Test
    // void testFetchIndexData() {
    //     LocalDate startDate = LocalDate.now().minusDays(30);
    //     LocalDate endDate = LocalDate.now();
    //     
    //     List<IndexData> dataList = indexDataService.fetchIndexData("000001", startDate, endDate);
    //     assertNotNull(dataList);
    //     // 不为空时验证数据结构
    //     if (!dataList.isEmpty()) {
    //         IndexData firstData = dataList.get(0);
    //         assertNotNull(firstData.getIndexCode());
    //         assertNotNull(firstData.getIndexName());
    //         assertNotNull(firstData.getTradeDate());
    //         assertNotNull(firstData.getOpenPrice());
    //         assertNotNull(firstData.getClosePrice());
    //         assertNotNull(firstData.getHighPrice());
    //         assertNotNull(firstData.getLowPrice());
    //         assertNotNull(firstData.getDailyReturn());
    //     }
    // }
}