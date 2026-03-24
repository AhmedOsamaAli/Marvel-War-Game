package com.marvelwargame.exceptions;

public class InvalidTargetException extends GameActionException {
    public InvalidTargetException() { super("Invalid target."); }
    public InvalidTargetException(String msg) { super(msg); }
}
