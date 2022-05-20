package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import exceptions.*;
import model.abilities.*;
import model.effects.*;
import model.world.*;

public class Game {

	private Player firstPlayer;
	private Player secondPlayer;
	private boolean firstLeaderAbilityUsed;
	private boolean secondLeaderAbilityUsed;
	private Object[][] board;
	private PriorityQueue turnOrder;
	private static ArrayList<Champion> availableChampions;
	private static ArrayList<Ability> availableAbilities;
	private final static int BOARDWIDTH = 5;
	private final static int BOARDHEIGHT = 5;

	public Game(Player p1, Player p2) throws CloneNotSupportedException, UnallowedMovementException {
		this.firstLeaderAbilityUsed = false;
		this.secondLeaderAbilityUsed = false;
		this.firstPlayer = p1;
		this.secondPlayer = p2;
		turnOrder = new PriorityQueue(6);
		board = new Object[BOARDHEIGHT][BOARDWIDTH];
		placeChampions();
		placeCovers();
		availableChampions = new ArrayList<Champion>();
		availableAbilities = new ArrayList<Ability>();
		prepareChampionTurns();

	}

	public boolean isFirstLeaderAbilityUsed() {
		return firstLeaderAbilityUsed;
	}

	public boolean isSecondLeaderAbilityUsed() {
		return secondLeaderAbilityUsed;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public Object[][] getBoard() {
		return board;
	}

	public PriorityQueue getTurnOrder() {
		return turnOrder;
	}

	public static ArrayList<Champion> getAvailableChampions() {
		return availableChampions;
	}

	public static ArrayList<Ability> getAvailableAbilities() {
		return availableAbilities;
	}

	public static int getBoardwidth() {
		return BOARDWIDTH;
	}

	public static int getBoardheight() {
		return BOARDHEIGHT;
	}

	public static void loadChampions(String filepath) throws IOException {
		availableChampions = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			if (content[0].equals("H")) {
				Hero h = new Hero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				h.getAbilities().add(getAbilityFromAvailable(content[8]));
				h.getAbilities().add(getAbilityFromAvailable(content[9]));
				h.getAbilities().add(getAbilityFromAvailable(content[10]));
				availableChampions.add(h);
			} else if (content[0].equals("V")) {
				Villain h = new Villain(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				h.getAbilities().add(getAbilityFromAvailable(content[8]));
				h.getAbilities().add(getAbilityFromAvailable(content[9]));
				h.getAbilities().add(getAbilityFromAvailable(content[10]));
				availableChampions.add(h);
			} else if (content[0].equals("A")) {
				AntiHero h = new AntiHero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				h.getAbilities().add(getAbilityFromAvailable(content[8]));
				h.getAbilities().add(getAbilityFromAvailable(content[9]));
				h.getAbilities().add(getAbilityFromAvailable(content[10]));
				availableChampions.add(h);
			}
			line = br.readLine();
		}
		br.close();
	}

	private static Ability getAbilityFromAvailable(String string) {
		for (Ability ability : availableAbilities) {
			if (ability.getName().equals(string))
				return ability;
		}
		return null;
	}

	public static void loadAbilities(String filepath) throws IOException {
		availableAbilities = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			if (content[0].equals("CC")) {
				availableAbilities.add(new CrowdControlAbility(content[1], Integer.parseInt(content[2]),
						Integer.parseInt(content[4]), Integer.parseInt(content[3]), AreaOfEffect.valueOf(content[5]),
						Integer.parseInt(content[6]), getEffect(content[7], Integer.parseInt(content[8]))));
			} else if (content[0].equals("DMG")) {
				availableAbilities.add(new DamagingAbility(content[1], Integer.parseInt(content[2]),
						Integer.parseInt(content[4]), Integer.parseInt(content[3]), AreaOfEffect.valueOf(content[5]),
						Integer.parseInt(content[6]), Integer.parseInt(content[7])));
			} else if (content[0].equals("HEL")) {
				availableAbilities.add(new HealingAbility(content[1], Integer.parseInt(content[2]),
						Integer.parseInt(content[4]), Integer.parseInt(content[3]), AreaOfEffect.valueOf(content[5]),
						Integer.parseInt(content[6]), Integer.parseInt(content[7])));
			}
			line = br.readLine();
		}
		br.close();
	}

