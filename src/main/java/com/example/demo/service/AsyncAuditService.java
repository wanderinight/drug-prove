package com.example.demo.service;

import com.example.demo.entity.OperationAudit;
import com.example.demo.repository.OperationAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAuditService {
    
    private final OperationAuditRepository auditRepository;
    
    @Async // 标记为异步方法
    public void saveAuditLog(OperationAudit audit) {
        try {
            auditRepository.save(audit);
            log.debug("审计日志保存成功：{}", audit);
        } catch (Exception e) {
            log.error("审计日志保存失败：{}", e.getMessage(), e);
            // 这里可以添加备用存储逻辑，比如存入本地文件
        }
    }
}