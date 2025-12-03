package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基金数据爬取服务测试类
 */
class FundCrawlerServiceTest {

    @Test
    void testParseHtml() {
        FundCrawlerService service = new FundCrawlerService();
        
        // 模拟HTML内容（来自http://www.dayfund.cn/fundvalue/006195.html）
        String html = "<table>\n" +
                "<tr class=\"row1\">\n" +
                "<td>2023-12-01</td>\n" +
                "<td>星期五</td>\n" +
                "<td>1.2050</td>\n" +
                "<td>1.2050</td>\n" +
                "<td>1.5620</td>\n" +
                "<td>1.5620</td>\n" +
                "<td>-0.33%</td>\n" +
                "<td>-0.33%</td>\n" +
                "<td>-0.33%</td>\n" +
                "</tr>\n" +
                "<tr class=\"row2\">\n" +
                "<td>2023-11-30</td>\n" +
                "<td>星期四</td>\n" +
                "<td>1.2090</td>\n" +
                "<td>1.2090</td>\n" +
                "<td>1.5660</td>\n" +
                "<td>1.5660</td>\n" +
                "<td>0.17%</td>\n" +
                "<td>0.17%</td>\n" +
                "<td>0.17%</td>\n" +
                "</tr>\n" +
                "</table>";
        
        // 使用反射调用私有方法
        try {
            java.lang.reflect.Method method = FundCrawlerService.class.getDeclaredMethod(
                    "parseHtml", String.class, String.class, String.class);
            method.setAccessible(true);
            
            List<FundNav> result = (List<FundNav>) method.invoke(
                    service, html, "006195", "国金量化多因子");
            
            // 验证结果
            assertNotNull(result);
            assertEquals(2, result.size());
            
            // 验证第一条记录（较早的日期应该在前面）
            FundNav first = result.get(0);
            assertEquals("006195", first.getFundCode());
            assertEquals("国金量化多因子", first.getFundName());
            assertEquals("2023-11-30", first.getNavDate().toString());
            assertEquals(new BigDecimal("1.2090"), first.getUnitNav());
            assertEquals(new BigDecimal("0.17"), first.getDailyReturn());
            
            // 验证第二条记录
            FundNav second = result.get(1);
            assertEquals("2023-12-01", second.getNavDate().toString());
            assertEquals(new BigDecimal("1.2050"), second.getUnitNav());
            assertEquals(new BigDecimal("-0.33"), second.getDailyReturn());
            
            System.out.println("解析结果:");
            for (FundNav nav : result) {
                System.out.println(nav);
            }
            
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage(), e);
        }
    }
    
    @Test
    void testParseHtmlWithoutGrowthRate() {
        FundCrawlerService service = new FundCrawlerService();
        
        // 模拟HTML内容（没有增长率数据）
        String html = "<table>\n" +
                "<tr class=\"row1\">\n" +
                "<td>2023-12-01</td>\n" +
                "<td>星期五</td>\n" +
                "<td>1.2050</td>\n" +
                "<td>1.2050</td>\n" +
                "<td>1.5620</td>\n" +
                "<td>1.5620</td>\n" +
                "<td>--</td>\n" +
                "<td>--</td>\n" +
                "<td>--</td>\n" +
                "</tr>\n" +
                "<tr class=\"row2\">\n" +
                "<td>2023-11-30</td>\n" +
                "<td>星期四</td>\n" +
                "<td>1.2090</td>\n" +
                "<td>1.2090</td>\n" +
                "<td>1.5660</td>\n" +
                "<td>1.5660</td>\n" +
                "<td>--</td>\n" +
                "<td>--</td>\n" +
                "<td>--</td>\n" +
                "</tr>\n" +
                "</table>";
        
        // 使用反射调用私有方法
        try {
            java.lang.reflect.Method method = FundCrawlerService.class.getDeclaredMethod(
                    "parseHtml", String.class, String.class, String.class);
            method.setAccessible(true);
            
            List<FundNav> result = (List<FundNav>) method.invoke(
                    service, html, "006195", "国金量化多因子");
            
            // 验证结果
            assertNotNull(result);
            assertEquals(2, result.size());
            
            // 验证第一条记录
            FundNav first = result.get(0);
            assertEquals("2023-11-30", first.getNavDate().toString());
            assertEquals(new BigDecimal("1.2090"), first.getUnitNav());
            // 没有有效增长率时应该重新计算，但由于只有两条记录且净值下降，应该有值
            assertNotNull(first.getDailyReturn());
            
            // 验证第二条记录
            FundNav second = result.get(1);
            assertEquals("2023-12-01", second.getNavDate().toString());
            assertEquals(new BigDecimal("1.2050"), second.getUnitNav());
            assertNotNull(second.getDailyReturn());
            
            System.out.println("无增长率解析结果:");
            for (FundNav nav : result) {
                System.out.println(nav);
            }
            
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage(), e);
        }
    }
}
