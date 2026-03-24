package com.marvelwargame.engine;

import com.marvelwargame.engine.events.EventBus;
import com.marvelwargame.engine.events.GameEvent;
import com.marvelwargame.exceptions.*;
import com.marvelwargame.model.abilities.*;
import com.marvelwargame.model.effects.*;
import com.marvelwargame.model.world.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Core game engine. Governs all game rules:
 *  - Turn ordering by champion speed
 *  - Movement, attack, ability casting
 *  - Board state (5×5 grid)
 *  - Ally/enemy classification (Hero ↔ Villain deal 1.5× damage; same type = reduced damage)
 *  - Leader abilities (once per game per player)
 *  - Win condition
 *
 * Events are published through {@link EventBus} for the UI to react to.
 */
public final class Game {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final int BOARD_SIZE = 5;
    private static final int COVERS_COUNT = 5;

    // ── State ─────────────────────────────────────────────────────────────────
    private final Player firstPlayer;
    private final Player secondPlayer;
    private boolean firstLeaderUsed;
    private boolean secondLeaderUsed;

    /** board[row][col] – row 0 is the bottom (player-1 side), row 4 is the top (player-2 side). */
    private final Object[][] board;
    private final PriorityQueue turnOrder;
    private final EventBus eventBus;

    // ── Constructor ───────────────────────────────────────────────────────────

