package rr.industries.util;

import java.lang.annotation.*;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/10/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface SubCommand {
    String name();

    Permissions permLevel() default Permissions.NORMAL;

    Syntax[] Syntax();

}
