package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect // 标识这是一个切面类
@Component // 让Spring管理这个Bean
@Slf4j // Lombok提供的日志注解，自动生成log对象
public class ServiceLogAspect {
    
    // 定义切入点：service包下的所有方法
    @Pointcut("execution(* com.example.demo.service..*.*(..))")
    public void servicePointcut() {}
    
    // 环绕通知：在方法执行前后都执行
    @Around("servicePointcut()")
    public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取类名和方法名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 记录方法开始日志
        log.info("【服务调用开始】{}.{}，参数：{}", 
                className, methodName, 
                Arrays.toString(joinPoint.getArgs()));
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录方法成功结束日志
            log.info("【服务调用成功】{}.{}，结果：{}", 
                    className, methodName, 
                    result != null ? result.toString() : "null");
            
            return result;
        } catch (Exception e) {
            // 记录方法异常日志
            log.error("【服务调用失败】{}.{}，异常：{}", 
                    className, methodName, 
                    e.getMessage(), e);
            throw e; // 重新抛出异常
        }
    }
}
