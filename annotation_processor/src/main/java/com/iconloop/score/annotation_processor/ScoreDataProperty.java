package com.iconloop.score.annotation_processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ScoreDataProperty {
    /**
     * In case of primitive type, nullable will be fixed 'false'
     * And for List type, nullable will be fixed 'true'
     */
    boolean nullable() default true;
    String getter() default "";
    String setter() default "";
    /**
     * If use ScoreDataObject as return type of external method,
     * JAVAEE reflect only typical getter which has 'get' prefix for representation.
     */
    boolean direct() default false;
    boolean ignore() default false;
    String writeObject() default "";
    String readObject() default "";
}
