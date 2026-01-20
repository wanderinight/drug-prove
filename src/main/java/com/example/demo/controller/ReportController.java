// ReportController.java - 新增PDF报告检索控制器
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.pharmqcs.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * 按时间范围检索PDF报告
     * @param dateRange 日期范围，格式：YYYYMMDD 或 YYYYMMDD-YYYYMMDD
     */
    @GetMapping("/search")
    public Result searchPdfReports(@RequestParam String dateRange) {
        List<Map<String, Object>> reports = pdfReportService.searchReportsByDate(dateRange);
        return Result.success(reports);
    }

    /**
     * 获取所有PDF报告列表
     */
    @GetMapping("/list")
    public Result getAllPdfReports() {
        List<Map<String, Object>> reports = pdfReportService.getAllReports();
        return Result.success(reports);
    }

    /**
     * 获取报告文件详情
     */
    @GetMapping("/detail/{fileName}")
    public Result getReportDetail(@PathVariable String fileName) {
        Map<String, Object> report = pdfReportService.getReportDetail(fileName);
        return Result.success(report);
    }
}