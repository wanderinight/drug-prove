package com.example.demo.aop;

import com.example.demo.annotation.AuditLog;
import com.example.demo.entity.OperationAudit;
import com.example.demo.repository.OperationAuditRepository;
import com.example.demo.service.AsyncAuditService;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


@Aspect
@Component
@RequiredArgsConstructor
public class OperationAuditAspect {
    
    private final AsyncAuditService asyncAuditService;
    
    // 拦截所有带有@AuditLog注解的方法
    @Around("@annotation(com.example.demo.annotation.AuditLog)")
    public Object auditOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        
        // 2. 获取当前用户信息（基于你的Spring Security配置）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        //  String staffCode = authentication != null ? authentication.getStaffcode() : "unknown";
        
        // 3. 准备审计记录
        OperationAudit audit = new OperationAudit();
        audit.setOperationType(auditLog.operationType());
        audit.setOperationModule(auditLog.module());
        audit.setOperatorName(username);
        audit.setOperationTime(LocalDateTime.now());
        audit.setMethodName(method.getName());
        audit.setClassName(joinPoint.getTarget().getClass().getSimpleName());
        
        try {
            // 4. 执行目标方法
            Object result = joinPoint.proceed();
            
            // 5. 记录成功
            audit.setOperationStatus("SUCCESS");
            audit.setOperationContent(auditLog.description() + "成功");
            asyncAuditService.saveAuditLog(audit);
            
            return result;
        } catch (Exception e) {
            // 6. 记录失败
            audit.setOperationStatus("FAILED");
            audit.setOperationContent(auditLog.description() + "失败: " + e.getMessage());
           asyncAuditService.saveAuditLog(audit);
            
            throw e;
        }
    }
}