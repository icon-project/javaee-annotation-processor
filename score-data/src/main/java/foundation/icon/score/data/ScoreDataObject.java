package foundation.icon.score.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [NOTE] When using ScoreDataObject as return type of external method,
 * JAVAEE reflect only typical getter which has 'get' prefix for representation.
 * And not allowed that include float, double, Float, Double, List type
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ScoreDataObject {
    String suffix() default "Sdo";
    String writeObject() default "writeObject";
    String readObject() default "readObject";
    boolean wrapList() default true;

    /**
     * begin field of optional fields for read,
     * check reader.hasNext before read
     * @return String begin field to optional
     */
    String beginOfOptionalFields() default "";
}