	private static Effect getEffect(String name, int duration) {
		if (name.equals("Dodge"))
			return new Dodge(duration);
		if (name.equals("Disarm"))
			return new Disarm(duration);
		if (name.equals("Embrace"))
			return new Embrace(duration);
		if (name.equals("Stun"))
			return new Stun(duration);
		if (name.equals("Shield"))
			return new Shield(duration);
		if (name.equals("Shock"))
			return new Shock(duration);
		if (name.equals("PowerUp"))
			return new PowerUp(duration);
		if (name.equals("SpeedUp"))
			return new SpeedUp(duration);
		if (name.equals("Silence"))
			return new Silence(duration);
		if (name.equals("Root"))
			return new Root(duration);
		return null;
	}

	public void placeChampions() throws UnallowedMovementException {
		for (int i = 0; i < firstPlayer.getTeam().size(); i++) {
			board[0][i + 1] = firstPlayer.getTeam().get(i);
			firstPlayer.getTeam().get(i).setLocation(new Point(0, i + 1));
			;
		}
		for (int i = 0; i < secondPlayer.getTeam().size(); i++) {
			board[4][i + 1] = secondPlayer.getTeam().get(i);
			secondPlayer.getTeam().get(i).setLocation(new Point(4, i + 1));
			;
		}
	}

	public void placeCovers() {
		int i = 0;
		while (i < 5) {
			int x = (int) (Math.random() * 3) + 1;
			int y = (int) (Math.random() * 5);
			if (board[x][y] == null) {
				board[x][y] = new Cover(x, y);
				i++;
			}
		}
	}

	public Champion getCurrentChampion() {
		return (Champion) turnOrder.peekMin();

	}

	public Player checkGameOver() {
		boolean f1 = false, f2 = false;

		for (Champion c : firstPlayer.getTeam()) {
			if (c.getCondition() != Condition.KNOCKEDOUT) {
				f1 = true;
				break;
			}
		}
		for (Champion c : secondPlayer.getTeam()) {
			if (c.getCondition() != Condition.KNOCKEDOUT) {
				f2 = true;
				break;
			}
		}
		if (f1 && !f2) {
			return firstPlayer;
		} else if (!f1 && f2) {

			return secondPlayer;
		} else {
			return null;
		}

	}

