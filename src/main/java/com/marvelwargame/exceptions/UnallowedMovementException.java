package com.marvelwargame.exceptions;

public class UnallowedMovementException extends GameActionException {
    public UnallowedMovementException() { super("Movement not allowed."); }
}
