package com.sunlight.invest.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StockDataParserTest {

    @Autowired
    private StockDataParser parser;

    @Test
    public void testParseStockData() {
        // 模拟搜狐返回的数据格式
        String jsonData = "historySearchHandler([{\"status\":0,\"hq\":[[\"2025-12-09\",\"11.52\",\"11.43\",\"-0.09\",\"-0.78%\",\"11.43\",\"11.54\",\"733957\",\"84273.23\",\"0.38%\"],[\"2025-12-08\",\"11.49\",\"11.52\",\"-0.01\",\"-0.09%\",\"11.48\",\"11.60\",\"8558\"]]}])";
        
        List<StockData> result = parser.parseStockData(jsonData);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        StockData first = result.get(0);
        assertEquals("2025-12-09", first.getDate());
        assertEquals(11.52, first.getOpenPrice(), 0.01);
        assertEquals(11.43, first.getClosePrice(), 0.01);
        
        System.out.println("解析结果: " + result);
    }
}