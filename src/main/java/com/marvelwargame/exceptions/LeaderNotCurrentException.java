package com.marvelwargame.exceptions;

public class LeaderNotCurrentException extends GameActionException {
    public LeaderNotCurrentException() { super("Current champion is not the player's leader."); }
}
