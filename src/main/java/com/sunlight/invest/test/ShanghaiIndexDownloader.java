package com.sunlight.invest.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 上证指数数据下载器（搜狐接口），输出Excel
 */
public class ShanghaiIndexDownloader {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String start = "20250101";
        String end   = "20251231";
        String fileName = "上证指数每日涨跌_" + start + "-" + end + ".xlsx";

        List<IndexDay> list = fetchIndex(start, end);
        writeExcel(list, fileName);
        System.out.println(">>> 完成！共 " + list.size() + " 条数据 → " + fileName);
    }

    /** 1. 拉取数据（网易接口） */
    public static List<IndexDay> fetchIndex(String start, String end) throws Exception {
        String url = "https://q.stock.sohu.com/hisHq?code=cn_000001&start=" + start + "&end=" + end +
                "&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        try (Response resp = client.newCall(request).execute()) {
            String jsonp = resp.body().string().trim();
            String json  = jsonp.replaceAll("^historySearchHandler\\((.*)\\);?$", "$1");
            List<Map<String, Object>> tmp = MAPPER.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {});
            List<List<String>> hq = (List<List<String>>) tmp.get(0).get("hq");
            return hq.stream().map(arr -> {
                IndexDay day = new IndexDay();
                day.date   = LocalDate.parse(arr.get(0), DateTimeFormatter.ISO_LOCAL_DATE);
                day.open   = new BigDecimal(arr.get(1));
                day.close  = new BigDecimal(arr.get(2));
                day.change = new BigDecimal(arr.get(3)); // 涨跌点数
                day.pct    = day.change.divide(day.open, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                return day;
            }).collect(Collectors.toList());
        }
    }

    /** 2. 写入 Excel */
    private static void writeExcel(List<IndexDay> list, String fileName) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("上证指数");
        Row header = sheet.createRow(0);
        String[] titles = {"日期", "开盘", "收盘", "涨跌点数", "涨跌幅(%)"};
        for (int i = 0; i < titles.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titles[i]);
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (IndexDay d : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(d.date.toString());
            row.createCell(1).setCellValue(d.open.doubleValue());
            row.createCell(2).setCellValue(d.close.doubleValue());
            row.createCell(3).setCellValue(d.change.doubleValue());
            row.createCell(4).setCellValue(d.pct.doubleValue());
        }

        // 自动列宽
        for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            wb.write(fos);
        }
        wb.close();
    }

    /** 3. 内部 DTO */
    public static class IndexDay {
        LocalDate date;
        BigDecimal open;
        BigDecimal close;
        BigDecimal change; // 点数
        BigDecimal pct;    // 百分比
        
        public IndexDay() {}
        
        // Getters
        public LocalDate getDate() {
            return date;
        }
        
        public BigDecimal getOpen() {
            return open;
        }
        
        public BigDecimal getClose() {
            return close;
        }
        
        public BigDecimal getChange() {
            return change;
        }
        
        public BigDecimal getPct() {
            return pct;
        }
    }
}