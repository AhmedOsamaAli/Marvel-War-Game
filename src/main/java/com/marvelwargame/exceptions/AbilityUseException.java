package com.marvelwargame.exceptions;

public class AbilityUseException extends GameActionException {
    public AbilityUseException() { super("Cannot use this ability right now."); }
    public AbilityUseException(String msg) { super(msg); }
}
