package com.sky.aspect;

import com.sky.annotation.AutoFillAnno;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // TODO AOP知识点和反射知识点复习

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFillAnno)")
    public void autoFillPointCut(){}

    @Before(value="autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        /*Object[] args = joinPoint.getArgs();
        String kind = joinPoint.getKind();
        Signature signature = joinPoint.getSignature();
        Object target = joinPoint.getTarget();
        Object aThis = joinPoint.getThis();
        SourceLocation sourceLocation = joinPoint.getSourceLocation();
        JoinPoint.StaticPart staticPart = joinPoint.getStaticPart();

        log.info(Arrays.toString(args));
        log.info(kind);
        log.info(signature.toString());
        log.info(target.toString());
        log.info(aThis.toString());
        log.info(sourceLocation.toString());
        log.info(staticPart.toString());*/

        MethodSignature signature=(MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFillAnno autoFill = signature.getMethod().getAnnotation(AutoFillAnno.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获取操作类型


        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}
