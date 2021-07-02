package com.iconloop.score.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface JsonObject {
    String suffix() default "Json";
    String parse() default "parse";
    String toJson() default "toJson";
    //TODO boolean nullable() default true;
    //TODO boolean ignoreUnknownProperty() default true;
}
