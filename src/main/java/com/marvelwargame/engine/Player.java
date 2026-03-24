package com.marvelwargame.engine;

import com.marvelwargame.model.world.Champion;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player with a team of champions and a designated leader.
 */
public final class Player {

    private final String name;
    private final List<Champion> team;
    private Champion leader;

    public Player(String name) {
        this.name = name;
        this.team = new ArrayList<>();
    }

    public String getName()          { return name; }
    public List<Champion> getTeam()  { return team; }
    public Champion getLeader()      { return leader; }
    public void setLeader(Champion c){ this.leader = c; }

    @Override public String toString() { return name; }
}
