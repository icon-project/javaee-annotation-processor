package com.iconloop.score.annotation_processor;

import com.iconloop.score.lib.PropertiesDB;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ScorePropertiesDBProperty {
    String value() default "";
    String getter() default "";
    String setter() default "";

    boolean ignore() default false;
    Class<? extends PropertiesDB> db() default PropertiesDB.class;
}
