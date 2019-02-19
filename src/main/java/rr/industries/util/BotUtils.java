/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;


import java.util.regex.Pattern;

import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;

public class BotUtils {
    public static boolean tryInt(String i) {
        return i.matches("^(\\+|-)?\\d+$");
    }

    public static String htmlToDiscord(String input) {
        return unescapeHtml4(input).replace("<b>", "**").replace("</b>", "**").replace("<i>", "*").replace("</i>", "*");
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

    public static String startsWithVowel(String input, String ifYes, String ifNo, boolean addInput) {
        Pattern vowel = Pattern.compile("^[aAeEiIoOuU].*");
        return (vowel.matcher(input).find() ? ifYes : ifNo) + (addInput ? input : "");
    }

    public static Permissions toPerms(int level) {
        for (Permissions perm : Permissions.values()) {
            if (level == perm.level) {
                return perm;
            }
        }
        throw new RuntimeException("Illegal bot perm tried to be parsed: " + level);
    }

    public static String numberExtension(int number) {
        StringBuilder result = new StringBuilder(Integer.toString(number));
        number = number % 10;
        switch (number) {
            case 1:
                result.append("st");
                break;
            case 2:
                result.append("nd");
                break;
            case 3:
                result.append("rd");
                break;
            default:
                result.append("th");
                break;
        }
        return result.toString();
    }
}
