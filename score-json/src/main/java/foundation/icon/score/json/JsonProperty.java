package foundation.icon.score.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface JsonProperty {
    String value() default "";
    String getter() default "";
    String setter() default "";
    boolean direct() default false;
    boolean ignore() default false;
    String parser() default "";
    String toJson() default "";
}
