package com.example.demo.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
@Documented
public @interface AuditLog {
    String operationType() default ""; // 操作类型
    String module() default "";        // 模块名称
    String description() default "";   // 操作描述
}