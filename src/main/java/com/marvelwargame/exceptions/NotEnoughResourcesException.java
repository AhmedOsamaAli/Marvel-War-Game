package com.marvelwargame.exceptions;

public class NotEnoughResourcesException extends GameActionException {
    public NotEnoughResourcesException() { super("Not enough action points or mana."); }
}
