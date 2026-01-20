package com.example.demo.controller.pharmqcs;

import com.example.demo.common.Result;
import com.example.demo.service.pharmqcs.PdfSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PDF同步Controller
 */
@RestController
@RequestMapping("/pdf")
public class PdfSyncController {

    @Autowired
    private PdfSyncService pdfSyncService;

    /**
     * 获取远程PDF文件列表
     */
    @GetMapping("/remote/list")
    public Result listRemotePdf() {
        List<String> files = pdfSyncService.listRemotePdfFiles();
        return Result.success(files);
    }

    /**
     * 手动触发同步
     */
    @PostMapping("/sync")
    public Result syncPdf() {
        int count = pdfSyncService.manualSync();
        return Result.success(count, "同步完成，新增" + count + "个文件");
    }

    /**
     * 下载指定文件
     */
    @PostMapping("/download")
    public Result downloadPdf(@RequestParam String fileName) {
        boolean success = pdfSyncService.downloadFile(fileName);
        if (success) {
            return Result.success(null, "下载成功");
        }
        return Result.error("500", "下载失败");
    }
}
