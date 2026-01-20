package com.example.demo.service.pharmqcs.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.pharmqcs.ChangeRecord;
import com.example.demo.mapper.pharmqcs.ChangeRecordMapper;
import com.example.demo.service.pharmqcs.ChangeRecordIService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 上位机操作记录Service实现
 * @Author: qqqqzh
 * @Date: 2025-12-26
 */
@Service
@DS("pharmqcs")  // 指定使用局域网数据源
public class ChangeRecordServiceImpl extends ServiceImpl<ChangeRecordMapper, ChangeRecord> implements ChangeRecordIService {

    @Override
    public List<ChangeRecord> getByUserId(Integer userId) {
        LambdaQueryWrapper<ChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChangeRecord::getUserId, userId)
               .orderByDesc(ChangeRecord::getChangeTime);
        return list(wrapper);
    }

    @Override
    public List<ChangeRecord> getByChangeType(String changeType) {
        LambdaQueryWrapper<ChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChangeRecord::getChangeType, changeType)
               .orderByDesc(ChangeRecord::getChangeTime);
        return list(wrapper);
    }
}
