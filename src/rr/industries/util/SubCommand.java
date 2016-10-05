package rr.industries.util;

import java.lang.annotation.*;

/**
 * @author robot_rover
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface SubCommand {
    String name();

    Permissions permLevel() default Permissions.NORMAL;

    Syntax[] Syntax();

}
