/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import java.util.Calendar;
import java.util.regex.Pattern;

public class BotUtils {
    public static boolean tryInt(String i) {
        boolean parsable = true;
        try {
            Integer.parseInt(i);
        } catch (NumberFormatException ex) {
            parsable = false;
        }
        return parsable;
    }

    public static boolean tryDouble(String i) {
        boolean parsable = true;
        try {
            Double.parseDouble(i);
        } catch (NumberFormatException ex) {
            parsable = false;
        }
        return parsable;
    }

    public static String startsWithVowel(String input, String ifYes, String ifNo) {
        Pattern vowel = Pattern.compile("^[aAeEiIoOuU].*");
        if (vowel.matcher(input).find()) {
            return ifYes + input;
        } else {
            return ifNo + input;
        }
    }

    public static Permissions toPerms(int level) {
        for (Permissions perm : Permissions.values()) {
            if (level == perm.level) {
                return perm;
            }
        }
        throw new IndexOutOfBoundsException("The level " + level + "is out of the range of Permissions");
    }

    public static String getPrettyTime(Calendar time) {
        return (time.get(Calendar.HOUR) == 0 ? "12" : time.get(Calendar.HOUR)) + ":" + String.format("%2s", time.get(Calendar.MINUTE)).replace(" ", "0") + " " + (time.get(Calendar.AM_PM) == 1 ? "PM" : "AM");
    }
}
