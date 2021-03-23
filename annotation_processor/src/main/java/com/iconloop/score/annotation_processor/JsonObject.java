package com.iconloop.score.annotation_processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface JsonObject {
    String suffix() default "Json";
    String parse() default "parse";
    String toJsonObject() default "toJsonObject";
    //TODO boolean ignoreUnknownProperty() default true;
}
