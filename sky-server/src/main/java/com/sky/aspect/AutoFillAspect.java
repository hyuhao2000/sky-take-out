package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @Description: 自动填充切面
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("AutoFill start");

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取方法参数
        Object[] args = joinPoint.getArgs();
        //获取方法参数类型
        if (args == null && args.length == 0) {
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setCreateTimeMethod.invoke(entity, now);
                setUpdateTimeMethod.invoke(entity, now);
                setCreateUserMethod.invoke(entity, currentId);
                setUpdateUserMethod.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException("Error setting fields: " + e.getMessage());
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTimeMethod.invoke(entity, now);
                setUpdateUserMethod.invoke(entity, currentId);

            }
            catch (Exception e) {
                throw new RuntimeException("Error setting fields: " + e.getMessage());
            }
        }
    }
}
