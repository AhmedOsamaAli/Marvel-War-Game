package com.marvelwargame.exceptions;

public class ChampionDisarmedException extends GameActionException {
    public ChampionDisarmedException() { super("Champion is disarmed and cannot attack."); }
}
