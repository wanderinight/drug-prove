package com.example.demo.service.pharmqcs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF报告同步服务
 * 从上位机Nginx服务器拉取PDF文件到本地
 */
@Slf4j
@Service
public class PdfSyncService {

    @Value("${pdf.remote.url:http://localhost/reports/}")
    private String remoteUrl;

    @Value("${pdf.local.path:./resouce/report/}")
    private String localPath;

    /**
     * 定时同步PDF文件（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)
    public void syncPdfFiles() {
        log.info("开始同步PDF文件...");
        try {
            List<String> pdfFiles = listRemotePdfFiles();
            int syncCount = 0;
            for (String fileName : pdfFiles) {
                if (downloadIfNotExists(fileName)) {
                    syncCount++;
                }
            }
            log.info("PDF同步完成，新增{}个文件", syncCount);
        } catch (Exception e) {
            log.error("PDF同步失败: {}", e.getMessage());
        }
    }

    /**
     * 手动触发同步
     */
    public int manualSync() {
        List<String> pdfFiles = listRemotePdfFiles();
        int syncCount = 0;
        for (String fileName : pdfFiles) {
            if (downloadIfNotExists(fileName)) {
                syncCount++;
            }
        }
        return syncCount;
    }

    /**
     * 获取远程PDF文件列表
     */
    public List<String> listRemotePdfFiles() {
        List<String> pdfFiles = new ArrayList<>();
        try {
            URL url = new URL(remoteUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder html = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    html.append(line);
                }
                reader.close();

                // 解析Nginx autoindex页面，提取PDF文件名
                Pattern pattern = Pattern.compile("href=\"([^\"]+\\.pdf)\"", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(html.toString());
                while (matcher.find()) {
                    pdfFiles.add(matcher.group(1));
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            log.error("获取远程PDF列表失败: {}", e.getMessage());
        }
        return pdfFiles;
    }

    /**
     * 下载文件（如果本地不存在）
     */
    private boolean downloadIfNotExists(String fileName) {
        try {
            Path localFile = Paths.get(localPath, fileName);
            
            // 确保目录存在
            Files.createDirectories(localFile.getParent());
            
            // 如果文件已存在则跳过
            if (Files.exists(localFile)) {
                return false;
            }

            // 下载文件
            String fileUrl = remoteUrl + fileName;
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     OutputStream out = Files.newOutputStream(localFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                log.info("下载成功: {}", fileName);
                return true;
            }
            conn.disconnect();
        } catch (Exception e) {
            log.error("下载文件失败 {}: {}", fileName, e.getMessage());
        }
        return false;
    }

    /**
     * 下载指定文件
     */
    public boolean downloadFile(String fileName) {
        try {
            Path localFile = Paths.get(localPath, fileName);
            Files.createDirectories(localFile.getParent());

            String fileUrl = remoteUrl + fileName;
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     OutputStream out = Files.newOutputStream(localFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return true;
            }
            conn.disconnect();
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
        }
        return false;
    }
}
