/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import rr.industries.Exceptions.BotException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.util.regex.Pattern;

public class BotUtils {
    public static boolean tryInt(String i) {
        return i.matches("^\\d+$");
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

    public static String numberExtension(int number) {
        StringBuilder result = new StringBuilder(number);
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

    public static <T> T bufferRequest(IExRequest<T> exRequest) throws BotException {
        RequestBuffer.IRequest<Entry<T, BotException>> request = () -> {
            try {
                return new Entry<>(exRequest.request(), null);
            } catch (BotException ex) {
                return new Entry<>(null, ex);
            }
        };
        Entry<T, BotException> returnEntry = RequestBuffer.request(request).get();
        if (returnEntry.second() != null)
            throw returnEntry.second();
        else
            return returnEntry.first();

    }

    public static void bufferRequest(IVoidExRequest exRequest) throws BotException {
        RequestBuffer.IRequest<BotException> request = () -> {
            try {
                exRequest.request();
            } catch (BotException ex) {
                return ex;
            }
            return null;
        };
        BotException returnEntry = RequestBuffer.request(request).get();
        if (returnEntry != null)
            throw returnEntry;

    }

    @FunctionalInterface
    public interface IExRequest<T> {

        /**
         * This is called when the request is attempted.
         *
         * @return The result of this request, if any.
         * @throws RateLimitException
         * @throws BotException
         */
        T request() throws RateLimitException, BotException;
    }

    @FunctionalInterface
    public interface IVoidExRequest<T> {

        /**
         * This is called when the request is attempted.
         *
         * @return The result of this request, if any.
         * @throws RateLimitException
         * @throws BotException
         */
        void request() throws RateLimitException, BotException;
    }
}
