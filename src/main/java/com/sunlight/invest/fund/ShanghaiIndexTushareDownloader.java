package com.sunlight.invest.fund;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 上证指数数据下载器（Tushare API），输出Excel
 */
public class ShanghaiIndexTushareDownloader {

    private static final String API_URL = "http://api.tushare.pro";
    private static final String TOKEN = System.getenv("TUSHARE_TOKEN") != null ? 
            System.getenv("TUSHARE_TOKEN") : "89f6d173d9147a236bfb10ede30722c25fe3c839df56f7b5589bae24"; // 替换为你的实际token
    private static final String INDEX_CODE = "000001.SH"; // 上证指数代码
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void main(String[] args) throws Exception {
        // 获取今年的开始和结束日期
        LocalDate now = LocalDate.now();
        String startDate = now.getYear() + "0101";
        String endDate = now.getYear() + "1231";
        
        String fileName = "上证指数每日涨跌_" + now.getYear() + ".xlsx";

        List<IndexData> dataList = fetchIndexData(startDate, endDate);
        writeExcel(dataList, fileName);
        System.out.println(">>> 完成！共 " + dataList.size() + " 条数据 → " + fileName);
    }

    /**
     * 从Tushare获取上证指数数据
     */
    private static List<IndexData> fetchIndexData(String startDate, String endDate) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 构造请求参数
        String requestBody = "{\n" +
                "  \"api_name\": \"index_daily\",\n" +
                "  \"token\": \"" + TOKEN + "\",\n" +
                "  \"params\": {\n" +
                "    \"ts_code\": \"" + INDEX_CODE + "\",\n" +
                "    \"start_date\": \"" + startDate + "\",\n" +
                "    \"end_date\": \"" + endDate + "\"\n" +
                "  },\n" +
                "  \"fields\": \"trade_date,open,high,low,close,change,pct_chg\"\n" +
                "}";

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode rootNode = MAPPER.readTree(responseBody);
            
            // 检查是否有错误
            int code = rootNode.get("code").asInt();
            if (code != 0) {
                String errorMsg = rootNode.has("msg") ? rootNode.get("msg").asText() : "未知错误";
                throw new RuntimeException("API错误: " + errorMsg);
            }

            // 解析数据
            JsonNode dataNode = rootNode.get("data");
            JsonNode fieldsNode = dataNode.get("fields");
            JsonNode itemsNode = dataNode.get("items");

            List<IndexData> dataList = new ArrayList<>();
            for (JsonNode itemNode : itemsNode) {
                IndexData data = new IndexData();
                data.tradeDate = LocalDate.parse(itemNode.get(0).asText(), DATE_FORMATTER);
                data.open = new BigDecimal(itemNode.get(1).asText());
                data.high = new BigDecimal(itemNode.get(2).asText());
                data.low = new BigDecimal(itemNode.get(3).asText());
                data.close = new BigDecimal(itemNode.get(4).asText());
                data.change = new BigDecimal(itemNode.get(5).asText()); // 涨跌点数
                data.pctChg = new BigDecimal(itemNode.get(6).asText()); // 涨跌幅(%)
                dataList.add(data);
            }

            return dataList;
        }
    }

    /**
     * 将数据写入Excel文件
     */
    private static void writeExcel(List<IndexData> dataList, String fileName) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("上证指数");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"交易日期", "开盘价", "最高价", "最低价", "收盘价", "涨跌点数", "涨跌幅(%)"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (IndexData data : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.tradeDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            row.createCell(1).setCellValue(data.open.doubleValue());
            row.createCell(2).setCellValue(data.high.doubleValue());
            row.createCell(3).setCellValue(data.low.doubleValue());
            row.createCell(4).setCellValue(data.close.doubleValue());
            row.createCell(5).setCellValue(data.change.doubleValue());
            row.createCell(6).setCellValue(data.pctChg.doubleValue());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    /**
     * 上证指数数据实体类
     */
    private static class IndexData {
        LocalDate tradeDate; // 交易日期
        BigDecimal open;     // 开盘价
        BigDecimal high;     // 最高价
        BigDecimal low;      // 最低价
        BigDecimal close;    // 收盘价
        BigDecimal change;   // 涨跌点数
        BigDecimal pctChg;   // 涨跌幅(%)
    }
}