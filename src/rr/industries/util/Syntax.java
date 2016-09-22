package rr.industries.util;

import java.lang.annotation.*;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/10/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface Syntax {
    String helpText();

    Arguments[] args();

    String[] options() default {};
}
