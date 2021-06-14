package com.iconloop.score.annotation_processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ScoreDataProperty {
    /**
     * If type of field is primitive, nullable will be fixed 'false'
     * And for List type, nullable will be fixed 'true'
     *
     * @return boolean nullable default true
     */
    boolean nullable() default true;
    String getter() default "";
    String setter() default "";
    /**
     * If use ScoreDataObject as return type of external method,
     * JAVAEE reflect only typical getter which has 'get' prefix for representation.
     *
     * @return boolean direct default false
     */
    boolean direct() default false;
    boolean ignore() default false;
    String writeObject() default "";
    String readObject() default "";

    /**
     * In case of byte[] wrapped
     * @return boolean wrapped default false
     */
    boolean wrapped() default false;

    /**
     * If type of field is not array or List, nullableComponent will be ignored
     * If type of component is primitive type, nullableComponent will be fixed 'false'
     *
     * @return boolean nullableComponent default true
     */
    boolean nullableComponent() default true;
}
