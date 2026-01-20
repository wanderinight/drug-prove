package com.example.demo.controller.pharmqcs;

import com.example.demo.common.Result;
import com.example.demo.service.pharmqcs.ChangeRecordIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 上位机操作记录Controller
 * @Author: qqqqzh
 * @Date: 2025-12-26
 */
@RestController
@RequestMapping("/changeRecord")
public class ChangeRecordController {

    @Autowired
    private ChangeRecordIService changeRecordService;

    /**
     * 查询所有操作记录
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success(changeRecordService.list());
    }

}
