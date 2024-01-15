package com.sky.Aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.Signature;
import java.text.Annotation;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..))&& @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointCut(){}

    @Before("AutoFillPointCut()")
    public void AutoFill(JoinPoint joinPoint){
        log.info("正在公共字段字段");

        MethodSignature signature=(MethodSignature) joinPoint.getSignature();
        AutoFill annotation=signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType=annotation.value();

        Object[] args=joinPoint.getArgs();
        if(args==null||args.length==0)return;
        Object object=args[0];

        LocalDateTime now=LocalDateTime.now();
        Long id=BaseContext.getCurrentId();

        try{
            if(operationType==OperationType.INSERT){

                Method setCreateTime=object.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser=object.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateTime.invoke(object,now);
                setCreateUser.invoke(object,id);
            }
            Method setUpdateTime=object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser=object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateUser.invoke(object,id);
            setUpdateTime.invoke(object,now);
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
