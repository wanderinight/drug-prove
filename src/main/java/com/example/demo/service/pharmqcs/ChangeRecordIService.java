package com.example.demo.service.pharmqcs;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.pharmqcs.ChangeRecord;

import java.util.List;

/**
 * 上位机操作记录Service接口
 * @Author: qqqqzh
 * @Date: 2025-12-26
 */
public interface ChangeRecordIService extends IService<ChangeRecord> {

    /**
     * 根据用户ID查询操作记录
     */
    List<ChangeRecord> getByUserId(Integer userId);

    /**
     * 根据变更类型查询操作记录
     */
    List<ChangeRecord> getByChangeType(String changeType);
}
