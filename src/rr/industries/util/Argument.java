package rr.industries.util;

/**
 *
 */
//todo: add class description
public @interface Argument {

    //leaving this blank uses the default description for the Validation Type
    String description() default "";

    Validate value();

    String[] options() default {};
}
