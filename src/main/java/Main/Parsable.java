/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

/**
 * @author Sam
 */
class Parsable {
    static boolean tryInt(String i) {
        boolean parsable = true;
        try {
            Integer.parseInt(i);
        } catch (NumberFormatException ex) {
            parsable = false;
        }
        return parsable;
    }

    static boolean tryDouble(String i) {
        boolean parsable = true;
        try {
            Double.parseDouble(i);
        } catch (NumberFormatException ex) {
            parsable = false;
        }
        return parsable;
    }
}
