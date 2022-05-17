package com.paperfly.imageShare.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD) //注解放置的目标位置,METHOD是可注解在方法级别上
@Retention(RetentionPolicy.RUNTIME) //注解在哪个阶段执行
@Documented
public @interface OperLog {
    String operType() default ""; // 操作类型（新增，删除，查询，更新）

    String operModule() default "";  // 操作模块

    String operDesc() default "";  // 操作代码
}