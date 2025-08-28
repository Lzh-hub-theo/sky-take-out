package com.sky.annotation;

import com.sky.enumeration.OperationType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value= ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
@Component
public @interface AutoFillAnno {
    OperationType value();
}
