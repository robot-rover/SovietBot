package rr.industries.util;

import java.lang.annotation.*;

/**
 * @author robot_rover
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface Syntax {
    String helpText();

    Arguments[] args();

    String[] options() default {};
}
