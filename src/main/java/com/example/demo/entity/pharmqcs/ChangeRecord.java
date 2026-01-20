package com.example.demo.entity.pharmqcs;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上位机操作记录实体
 * @Author: qqqqzh
 * @Date: 2025-12-26
 */
@Data
@TableName("change_record")
public class ChangeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 变更时间
     */
    private LocalDateTime changeTime;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * 变更详情
     */
    private String changeDetails;
}
