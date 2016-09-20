package rr.industries.util;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/19/2016
 */
public @interface ArgSet {
    Arguments arg();

    //Number of Occurrences Required
    //Nonzero Positive is amount
    //Zero means 1-Infinity allowed
    int num() default 1;
}
