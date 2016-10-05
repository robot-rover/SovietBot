package rr.industries.util;

import java.lang.annotation.*;

/**
 * @author robot_rover
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String commandName();

    String helpText();

    Permissions permLevel() default Permissions.NORMAL;

    boolean deleteMessage() default true;
}