    public Game(Player p1, Player p2) throws UnallowedMovementException {
        this.firstPlayer  = p1;
        this.secondPlayer = p2;
        this.firstLeaderUsed  = false;
        this.secondLeaderUsed = false;
        this.board     = new Object[BOARD_SIZE][BOARD_SIZE];
        this.turnOrder = new PriorityQueue(BOARD_SIZE * 2);
        this.eventBus  = new EventBus();

        placeChampions();
        placeCovers();
        prepareChampionTurns();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Player getFirstPlayer()   { return firstPlayer; }
    public Player getSecondPlayer()  { return secondPlayer; }
    public Object[][] getBoard()     { return board; }
    public PriorityQueue getTurnOrder() { return turnOrder; }
    public EventBus getEventBus()    { return eventBus; }
    public boolean isFirstLeaderUsed()  { return firstLeaderUsed; }
    public boolean isSecondLeaderUsed() { return secondLeaderUsed; }

    public Champion getCurrentChampion() {
        return turnOrder.peekMin();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void placeChampions() throws UnallowedMovementException {
        List<Champion> team1 = firstPlayer.getTeam();
        for (int i = 0; i < team1.size(); i++) {
            Champion c = team1.get(i);
            board[0][i + 1] = c;
            c.placeAt(new Point(0, i + 1));
        }
        List<Champion> team2 = secondPlayer.getTeam();
        for (int i = 0; i < team2.size(); i++) {
            Champion c = team2.get(i);
            board[4][i + 1] = c;
            c.placeAt(new Point(4, i + 1));
        }
    }

    private void placeCovers() {
        int placed = 0;
        while (placed < COVERS_COUNT) {
            int x = (int)(Math.random() * 3) + 1;   // rows 1–3 only
            int y = (int)(Math.random() * BOARD_SIZE);
            if (board[x][y] == null) {
                board[x][y] = new Cover(x, y);
                placed++;
            }
        }
    }

    /**
     * Fill the turn-order queue with active champions sorted by speed.
     * Called once at game start and whenever the queue becomes empty.
     */
    private void prepareChampionTurns() {
        List<Champion> all = new ArrayList<>();
        all.addAll(firstPlayer.getTeam());
        all.addAll(secondPlayer.getTeam());
        for (Champion c : all) {
            if (c.getCondition() != Condition.KNOCKEDOUT) {
                turnOrder.insert(c);
            }
        }
    }

    // ── Turn Management ───────────────────────────────────────────────────────

    /**
     * End the current champion's turn, decrement their cooldowns/effects,
     * then pop them from the queue. If the queue empties, refill it.
     */
    public void endTurn() {
        Champion current = getCurrentChampion();
        current.endTurn();
        turnOrder.remove();
        if (turnOrder.isEmpty()) {
            prepareChampionTurns();
        }
        // Start next champion's turn
        Champion next = getCurrentChampion();
        next.startTurn();
        // If next is knocked-out (edge case), skip
        while (next.getCondition() == Condition.KNOCKEDOUT) {
            turnOrder.remove();
            if (turnOrder.isEmpty()) prepareChampionTurns();
            next = getCurrentChampion();
            next.startTurn();
        }
        eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_TURN_STARTED, next,
                next.getName() + "'s turn begins!"));
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    public void move(Direction d) throws UnallowedMovementException, NotEnoughResourcesException {
        Champion c = getCurrentChampion();
        if (c.getCurrentActionPoints() < 1) throw new NotEnoughResourcesException();
        if (c.getCondition() == Condition.ROOTED || c.getCondition() == Condition.INACTIVE)
            throw new UnallowedMovementException();

        Point old = c.getLocation();
        int nx = old.x, ny = old.y;
        switch (d) {
            case UP    -> nx++;
            case DOWN  -> nx--;
            case RIGHT -> ny++;
            case LEFT  -> ny--;
        }

        if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE || board[nx][ny] != null)
            throw new UnallowedMovementException();

        board[old.x][old.y] = null;
        board[nx][ny] = c;
        c.placeAt(new Point(nx, ny));
        c.setCurrentActionPoints(c.getCurrentActionPoints() - 1);

        eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_MOVED, c,
                c.getName() + " moved " + d));
    }

    // ── Attack ────────────────────────────────────────────────────────────────

    public void attack(Direction d) throws NotEnoughResourcesException, ChampionDisarmedException,
            InvalidTargetException {
        Champion attacker = getCurrentChampion();
        if (attacker.getCurrentActionPoints() < 2) throw new NotEnoughResourcesException();

        // Deduct APs first (even if Disarmed, so being disarmed wastes your turn)
        attacker.setCurrentActionPoints(attacker.getCurrentActionPoints() - 2);

        for (Effect e : attacker.getAppliedEffects()) {
            if (e instanceof Disarm) throw new ChampionDisarmedException();
        }

        int dx = 0, dy = 0;
        switch (d) {
            case UP    -> dx = 1;
            case DOWN  -> dx = -1;
            case RIGHT -> dy = 1;
            case LEFT  -> dy = -1;
        }

        performRangedAttack(attacker, dx, dy);
        resolveKnockouts();

        eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_ATTACKED, attacker,
                attacker.getName() + " attacks " + d));
    }

    private void performRangedAttack(Champion attacker, int dx, int dy) throws InvalidTargetException {
        int range = attacker.getAttackRange();
        int ix = attacker.getLocation().x + dx;
        int iy = attacker.getLocation().y + dy;
        boolean isFirstTeam = firstPlayer.getTeam().contains(attacker);

        for (int r = 0; r < range && ix >= 0 && ix < BOARD_SIZE && iy >= 0 && iy < BOARD_SIZE; r++) {
            Object cell = board[ix][iy];
            if (cell == null) { ix += dx; iy += dy; continue; }

            if (cell instanceof Cover cover) {
                cover.setCurrentHP(cover.getCurrentHP() - attacker.getAttackDamage());
                if (cover.isDestroyed()) {
                    board[ix][iy] = null;
                    eventBus.publish(GameEvent.of(GameEvent.Type.COVER_DESTROYED, attacker,
                            "Cover at (" + ix + "," + iy + ") destroyed"));
                }
                return; // cover blocks further attack
            }

            if (cell instanceof Champion target) {
                boolean targetIsAlly = isFirstTeam
                        ? firstPlayer.getTeam().contains(target)
                        : secondPlayer.getTeam().contains(target);
                if (targetIsAlly) throw new InvalidTargetException("Cannot attack an ally!");

                applyAttackDamage(attacker, target);
                return;
            }
            break;
        }
    }

    private void applyAttackDamage(Champion attacker, Champion target) {
        // Dodge: 50% chance to negate
        for (Effect e : target.getAppliedEffects()) {
            if (e instanceof Dodge) {
                if (Math.random() < 0.5) {
                    eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_DAMAGED, attacker, target,
                            target.getName() + " dodged the attack!"));
                    return;
                }
                break;
            }
        }
        // Shield: absorb one hit
        for (Effect e : List.copyOf(target.getAppliedEffects())) {
            if (e instanceof Shield) {
                e.remove(target);
                eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_DAMAGED, attacker, target,
                        target.getName() + "'s shield absorbed the blow!"));
                return;
            }
        }

        // Type-based damage modifier:
        // Hero vs Villain (or Villain vs Hero) = 1.5× damage
        // Same type = normal (original logic: checker returns true for same type)
        int rawDamage = attacker.getAttackDamage();
        int damage = sameType(attacker, target) ? rawDamage : (int)(rawDamage * 1.5);

        target.setCurrentHP(target.getCurrentHP() - damage);
        eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_DAMAGED, attacker, target,
                attacker.getName() + " dealt " + damage + " damage to " + target.getName()));
    }

    // Same type = both Hero, both Villain, or both AntiHero
    private boolean sameType(Champion a, Champion b) {
        return (a instanceof Hero && b instanceof Hero)
            || (a instanceof Villain && b instanceof Villain)
            || (a instanceof AntiHero && b instanceof AntiHero);
    }

    // ── Ability Casting ───────────────────────────────────────────────────────

    /** SELFTARGET / TEAMTARGET / SURROUND abilities (no direction or point needed). */
    public void castAbility(Ability a) throws NotEnoughResourcesException, AbilityUseException,
            InvalidTargetException, CloneNotSupportedException {
        Champion c = getCurrentChampion();
        validateAbilityPreconditions(c, a);

        AreaOfEffect aoe = a.getCastArea();
        List<Damageable> targets;

        if (aoe == AreaOfEffect.SELFTARGET) {
            targets = List.of(c);
        } else if (a instanceof HealingAbility
                || (a instanceof CrowdControlAbility cc && cc.getEffect().getType() == EffectType.BUFF)) {
            targets = aoeFilter(getAllies(c), c, a);
        } else if (a instanceof CrowdControlAbility cc && cc.getEffect().getType() == EffectType.DEBUFF) {
            targets = aoeFilter(getEnemies(c), c, a);
        } else if (a instanceof DamagingAbility da) {
            if (aoe == AreaOfEffect.SURROUND) {
                targets = aoeFilter(getDamageables(c), c, a);
            } else {
                targets = aoeFilter(getEnemies(c), c, a);
            }
        } else {
            throw new AbilityUseException("Cannot determine targets for this ability.");
        }

        a.execute(targets);
        if (a instanceof DamagingAbility) {
            targets.forEach(this::resolveDeadDamageable);
        }
        deductAbilityCost(c, a);
        resolveKnockouts();

        eventBus.publish(GameEvent.of(GameEvent.Type.ABILITY_CAST, c,
                c.getName() + " used " + a.getName()));
    }

    /** DIRECTIONAL ability. */
    public void castAbility(Ability a, Direction d) throws NotEnoughResourcesException,
            AbilityUseException, CloneNotSupportedException {
        Champion c = getCurrentChampion();
        validateAbilityPreconditions(c, a);

        List<Damageable> targets = getDirectionalTargets(d, c, a);
        a.execute(targets);
        if (a instanceof DamagingAbility) {
            targets.forEach(this::resolveDeadDamageable);
        }
        deductAbilityCost(c, a);
        resolveKnockouts();

        eventBus.publish(GameEvent.of(GameEvent.Type.ABILITY_CAST, c,
                c.getName() + " used " + a.getName() + " towards " + d));
    }

    /** SINGLETARGET ability aimed at a specific board cell (x = row, y = col). */
    public void castAbility(Ability a, int x, int y) throws NotEnoughResourcesException,
            AbilityUseException, InvalidTargetException, CloneNotSupportedException {
        Champion c = getCurrentChampion();
        validateAbilityPreconditions(c, a);

        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE)
            throw new InvalidTargetException("Target out of bounds.");
        if (board[x][y] == null)
            throw new InvalidTargetException("No target at (" + x + "," + y + ").");
        if (!(a instanceof DamagingAbility) && board[x][y] instanceof Cover)
            throw new InvalidTargetException("Cannot apply this ability to a cover.");

        int dist = Math.abs(x - c.getLocation().x) + Math.abs(y - c.getLocation().y);
        if (dist > a.getCastRange())
            throw new AbilityUseException("Target out of cast range.");

        if (a instanceof DamagingAbility && isAlly(c, board[x][y]))
            throw new InvalidTargetException("Cannot damage an ally.");
        if (a instanceof HealingAbility && !isAlly(c, board[x][y]))
            throw new InvalidTargetException("Can only heal allies.");
        if (a instanceof CrowdControlAbility cc) {
            boolean debuff = cc.getEffect().getType() == EffectType.DEBUFF;
            if (debuff && isAlly(c, board[x][y]))
                throw new InvalidTargetException("Cannot debuff an ally.");
            if (!debuff && !isAlly(c, board[x][y]))
                throw new InvalidTargetException("Cannot buff an enemy.");
        }

        List<Damageable> targets = List.of((Damageable) board[x][y]);
        a.execute(targets);
        if (a instanceof DamagingAbility) {
            resolveDeadDamageable((Damageable) board[x][y]);
        }
        deductAbilityCost(c, a);
        resolveKnockouts();

        eventBus.publish(GameEvent.of(GameEvent.Type.ABILITY_CAST, c,
                c.getName() + " used " + a.getName() + " on (" + x + "," + y + ")"));
    }

    // ── Leader Ability ────────────────────────────────────────────────────────

    public void useLeaderAbility() throws LeaderAbilityAlreadyUsedException, LeaderNotCurrentException {
        Champion current = getCurrentChampion();
        Player player = isFirstTeam(current) ? firstPlayer : secondPlayer;
        boolean alreadyUsed = isFirstTeam(current) ? firstLeaderUsed : secondLeaderUsed;

        if (alreadyUsed) throw new LeaderAbilityAlreadyUsedException();
        if (player.getLeader() != current) throw new LeaderNotCurrentException();

        List<Champion> allies = isFirstTeam(current)
                ? firstPlayer.getTeam() : secondPlayer.getTeam();
        current.useLeaderAbility(allies);

        if (isFirstTeam(current)) firstLeaderUsed = true;
        else secondLeaderUsed = true;

        eventBus.publish(GameEvent.of(GameEvent.Type.LEADER_ABILITY_USED, current,
                current.getName() + " unleashes their LEADER ABILITY!"));
    }

    // ── Win Condition ─────────────────────────────────────────────────────────

    /**
     * Returns the winning player, or null if the game is still ongoing.
     */
    public Player checkGameOver() {
        boolean p1alive = firstPlayer.getTeam().stream()
                .anyMatch(c -> c.getCondition() != Condition.KNOCKEDOUT);
        boolean p2alive = secondPlayer.getTeam().stream()
                .anyMatch(c -> c.getCondition() != Condition.KNOCKEDOUT);

        if (p1alive && !p2alive) return firstPlayer;
        if (!p1alive && p2alive) return secondPlayer;
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateAbilityPreconditions(Champion c, Ability a)
            throws NotEnoughResourcesException, AbilityUseException {
        if (c.getMana() < a.getManaCost() || c.getCurrentActionPoints() < a.getRequiredActionPoints())
            throw new NotEnoughResourcesException();
        if (a.getCurrentCooldown() > 0)
            throw new AbilityUseException(a.getName() + " is on cooldown.");
        if (c.getCondition() == Condition.INACTIVE)
            throw new AbilityUseException("Champion is stunned.");
        for (Effect e : c.getAppliedEffects()) {
            if (e instanceof Silence)
                throw new AbilityUseException("Champion is silenced.");
        }
    }

    private void deductAbilityCost(Champion c, Ability a) {
        c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());
        c.setMana(c.getMana() - a.getManaCost());
        a.setCurrentCooldown(a.getBaseCooldown());
    }

    /** AoE filter: returns targets from candidates that fall within cast range / area. */
    private List<Damageable> aoeFilter(List<Damageable> candidates, Champion caster, Ability a) {
        List<Damageable> result = new ArrayList<>();
        AreaOfEffect aoe = a.getCastArea();

        if (aoe == AreaOfEffect.TEAMTARGET) {
            for (Damageable d : candidates) {
                if (manhattanDist(caster.getLocation(), d.getLocation()) <= a.getCastRange()) {
                    result.add(d);
                }
            }
            // Caster itself for healing/buff team-target
            if (!(a instanceof CrowdControlAbility cc && cc.getEffect().getType() == EffectType.DEBUFF)) {
                result.add(caster);
            }
        } else if (aoe == AreaOfEffect.SURROUND) {
            Point cp = caster.getLocation();
            for (Damageable d : candidates) {
                int dr = Math.abs(d.getLocation().x - cp.x);
                int dc = Math.abs(d.getLocation().y - cp.y);
                if (dr <= 1 && dc <= 1 && !(d.getLocation().equals(cp))) {
                    result.add(d);
                }
            }
        }
        return result;
    }

    private List<Damageable> getDirectionalTargets(Direction d, Champion caster, Ability a) {
        List<Damageable> result = new ArrayList<>();
        int dx = 0, dy = 0;
        switch (d) {
            case UP    -> dx = 1;
            case DOWN  -> dx = -1;
            case RIGHT -> dy = 1;
            case LEFT  -> dy = -1;
        }
        int ix = caster.getLocation().x + dx, iy = caster.getLocation().y + dy;
        int remaining = a.getCastRange();
        while (remaining-- > 0 && ix >= 0 && ix < BOARD_SIZE && iy >= 0 && iy < BOARD_SIZE) {
            Object cell = board[ix][iy];
            if (cell instanceof Damageable da) result.add(da);
            ix += dx; iy += dy;
        }
        return result;
    }

    private List<Damageable> getEnemies(Champion c) {
        List<Champion> team = isFirstTeam(c) ? secondPlayer.getTeam() : firstPlayer.getTeam();
        return new ArrayList<>(team);
    }

    private List<Damageable> getAllies(Champion c) {
        List<Champion> team = isFirstTeam(c) ? firstPlayer.getTeam() : secondPlayer.getTeam();
        return new ArrayList<>(team);
    }

    private List<Damageable> getDamageables(Champion c) {
        List<Damageable> list = getEnemies(c);
        for (Object[] row : board) {
            for (Object cell : row) {
                if (cell instanceof Cover cov) list.add(cov);
            }
        }
        return list;
    }

    public boolean isFirstTeam(Champion c) {
        return firstPlayer.getTeam().contains(c);
    }

    private boolean isAlly(Champion caster, Object target) {
        if (!(target instanceof Champion t)) return false;
        return isFirstTeam(caster) == isFirstTeam(t);
    }

    private int manhattanDist(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void resolveDeadDamageable(Damageable d) {
        if (d.getCurrentHP() == 0) {
            board[d.getLocation().x][d.getLocation().y] = null;
            if (d instanceof Champion c) {
                c.setCondition(Condition.KNOCKEDOUT);
                turnOrder.remove(c);
                eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_KNOCKED_OUT, c,
                        c.getName() + " has been knocked out!"));
            }
        }
    }

    private void resolveKnockouts() {
        List<Champion> all = new ArrayList<>();
        all.addAll(firstPlayer.getTeam());
        all.addAll(secondPlayer.getTeam());
        for (Champion c : all) {
            if (c.getCurrentHP() == 0 && c.getCondition() != Condition.KNOCKEDOUT) {
                c.setCondition(Condition.KNOCKEDOUT);
                if (c.getLocation() != null) {
                    board[c.getLocation().x][c.getLocation().y] = null;
                }
                turnOrder.remove(c);
                eventBus.publish(GameEvent.of(GameEvent.Type.CHAMPION_KNOCKED_OUT, c,
                        c.getName() + " is knocked out!"));
            }
        }
        Player winner = checkGameOver();
        if (winner != null) {
            eventBus.publish(GameEvent.of(GameEvent.Type.GAME_OVER, null,
                    winner.getName() + " wins the battle!"));
        }
    }
}
