package com.sunlight.invest.fund.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 国金量化多因子 006195
 * 2020-11-21 ~ 2025-11-21 全部净值 → Excel
 */
public class GsNavHtmlToExcel {

    private static final String URL =
            "http://www.dayfund.cn/fundvalue/006195.html?sdate=2020-11-21&edate=2025-11-21";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
        List<Nav> list = fetchTable();
        writeExcel(list);
        System.out.println("✅ 共 " + list.size() + " 条，已写入 国金量化多因子_2020-2025净值.xlsx");
    }

    /** 拉 HTML 并解析表格 */
    public static List<Nav> fetchTable() throws Exception {
        String html = fetchHtml(URL);
        List<Nav> rows = parseTable(html);
        // 按日期升序
        rows.sort((a, b) -> a.date.compareTo(b.date));
        return rows;
    }

    /** 使用 HTTP 获取网页内容 */
    private static String fetchHtml(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(15_000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        connection.disconnect();
        return content.toString();
    }

    /** 解析 HTML 表格 */
    private static List<Nav> parseTable(String html) {
        List<Nav> rows = new ArrayList<>();
        
        // 使用正则表达式匹配表格行
        Pattern rowPattern = Pattern.compile("<tr[^>]*class=\"row[12]\"[^>]*>(.*?)</tr>", Pattern.DOTALL);
        Matcher rowMatcher = rowPattern.matcher(html);
        
        while (rowMatcher.find()) {
            String rowContent = rowMatcher.group(1);
            
            // 匹配所有<td>标签
            Pattern cellPattern = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.DOTALL);
            Matcher cellMatcher = cellPattern.matcher(rowContent);
            
            List<String> cells = new ArrayList<>();
            while (cellMatcher.find()) {
                String cellContent = cellMatcher.group(1).trim();
                // 清理HTML标签和特殊字符
                cellContent = cellContent.replaceAll("<[^>]+>", "").trim();
                cellContent = cellContent.replace("&nbsp;", " ");
                cells.add(cellContent);
            }
            
            // 确保有足够的列 (至少9列)
            if (cells.size() >= 9) {
                String date = cells.get(0);      // 净值日期
                String nav = cells.get(3);       // 最新单位净值
                String accNav = cells.get(4);    // 最新累计净值
                String growth = cells.get(8);    // 当日增长率
                
                // 添加数据前进行基本验证
                if (!date.isEmpty() && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    rows.add(new Nav(date, nav, accNav, growth));
                }
            }
        }
        
        return rows;
    }

    /** 写入 Excel */
    private static void writeExcel(List<Nav> list) throws Exception {
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream("国金量化多因子_2020-2025净值.xlsx")) {
            Sheet sheet = wb.createSheet("净值");
            Row head = sheet.createRow(0);
            String[] title = {"净值日期", "单位净值", "累计净值", "当日增长率(%)"};
            for (int i = 0; i < title.length; i++) head.createCell(i).setCellValue(title[i]);

            for (int i = 0; i < list.size(); i++) {
                Nav n = list.get(i);
                Row r = sheet.createRow(i + 1);
                r.createCell(0).setCellValue(n.date.toString());
                r.createCell(1).setCellValue(n.nav);
                r.createCell(2).setCellValue(n.accNav);
                r.createCell(3).setCellValue(n.growth);
            }
            for (int c = 0; c < 4; c++) sheet.autoSizeColumn(c);
            wb.write(out);
        }
    }

    /** 内部行对象 */
    public static class Nav {
        public final LocalDate date;
        public final double nav;
        public final double accNav;
        public final double growth;
        
        public Nav(String d, String n, String an, String g) {
            this.date   = LocalDate.parse(d, FMT);
            this.nav    = Double.parseDouble(n.isEmpty() ? "0" : n);
            this.accNav = Double.parseDouble(an.isEmpty() ? "0" : an);
            this.growth = Double.parseDouble(
                    g.isEmpty() || g.equals("---") ? "0" : g.replace("%", "").replace(" ", ""));
        }
        
        // Getters
        public LocalDate getDate() {
            return date;
        }
        
        public double getNav() {
            return nav;
        }
        
        public double getAccNav() {
            return accNav;
        }
        
        public double getGrowth() {
            return growth;
        }
    }
}
