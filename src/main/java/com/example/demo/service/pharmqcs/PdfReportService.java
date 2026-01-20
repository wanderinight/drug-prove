// PdfReportService.java - PDF报告检索服务
package com.example.demo.service.pharmqcs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PdfReportService {

    @Value("${report.storage.path}")
    private String reportStoragePath;

    // PDF文件名模式：D4_Demo_Report_20260116_200636.pdf
    private static final Pattern PDF_PATTERN =
            Pattern.compile(".*_(\\d{8})_(\\d{6})\\.pdf$", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 按日期范围检索报告
     * @param dateRange 格式：YYYYMMDD 或 YYYYMMDD-YYYYMMDD
     */
    public List<Map<String, Object>> searchReportsByDate(String dateRange) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // 解析日期范围
            LocalDate startDate;
            LocalDate endDate;

            if (dateRange.contains("-")) {
                String[] parts = dateRange.split("-");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("日期范围格式错误，应为YYYYMMDD-YYYYMMDD");
                }
                startDate = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
                endDate = LocalDate.parse(parts[1].trim(), DATE_FORMATTER);
            } else {
                startDate = LocalDate.parse(dateRange.trim(), DATE_FORMATTER);
                endDate = startDate; // 单日查询
            }

            // 确保startDate <= endDate
            if (startDate.isAfter(endDate)) {
                LocalDate temp = startDate;
                startDate = endDate;
                endDate = temp;
            }

            final LocalDate finalStartDate = startDate;
            final LocalDate finalEndDate = endDate;

            Path reportDir = Paths.get(reportStoragePath);
            if (!Files.exists(reportDir) || !Files.isDirectory(reportDir)) {
                log.warn("报告目录不存在: {}", reportStoragePath);
                return results;
            }

            // 遍历目录中的所有PDF文件
            Files.list(reportDir)
                    .filter(path -> Files.isRegularFile(path) &&
                            path.toString().toLowerCase().endsWith(".pdf"))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            Matcher matcher = PDF_PATTERN.matcher(fileName);

                            if (matcher.find()) {
                                String dateStr = matcher.group(1); // YYYYMMDD
                                LocalDate fileDate = LocalDate.parse(dateStr, DATE_FORMATTER);

                                // 检查文件日期是否在范围内
                                if ((fileDate.isEqual(finalStartDate) || fileDate.isAfter(finalStartDate)) &&
                                        (fileDate.isEqual(finalEndDate) || fileDate.isBefore(finalEndDate))) {

                                    Map<String, Object> reportInfo = extractReportInfo(path, fileName);
                                    results.add(reportInfo);
                                }
                            }
                        } catch (Exception e) {
                            log.error("处理文件失败 {}: {}", path.getFileName(), e.getMessage());
                        }
                    });

            // 按日期倒序排序
            results.sort((a, b) -> {
                String dateA = (String) a.get("date");
                String dateB = (String) b.get("date");
                String timeA = (String) a.get("time");
                String timeB = (String) b.get("time");

                String dateTimeA = dateA + timeA;
                String dateTimeB = dateB + timeB;

                return dateTimeB.compareTo(dateTimeA); // 倒序
            });

        } catch (DateTimeParseException e) {
            log.error("日期格式解析错误: {}", dateRange, e);
            throw new IllegalArgumentException("日期格式错误，请使用YYYYMMDD或YYYYMMDD-YYYYMMDD格式");
        } catch (Exception e) {
            log.error("检索报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("检索报告失败: " + e.getMessage());
        }

        return results;
    }

    /**
     * 获取所有PDF报告
     */
    public List<Map<String, Object>> getAllReports() {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            Path reportDir = Paths.get(reportStoragePath);
            if (!Files.exists(reportDir) || !Files.isDirectory(reportDir)) {
                return results;
            }

            Files.list(reportDir)
                    .filter(path -> Files.isRegularFile(path) &&
                            path.toString().toLowerCase().endsWith(".pdf"))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            Map<String, Object> reportInfo = extractReportInfo(path, fileName);
                            results.add(reportInfo);
                        } catch (Exception e) {
                            log.error("处理文件失败 {}: {}", path.getFileName(), e.getMessage());
                        }
                    });

            // 按日期倒序排序
            results.sort((a, b) -> {
                String dateA = (String) a.get("date");
                String dateB = (String) b.get("date");
                String timeA = (String) a.get("time");
                String timeB = (String) b.get("time");

                String dateTimeA = dateA + timeA;
                String dateTimeB = dateB + timeB;

                return dateTimeB.compareTo(dateTimeA); // 倒序
            });

        } catch (Exception e) {
            log.error("获取报告列表失败: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 获取报告详情
     */
    public Map<String, Object> getReportDetail(String fileName) {
        Map<String, Object> result = new HashMap<>();

        try {
            Path filePath = Paths.get(reportStoragePath, fileName);
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("报告文件不存在: " + fileName);
            }

            result = extractReportInfo(filePath, fileName);

            // 添加文件大小信息
            result.put("size", Files.size(filePath));
            result.put("sizeFormatted", formatFileSize(Files.size(filePath)));

            // 解析文件名中的设备信息
            Matcher matcher = Pattern.compile("(D4|DB)_(.*?)_Report").matcher(fileName);
            if (matcher.find()) {
                result.put("deviceType", matcher.group(1));
                result.put("deviceInfo", matcher.group(2));
            }

        } catch (Exception e) {
            log.error("获取报告详情失败 {}: {}", fileName, e.getMessage());
        }

        return result;
    }

    /**
     * 从文件中提取报告信息
     */
    private Map<String, Object> extractReportInfo(Path path, String fileName) {
        Map<String, Object> info = new HashMap<>();

        try {
            // 提取日期时间信息
            Matcher matcher = PDF_PATTERN.matcher(fileName);
            if (matcher.find()) {
                String dateStr = matcher.group(1); // YYYYMMDD
                String timeStr = matcher.group(2); // HHMMSS

                info.put("fileName", fileName);
                info.put("date", dateStr);
                info.put("time", timeStr);
                info.put("dateFormatted", formatDate(dateStr));
                info.put("timeFormatted", formatTime(timeStr));

                // 获取文件属性
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                info.put("createTime", attrs.creationTime().toString());
                info.put("lastModified", attrs.lastModifiedTime().toString());
                info.put("fileSize", attrs.size());
                info.put("fileSizeFormatted", formatFileSize(attrs.size()));

                // 构建预览URL
                String previewUrl = "/report/" + fileName;
                info.put("previewUrl", previewUrl);
                info.put("downloadUrl", previewUrl);

                // 文件路径
                info.put("filePath", path.toString());
            }
        } catch (Exception e) {
            log.error("提取报告信息失败 {}: {}", fileName, e.getMessage());
        }

        return info;
    }

    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatTime(String timeStr) {
        try {
            String formatted = timeStr.replaceAll("(\\d{2})(\\d{2})(\\d{2})", "$1:$2:$3");
            return formatted;
        } catch (Exception e) {
            return timeStr;
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}