package rr.industries.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/5/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandInfo {
    String commandName();

    String helpText();

    Permissions permLevel() default Permissions.NORMAL;

    boolean deleteMessage() default true;
}