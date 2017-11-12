package com.github.adamint.Exceptions;

public class OAuthException extends Exception {
    private int errorCode;

    public OAuthException(String msg, int errorCode) {
        super(errorCode == 0 ? msg : "Http Error (" + errorCode + ") - " + msg);
        this.errorCode = errorCode;
    }
    public int getErrorCode(){
        return errorCode;
    }
}
