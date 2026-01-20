package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import com.example.demo.common.Result;
import com.example.demo.dto.ReportGenerateRequest;
import com.example.demo.dto.ReportPrintRequest;
import com.example.demo.entity.InspectionData;
import com.example.demo.service.InspectDataService;
import com.example.demo.service.ReportService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/inspect-data")
public class InspectDataController {
    @Autowired
    private InspectDataService inspectDataService;

    @Autowired
    private ReportService reportService;

    private static final Logger log = LoggerFactory.getLogger(InspectDataController.class);
    // 返回所有设备的最新检测数据
    @GetMapping("/showrecent-all")
    public ResponseEntity<Result> getLatestInspections() {
        List<InspectionData> inspections = inspectDataService.getLatestInspectionsWithDeviceInfo();
        return ResponseEntity.ok(Result.success(inspections));
    }
    
    // 返回指定设备的最新检测数据
    @GetMapping("/showrecent-bydevice")
    public ResponseEntity<Result> getLatestByDevice(@RequestParam String deviceCode) {
        Optional<InspectionData> inspection = inspectDataService.getLatestInspectionByDeviceId(deviceCode);
        return inspection.map(i -> ResponseEntity.ok(Result.success(i)))
                         .orElseGet(() -> ResponseEntity.ok(Result.error("200","未找到该设备的检测数据")));
    }
    //返回时间段内的检测数据
    @GetMapping("/details-by-time-range")
    public ResponseEntity<Result> getInspectDataBytime(
            // @RequestParam String startTime, 
            // @RequestParam String endTime) throws Exception
        @RequestParam 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        LocalDateTime startTime, 

        @RequestParam 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        LocalDateTime endTime) throws Exception {

        ZonedDateTime start = startTime.atZone(ZonedDateTime.now().getZone());
        ZonedDateTime end = endTime.atZone(ZonedDateTime.now().getZone());
        
        List<InspectionData> inspection = inspectDataService.getInspectionsByTimeRange(start, end);
        return ResponseEntity.ok(Result.success(inspection));

    }
    
    
    //返回检测报告批-次-产品-时间
     // 生成按批次的报告并下载
    @GetMapping("/report/batch")
    public ResponseEntity<byte[]> generateBatchReport(@RequestParam String batchNo) throws Exception {
        List<InspectionData> dataList = inspectDataService.getInspectionsByBatch(batchNo);
        
        // 生成PDF报告
        byte[] pdfBytes = reportService.generatePdfReport(dataList, "按批次报告", "批次号: " + batchNo);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);//告诉浏览器返回一个pdf文件
        headers.setContentDispositionFormData("attachment", //attachment表示下载
            "药品检测报告_批次_" + batchNo + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    // 生成按产品的报告并下载
    @GetMapping("/report/product")
    public ResponseEntity<byte[]> generateProductReport(@RequestParam String productId) throws Exception {
        List<InspectionData> dataList = inspectDataService.getInspectionsByProduct(productId);
        
        // 生成PDF报告
        byte[] pdfBytes = reportService.generatePdfReport(dataList, "按产品报告", "产品ID: " + productId);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "药品检测报告_产品_" + productId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    // 生成按时间范围的报告并下载
    @GetMapping("/report/time-range")//时间模式！！！！！
    public ResponseEntity<byte[]> generateTimeRangeReport(
            // @RequestParam String startTime, 
            // @RequestParam String endTime) throws Exception
        @RequestParam 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        LocalDateTime startTime, 

        @RequestParam 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        LocalDateTime endTime) throws Exception {

        ZonedDateTime start = startTime.atZone(ZonedDateTime.now().getZone());
        ZonedDateTime end = endTime.atZone(ZonedDateTime.now().getZone());
        
        List<InspectionData> dataList = inspectDataService.getInspectionsByTimeRange(start, end);
        
        // 生成PDF报告
        byte[] pdfBytes = reportService.generatePdfReport(dataList, "按时间范围报告", 
            "时间范围: " + start.toString() + " 至 " + end.toString());
        
        // 保存PDF报告到服务器
        String fileName = "药品检测报告_时间范围_" + start.toLocalDate() + "_至_" + end.toLocalDate() + "_" + 
            UUID.randomUUID().toString().substring(0, 8) + ".pdf";
        try {
            String savedPath = reportService.savePdfReport(pdfBytes, fileName);
            log.info("PDF报告已保存到: {}", savedPath);
        } catch (Exception e) {
            log.error("保存PDF报告失败: {}", e.getMessage(), e);
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "药品检测报告_时间范围_" + start.toLocalDate() + "_至_" + end.toLocalDate() + "_" + 
            UUID.randomUUID().toString().substring(0, 8) + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/report/generate")
    public ResponseEntity<Result> generateReportByFilters(@RequestBody ReportGenerateRequest request) {
        try {
            ZonedDateTime start = parseDateTime(request.getStartTime());
            ZonedDateTime end = parseDateTime(request.getEndTime());

            List<InspectionData> dataList = inspectDataService.getInspectionsByFilters(
                normalizeList(request.getDeviceCodes()),
                normalizeList(request.getBatchNos()),
                normalizeList(request.getProductIds()),
                start,
                end
            );

            if (dataList.isEmpty()) {
                return ResponseEntity.ok(Result.error("404", "未查询到符合条件的检测数据"));
            }

            String criteria = buildCriteriaDescription(request, start, end);
            String title = StringUtils.hasText(request.getReportTitle()) ? request.getReportTitle() : "组合条件报告";

            byte[] pdfBytes = reportService.generatePdfReport(dataList, title, criteria);

            boolean saveFile = request.getSaveFile() == null || Boolean.TRUE.equals(request.getSaveFile());
            String fileName = "药品检测报告_组合查询_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 6) + ".pdf";
            String savedPath = null;
            if (saveFile) {
                savedPath = reportService.savePdfReport(pdfBytes, fileName);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("fileName", fileName);
            payload.put("savedPath", savedPath);
            payload.put("previewBase64", Base64.getEncoder().encodeToString(pdfBytes));
            payload.put("recordCount", dataList.size());
            payload.put("criteria", criteria);

            return ResponseEntity.ok(Result.success(payload));
        } catch (Exception e) {
            log.error("组合条件生成报告失败", e);
            return ResponseEntity.ok(Result.error("500", "生成测量报告失败: " + e.getMessage()));
        }
    }

    @PostMapping("/report/print")
    public ResponseEntity<Result> printReport(@RequestBody ReportPrintRequest request) {
        log.info("收到打印请求，文件: {}, 路径: {}, 份数: {}", 
            request.getFileName(), request.getFilePath(), request.getCopies());
        return ResponseEntity.ok(Result.success("打印接口已预留，后续可接入打印机服务"));
    }

    private ZonedDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value);
        } catch (Exception e) {
            LocalDateTime local = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return local.atZone(ZoneId.systemDefault());
        }
    }

    private List<String> normalizeList(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
    }

    private String buildCriteriaDescription(ReportGenerateRequest request, ZonedDateTime start, ZonedDateTime end) {
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(request.getDeviceCodes())) {
            builder.append("设备: ").append(String.join(",", request.getDeviceCodes())).append("；");
        }
        if (!CollectionUtils.isEmpty(request.getBatchNos())) {
            builder.append("批次: ").append(String.join(",", request.getBatchNos())).append("；");
        }
        if (!CollectionUtils.isEmpty(request.getProductIds())) {
            builder.append("产品: ").append(String.join(",", request.getProductIds())).append("；");
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (start != null || end != null) {
            builder.append("时间: ");
            builder.append(start != null ? start.withZoneSameInstant(ZoneId.systemDefault()).format(fmt) : "不限");
            builder.append(" 至 ");
            builder.append(end != null ? end.withZoneSameInstant(ZoneId.systemDefault()).format(fmt) : "不限");
            builder.append("；");
        }
        if (!StringUtils.hasText(builder.toString())) {
            builder.append("条件: 全部数据；");
        }
        return builder.toString();
    }
}
