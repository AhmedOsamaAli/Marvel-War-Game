package com.marvelwargame.exceptions;

public class LeaderAbilityAlreadyUsedException extends GameActionException {
    public LeaderAbilityAlreadyUsedException() { super("Leader ability already used this game."); }
}