	public void move(Direction d) throws UnallowedMovementException, NotEnoughResourcesException {
		Point oldLocation = this.getCurrentChampion().getLocation();
		int x = oldLocation.x;
		int y = oldLocation.y;

		if (getCurrentChampion().getCurrentActionPoints() <= 0) {
			throw new NotEnoughResourcesException();
		}

		if (this.getCurrentChampion().getCondition() == Condition.ROOTED)
			throw new UnallowedMovementException();

		if (d == Direction.UP) {
			if (x + 1 >= 5) {
				throw new UnallowedMovementException();
			} else {
				if (board[x + 1][y] == null) {
					x++;
				} else {
					throw new UnallowedMovementException();
				}
			}
		} else if ((d == Direction.DOWN)) {
			if (x - 1 < 0) {
				throw new UnallowedMovementException();
			} else {
				if (board[x - 1][y] == null) {
					x--;
				} else {
					throw new UnallowedMovementException();
				}
			}
		} else if ((d == Direction.RIGHT)) {
			if (y + 1 >= 5) {
				throw new UnallowedMovementException();
			} else if (board[x][y + 1] == null) {
				y++;
			} else {
				throw new UnallowedMovementException();
			}
		}

		else {
			if (y - 1 < 0) {
				throw new UnallowedMovementException();
			} else {
				if (board[x][y - 1] == null) {
					y--;
				} else {
					throw new UnallowedMovementException();
				}
			}
		}
		board[oldLocation.x][oldLocation.y] = null;
		oldLocation.x = x;
		oldLocation.y = y;
		board[x][y] = getCurrentChampion();
		this.getCurrentChampion().setLocation(oldLocation);
		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 1);
	}

	private static boolean checker(Champion a, Champion b) {
		if (a instanceof Hero && b instanceof Hero) {
			return true;
		}
		if (a instanceof AntiHero && b instanceof AntiHero) {
			return true;
		}
		if (a instanceof Villain && b instanceof Villain) {
			return true;
		}
		return false;

	}

	public static boolean checkEnough(Champion c, Ability a) {
		if (c.getMana() < a.getManaCost() || c.getCurrentActionPoints() < a.getRequiredActionPoints()) {
			return false;
		}
		return true;
	}

	private void validateAttack(int xx, int yy) throws InvalidTargetException {

		int range = getCurrentChampion().getAttackRange();
		int ix = getCurrentChampion().getLocation().x + xx, iy = getCurrentChampion().getLocation().y + yy;
		boolean ok = false;
		if (firstPlayer.getTeam().contains(getCurrentChampion())) {
			ok = true;
		}

		while (ix < 5 && ix >= 0 && iy < 5 && iy >= 0) {
			if (range == 0) {
				break;
			}
			range--;
			if (board[ix][iy] == null) {
				ix += xx;
				iy += yy;
				continue;
			}
			if (!(firstPlayer.getTeam().contains((board[ix][iy])) ^ ok)) {
				getEnemy(getCurrentChampion()).contains((board[ix][iy]));

				throw new InvalidTargetException();
			} else {
				

				if (board[ix][iy] instanceof Cover) {

					Cover cc = (Cover) board[ix][iy];
					cc.setCurrentHP(cc.getCurrentHP() - (getCurrentChampion().getAttackDamage()));
					if (cc.getCurrentHP() <= 0)

						board[ix][iy] = null;

				}

				else if (checker(getCurrentChampion(), (Champion) board[ix][iy])) {

					Champion ch = (Champion) board[ix][iy];
					if (checkShield(ch))
						break;
					if (checkDodge(ch)) {
						int rnd = (int) (Math.random() * 2);
						if (rnd == 0)
							ch.setCurrentHP(ch.getCurrentHP() - (getCurrentChampion().getAttackDamage()));
						break;

					}
					if (!checkShield(ch))
						ch.setCurrentHP(ch.getCurrentHP() - (getCurrentChampion().getAttackDamage()));

					if (ch.getCurrentHP() <= 0) {
						ch.setCondition(Condition.KNOCKEDOUT);
						board[ix][iy] = null;

					}
				} else {
					Champion ch = (Champion) board[ix][iy];
					if (checkShield(ch))
						break;

					if (checkDodge(ch)) {
						int rnd = (int) (Math.random() * 2);
						if (rnd == 0)
							ch.setCurrentHP(ch.getCurrentHP() - (getCurrentChampion().getAttackDamage()));
						break;
					}
					if (!checkShield(ch))

						ch.setCurrentHP(ch.getCurrentHP() - (int) (1.5 * getCurrentChampion().getAttackDamage()));

					if (ch.getCurrentHP() <= 0) {
						ch.setCondition(Condition.KNOCKEDOUT);
						board[ix][iy] = null;
					}
				}

				break;
			}

		}
	}

	private boolean checkShield(Champion c) {
		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Shield) {
				e.remove(c);
				return true;
			}
		}
		return false;

	}

	private boolean checkDodge(Champion c) {
		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Dodge) {
				return true;
			}
		}
		return false;

	}

	public void attack(Direction d) throws InvalidTargetException, ChampionDisarmedException,
			NotEnoughResourcesException, CloneNotSupportedException {

		Champion c = getCurrentChampion();
		if (c.getCurrentActionPoints() < 2) {
			throw new NotEnoughResourcesException();
		}

		c.setCurrentActionPoints(c.getCurrentActionPoints() - 2);
		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Disarm) {
				throw new ChampionDisarmedException();
			}
		}

		if (d == Direction.DOWN) {
			validateAttack(-1, 0);
		} else if (d == Direction.UP) {
			validateAttack(1, 0);
		} else if (d == Direction.RIGHT) {
			validateAttack(0, 1);

		} else {
			validateAttack(0, -1);
		}
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);

	}

	public ArrayList<Damageable> AOF(ArrayList<Damageable> d, Champion c, Ability a) {
		ArrayList<Damageable> champions = new ArrayList<Damageable>();

		if (a.getCastArea() == AreaOfEffect.TEAMTARGET) {
			for (Damageable x : d) {
				if (isInRange(a.getCastRange(), x.getLocation(), c.getLocation())) {
					champions.add(x);
				}
			}
			if (!(a instanceof CrowdControlAbility
					&& ((CrowdControlAbility) a).getEffect().getType() == EffectType.DEBUFF))

				champions.add((Damageable) c);
		}

		if (a.getCastArea() == AreaOfEffect.SURROUND) {
			int x = c.getLocation().x;
			int y = c.getLocation().y;
			for (int i = x - 1; i <= x + 1; i++) {
				for (int j = y - 1; j <= y + 1; j++) {

					for (Damageable r : d) {
						if (r.getLocation().x == i && r.getLocation().y == j) {
							if ((i == x && j == y)) {
								continue;
							}
							champions.add(r);
						}
					}

				}
			}
		}

		return champions;
	}

	public static boolean isInRange(int range, Point p1, Point p2) {
		int distance = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
		if (p1 != p2 && distance <= range) {
			return true;
		}
		return false;
	}

	public void removeDamageable(Damageable d) {
		if (d.getCurrentHP() == 0) {
			board[d.getLocation().x][d.getLocation().y] = null;

			if (d instanceof Champion) {
				((Champion) d).setCondition(Condition.KNOCKEDOUT);
				if (isFirstPlayer((Champion) d)) {
					firstPlayer.getTeam().remove((Champion) d);
				}
				removeFromQ((Champion) d);
			}
		}
	}

	public boolean isFirstPlayer(Champion c) {
		for (Champion x : firstPlayer.getTeam()) {
			if (x == c) {
				return true;
			}
		}
		return false;
	}

	public EffectType ccType(Ability a) {
		if (a instanceof CrowdControlAbility) {
			return ((CrowdControlAbility) a).getEffect().getType();
		} else {
			return null;
		}
	}

	public void castAbility(Ability a, int x, int y) throws NotEnoughResourcesException, AbilityUseException,
			InvalidTargetException, CloneNotSupportedException {
		final Champion c = getCurrentChampion();

		if (x > 5 || x < 0 || y > 5 || y < 0)
			throw new InvalidTargetException();

		if (!checkEnough(c, a)) {
			throw new NotEnoughResourcesException();
		}

		if (a.getCurrentCooldown() > 0) {
			throw new AbilityUseException();
		}

		if (c.getCondition() == Condition.INACTIVE) {
			throw new AbilityUseException();
		}

		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Silence) {
				throw new AbilityUseException();
			}
		}
		if (board[x][y] == null) {
			throw new InvalidTargetException();
		}

		if ((!(a instanceof DamagingAbility)) && (board[x][y] instanceof Cover)) {
			throw new InvalidTargetException();
		}

		if (a instanceof DamagingAbility && member(c, (Champion) board[x][y])) {
			throw new InvalidTargetException();
		}

		if (a instanceof HealingAbility && !member(c, (Champion) board[x][y])) {
			throw new InvalidTargetException();
		}
		if (a.getCastArea() != AreaOfEffect.SELFTARGET) {
			if (x == c.getLocation().x && y == c.getLocation().y) {

				if (a instanceof DamagingAbility && x == c.getLocation().x && y == c.getLocation().y) {
					throw new InvalidTargetException();
				}
				if (a instanceof CrowdControlAbility && x == c.getLocation().x && y == c.getLocation().y
						&& ((CrowdControlAbility) a).getEffect().getType() == EffectType.DEBUFF) {
					throw new InvalidTargetException();
				}

			}

		}

		if (Math.abs(x - this.getCurrentChampion().getLocation().x)
				+ Math.abs(y - this.getCurrentChampion().getLocation().y) > a.getCastRange())
			throw new AbilityUseException();

		if (ccType(a) != null) {
			if (a instanceof CrowdControlAbility
					&& (ccType(a) == EffectType.DEBUFF && member((Champion) board[x][y], c))) {
				throw new InvalidTargetException();
			} else if (a instanceof CrowdControlAbility
					&& (ccType(a) == EffectType.BUFF && !member((Champion) board[x][y], c))) {
				throw new InvalidTargetException();
			}
		}

		ArrayList<Damageable> target = new ArrayList<Damageable>();
		target.add((Damageable) board[x][y]);
		a.execute(target);
		if (a instanceof DamagingAbility) {
			removeDamageable((Damageable) board[x][y]);
		}
		c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());
		c.setMana(c.getMana() - a.getManaCost());
		a.setCurrentCooldown(a.getBaseCooldown());
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);
	}


	public ArrayList<Damageable> getEnemy(Champion c) {
		ArrayList<Damageable> d = new ArrayList<Damageable>();
		ArrayList<Champion> team = null;
		if (isFirstPlayer(c)) {
			team = secondPlayer.getTeam();
		}

		else {
			team = firstPlayer.getTeam();
		}

		for (Champion x : team) {
			d.add((Damageable) x);
		}

		return d;
	}

	
	public ArrayList<Damageable> getAlly(Champion c) {
		ArrayList<Damageable> d = new ArrayList();
		ArrayList<Champion> team = null;
		if (isFirstPlayer(c)) {
			team = firstPlayer.getTeam();
		}

		else {
			team = secondPlayer.getTeam();
		}

		for (Champion x : team) {
			d.add((Damageable) x);

		}
		return d;
	}

	public ArrayList<Damageable> getAlly2(Champion c) {
		ArrayList<Damageable> allies = new ArrayList();
		for (int i = 0; i < BOARDWIDTH; i++) {
			for (int j = 0; j < BOARDHEIGHT; j++) {
				if (board[i][j] != null) {
					if (board[i][j] instanceof Champion) {
						Champion x = (Champion) board[i][j];
						if (member(c, x)) {
							allies.add((Damageable) x);
						}
					}
				}
			}
		}
		return allies;
	}


	public boolean member(Champion c1, Champion c2) {

		return ((firstPlayer.getTeam().contains((c2)) ^ firstPlayer.getTeam().contains(c1)) ? false : true);

	}

	

	public void castAbility(Ability a) throws NotEnoughResourcesException, AbilityUseException,
			CloneNotSupportedException, InvalidTargetException {
		Champion c = getCurrentChampion();

		if (!checkEnough(c, a)) {
			throw new NotEnoughResourcesException();
		}

		c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());

		if (a.getCurrentCooldown() > 0) {
			throw new AbilityUseException();
		}

		if (c.getCondition() == Condition.INACTIVE) {
			throw new AbilityUseException();
		}

		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Silence) {
				throw new AbilityUseException();
			}
		}

		if (a.getCastArea() == AreaOfEffect.SELFTARGET) {
			ArrayList<Damageable> target = new ArrayList<Damageable>();
			if (board[c.getLocation().x][c.getLocation().y] != null)
				target.add((Damageable) board[c.getLocation().x][c.getLocation().y]);
			else {
				
				throw new InvalidTargetException();
			}

			a.execute(target);

		}

		if ((a instanceof HealingAbility || (a instanceof CrowdControlAbility
				&& ((CrowdControlAbility) a).getEffect().getType() == EffectType.BUFF))) {
			a.execute(AOF(getAlly2(c), c, a));

		}

		if (a instanceof CrowdControlAbility && ((CrowdControlAbility) a).getEffect().getType() == EffectType.DEBUFF) {
			a.execute(AOF(getEnemy(c), c, a));
		}

		if (a instanceof DamagingAbility) {

			if (a.getCastArea() == AreaOfEffect.SURROUND) {
				a.execute(AOF(getDamageable(c), c, a));
			} else {
				a.execute(AOF(getEnemy(c), c, a));
			}
			for (int i = 0; i < AOF(getDamageable(c), c, a).size(); i++) {
				removeDamageable(AOF(getDamageable(c), c, a).get(i));
			}
		}
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);
		c.setMana(c.getMana() - a.getManaCost());
		a.setCurrentCooldown(a.getBaseCooldown());
	}

	public void castAbility(Ability a, Direction d)
			throws NotEnoughResourcesException, AbilityUseException, CloneNotSupportedException {
		Champion c = getCurrentChampion();

		if (!checkEnough(c, a)) {
			throw new NotEnoughResourcesException();
		}

		c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());

		if (a.getCurrentCooldown() > 0) {
			throw new AbilityUseException();
		}

		if (c.getCondition() == Condition.INACTIVE) {
			throw new AbilityUseException();
		}

		for (Effect e : c.getAppliedEffects()) {
			if (e instanceof Silence) {
				throw new AbilityUseException();
			}
		}

		a.execute(getTargets(d, c, a));

		for (int i = 0; i < getTargets(d, c, a).size(); i++) {
			removeDamageable(getTargets(d, c, a).get(i));
		}
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);
		c.setMana(c.getMana() - a.getManaCost());
		a.setCurrentCooldown(a.getBaseCooldown());
	}

	
	public ArrayList<Damageable> getDamageable(Champion c) {
		ArrayList<Damageable> d = new ArrayList<Damageable>();
		d.addAll(getEnemy(c));
		for (Object[] y : board) {
			for (Object e : y) {
				if (e instanceof Cover) {
					d.add((Damageable) e);
				}
			}
		}

		return d;
	}

	private static int xx = 0;
	private static int yy = 0;

	private static void setTheIcrementValue(Direction d) {
		if (d == Direction.DOWN) {
			xx = -1;
			yy = 0;
		} else if (d == Direction.UP) {
			xx = 1;
			yy = 0;
		} else if (d == Direction.RIGHT) {
			xx = 0;
			yy = 1;

		} else {
			xx = 0;
			yy = -1;
		}
	}

	public ArrayList<Damageable> getTargets(Direction d, Champion c, Ability a) {
		int x = c.getLocation().x;
		int y = c.getLocation().y;
		ArrayList<Damageable> targets = new ArrayList<Damageable>();
		setTheIcrementValue(d);
		x += xx;
		y += yy;

		if ((a instanceof HealingAbility) || (ccType(a) == EffectType.BUFF)) {
			while (x < 5 && x >= 0 && y < 5 && y >= 0) {

				if (board[x][y] instanceof Champion && (member(c, (Champion) board[x][y]))) {
					Champion f = (Champion) board[x][y];
					if (isInRange(a.getCastRange(), c.getLocation(), f.getLocation())) {
						targets.add((Damageable) f);

					}
				}
				x += xx;
				y += yy;

			}
		}
		if (a instanceof DamagingAbility) {

			while (x < 5 && x >= 0 && y < 5 && y >= 0) {

				if ((board[x][y] instanceof Champion && !(member(c, (Champion) board[x][y])))
						|| (board[x][y] instanceof Cover)) {
					Damageable t = (Damageable) board[x][y];
					if (isInRange(a.getCastRange(), c.getLocation(), t.getLocation())) {
						if (t instanceof Champion && checkShield((Champion) t)) {
							break;
						}
						targets.add(t);

					}

				}
				x += xx;
				y += yy;

			}
		}
		if (ccType(a) == EffectType.DEBUFF) {
			while (x < 5 && x >= 0 && y < 5 && y >= 0) {
				if (board[x][y] instanceof Champion && !(member(c, (Champion) board[x][y]))) {
					Champion f = (Champion) board[x][y];
					if (isInRange(a.getCastRange(), c.getLocation(), f.getLocation())) {
						targets.add((Damageable) f);
					}
				}
				x += xx;
				y += yy;
			}
		}
		return targets;
	}

	public void removeFromQ(Comparable o) {
		PriorityQueue temp = new PriorityQueue(turnOrder.size());
		while (turnOrder.size() != 0) {
			if (turnOrder.peekMin() != o) {
				temp.insert(turnOrder.remove());
			}

			else {
				turnOrder.remove();
			}
		}
		turnOrder = temp;
	}

	public void updateChampion() {
		Champion champion = getCurrentChampion();
		champion.setCurrentActionPoints(champion.getMaxActionPointsPerTurn());
		for (int i = 0; i < champion.getAppliedEffects().size(); i++) {
			champion.getAppliedEffects().get(i).setDuration(champion.getAppliedEffects().get(i).getDuration() - 1);
			if (champion.getAppliedEffects().get(i).getDuration() <= 0)
				champion.getAppliedEffects().get(i).remove(champion);
		}
		for (Ability a : champion.getAbilities()) {
			a.setCurrentCooldown(a.getCurrentCooldown() - 1);
		}
	}

	public void endTurn() throws CloneNotSupportedException {
		turnOrder.remove();
		if (turnOrder.isEmpty()) {
			prepareChampionTurns();
		}
		while (((Champion) turnOrder.peekMin()).getCondition() == Condition.INACTIVE) {
			updateChampion();
			turnOrder.remove();
			if (turnOrder.isEmpty())
				prepareChampionTurns();
		}
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);

		updateChampion();

	}

	public void checkKnockedOut(Player player) throws CloneNotSupportedException {
		for (int i = 0; i < player.getTeam().size(); i++) {
			Champion c = player.getTeam().get(i);
			if (c.getCondition() == Condition.KNOCKEDOUT) {

				removeFromQ(c);

				player.getTeam().remove(i);
			}

		}
	}

	public void PushChampions(ArrayList<Champion> champions) {
		for (int i = 0; i < champions.size(); i++) {
			turnOrder.insert(champions.get(i));
		}
	}

	private void prepareChampionTurns() throws CloneNotSupportedException {
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);
		PushChampions(firstPlayer.getTeam());
		PushChampions(secondPlayer.getTeam());

	}

	public void useLeaderAbility()
			throws LeaderAbilityAlreadyUsedException, LeaderNotCurrentException, CloneNotSupportedException {
		if ((firstPlayer.getTeam().contains(this.getCurrentChampion()) && isFirstLeaderAbilityUsed())
				|| (secondPlayer.getTeam().contains(this.getCurrentChampion()) && isSecondLeaderAbilityUsed()))
			throw new LeaderAbilityAlreadyUsedException();
		if (!(firstPlayer.getLeader().equals(this.getCurrentChampion())
				|| secondPlayer.getLeader().equals(this.getCurrentChampion())))
			throw new LeaderNotCurrentException();
		if (this.getCurrentChampion() instanceof Hero) {
			Hero yehia = (Hero) this.getCurrentChampion();
			if ((firstPlayer.getTeam().contains(this.getCurrentChampion())))

			{
				yehia.useLeaderAbility(firstPlayer.getTeam());
				this.firstLeaderAbilityUsed = true;

			} else {

				yehia.useLeaderAbility(secondPlayer.getTeam());
				this.secondLeaderAbilityUsed = true;

			}

		} else if (getCurrentChampion() instanceof Villain) {
			Villain osama = (Villain) getCurrentChampion();
			if (isFirstPlayer(osama)) {

				osama.useLeaderAbility((ArrayList<Champion>) secondPlayer.getTeam().clone());
				firstLeaderAbilityUsed = true;

			} else {

				osama.useLeaderAbility((ArrayList<Champion>) firstPlayer.getTeam().clone());
				secondLeaderAbilityUsed = true;

			}
		} else {
			AntiHero omar = (AntiHero) this.getCurrentChampion();
			ArrayList<Champion> mytargets = new ArrayList<>();
			for (int i = 0; i < Math.max(firstPlayer.getTeam().size(), secondPlayer.getTeam().size()); i++) {
				if (i < firstPlayer.getTeam().size()) {
					if (firstPlayer.getTeam().get(i) != firstPlayer.getLeader()) {
						mytargets.add(firstPlayer.getTeam().get(i));
					}
				}
				if (i < secondPlayer.getTeam().size()) {
					if (secondPlayer.getTeam().get(i) != secondPlayer.getLeader()) {
						mytargets.add(secondPlayer.getTeam().get(i));
					}
				}
			}

			if (isFirstPlayer(omar))
				this.firstLeaderAbilityUsed = true;
			else
				this.secondLeaderAbilityUsed = true;
			omar.useLeaderAbility(mytargets);

		}
		checkKnockedOut(firstPlayer);
		checkKnockedOut(secondPlayer);

	}
}
