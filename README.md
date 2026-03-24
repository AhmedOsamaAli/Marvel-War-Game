# Marvel War Game FX

A turn-based tactical battle game featuring 15 Marvel champions built with **Java 17 + JavaFX 21**.

---

## Screenshots

> Select champions, view their full profile, then battle on a 5×5 grid.

---

## Features

- **15 Marvel Champions** across 3 factions — Heroes, Villains, Anti-Heroes
- **5×5 tactical grid** with cover obstacles and range-based attacks
- **Full champion profiles** — hover any card to see stats, all abilities explained, and leader ability
- **3 unique ability types** — Damage, Heal, Crowd Control (stun, root, silence, disarm, shock, etc.)
- **Leader abilities** — one powerful team-wide ability per player (once per battle)
- **Per-player color coding** — your champions are blue, opponent's are red
- **Real champion portraits** for all 15 characters
- **Sound effects** via OGG audio (click, select, attack, heal, combat hits, etc.)
- **Event log** — every action narrated in the battle log panel
- **Effect system** — 10 effects with visual badges, tooltips, and duration counters

---

## Requirements

| Tool | Version |
|------|---------|
| Java JDK | 17 or higher |
| Gradle | Bundled (via `gradlew.bat`) |
| OS | Windows / Linux / macOS |

---

## How to Run

**Windows — double-click:**
```
RUN_GAME.bat
```

**Or from terminal:**
```bash
.\gradlew.bat run          # Windows
./gradlew run              # Linux / macOS
```

---

## Champions Roster

| # | Champion | Type | HP | Speed | Attack |
|---|---|---|---|---|---|
| 1 | Captain America | Hero | 1500 | 80 | 100 |
| 2 | Deadpool | Anti-Hero | 1350 | 80 | 90 |
| 3 | Dr. Strange | Hero | 1100 | 60 | 60 |
| 4 | Electro | Villain | 1200 | 75 | 110 |
| 5 | Ghost Rider | Anti-Hero | 1800 | 85 | 140 |
| 6 | Hela | Villain | 1500 | 75 | 150 |
| 7 | Hulk | Hero | 2250 | 55 | 200 |
| 8 | Iceman | Hero | 1000 | 65 | 120 |
| 9 | Iron Man | Hero | 1200 | 85 | 90 |
| 10 | Loki | Villain | 1150 | 70 | 150 |
| 11 | Quicksilver | Villain | 1200 | 99 | 70 |
| 12 | Spider-Man | Hero | 1400 | 85 | 120 |
| 13 | Thor | Hero | 1800 | 90 | 130 |
| 14 | Venom | Anti-Hero | 1650 | 70 | 140 |
| 15 | Yellow Jacket | Villain | 1050 | 60 | 80 |

### Leader Abilities

| Type | Effect |
|---|---|
| **Hero** | Cleanses all debuffs from allies + grants Embrace (damage reduction) for 2 turns |
| **Villain** | Instantly executes any enemy at ≤30% HP |
| **Anti-Hero** | Grants Dodge (evade all attacks) to the entire team for 3 turns |

---

## Project Structure

```
src/main/java/com/marvelwargame/
├── MarvelWarApp.java              ← JavaFX entry point & scene manager
├── engine/
│   ├── Game.java                  ← All game rules (move, attack, abilities, turns)
│   ├── Player.java                ← Player with team + leader
│   ├── PriorityQueue.java         ← Speed-ordered turn queue
│   ├── events/                    ← EventBus (Observer pattern)
│   └── factory/                   ← CSV parsers for champions & abilities
├── model/
│   ├── abilities/                 ← Ability hierarchy (Damage / Heal / CC)
│   ├── effects/                   ← 10 status effects (Shield, Stun, Root, etc.)
│   └── world/                     ← Champion, Cover, Board entities
├── exceptions/                    ← Custom game exceptions
└── ui/
    ├── controllers/               ← FXML controllers (MainMenu, PlayerSetup, ChampionSelect, GameBoard)
    ├── components/                ← ChampionCardView reusable component
    └── util/                      ← AssetManager (images), SoundManager (OGG audio)

src/main/resources/
├── data/
│   ├── Champions.csv              ← Champion stats & ability assignments
│   └── Abilities.csv              ← All ability definitions
├── fxml/                          ← UI layouts
├── css/marvel-theme.css           ← Full JavaFX dark theme
├── images/
│   ├── champions/                 ← Portrait images (JPG/PNG)
│   └── icons/                     ← Kenney game icons (105 PNGs)
└── audio/
    ├── ui/                        ← UI interaction sounds (OGG)
    └── impact/                    ← Combat impact sounds (OGG)
```

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Factory Method** | `ChampionFactory`, `AbilityFactory` — parse CSV into typed objects |
| **Observer / Event Bus** | `EventBus` + `GameEvent` — decoupled game events to UI |
| **Strategy** | Each `Effect` subclass encapsulates its own apply/remove logic |
| **Singleton** | `AssetManager`, `SoundManager` — shared resource managers |
| **MVC** | FXML views + controllers separated from engine model |

---

## Game Flow

1. **Main Menu** → enter player names
2. **Champion Select** → each player picks 3 champions + designates a leader (hover cards to see full profiles)
3. **Battle Board** → turn-based play on 5×5 grid; use abilities, attack, move, or end turn
4. Game ends when all of one player's champions are knocked out

---

## Audio Credits

Sound effects from **Kenney.nl** (CC0 Public Domain):
- [Interface Sounds](https://kenney.nl/assets/interface-sounds)
- [Impact Sounds](https://kenney.nl/assets/impact-sounds)

Icon assets from **Kenney.nl** (CC0 Public Domain):
- [Game Icons](https://kenney.nl/assets/game-icons)
- [UI Pack RPG Expansion](https://kenney.nl/assets/ui-pack-rpg-expansion)

│   ├── effects/                   ← 10 Effects (Buff/Debuff) – Strategy pattern
│   └── world/                     ← Champion/Hero/Villain/AntiHero/Cover/Damageable
├── exceptions/                    ← Typed game exceptions
└── ui/
    ├── controllers/               ← MainMenuController, PlayerSetupController,
    │                                 ChampionSelectController, GameBoardController
    ├── components/
    │   └── ChampionCardView.java  ← Custom reusable champion card widget
    └── util/
        └── AssetManager.java      ← Loads portraits (local → online → placeholder)
```

## Design Patterns Applied

| Pattern | Where |
|---------|-------|
| **MVC** | FXML views + controllers + model separation |
| **Observer (EventBus)** | Game publishes events; UI controllers subscribe |
| **Strategy** | Each `Effect` and `Ability` subclass has its own logic |
| **Factory** | `AbilityFactory` + `ChampionFactory` build objects from CSV |
| **Template Method** | `Game.castAbility()` overloads for different AoE types |

## Game Rules

- **15 Champions**: Heroes, Villains, AntiHeroes
- **5×5 tactical grid** with destructible covers
- **Turn order** by Speed (fastest first)
- **Each turn**: Move (1 AP), Attack (2 AP), Cast Ability (variable), Leader Ability (once per game)
- **Type advantage**: Hero ↔ Villain deals 1.5× damage
- **Leader abilities**:
  - Hero leader → Embraces entire team (clears debuffs + heals + boosts)
  - Villain leader → Executes any enemy below 30% HP
  - AntiHero leader → Grants Dodge to entire team

## Building a Fat JAR

```
.\gradlew.bat jar
```
Output: `build/libs/marvel-war-game-fx-2.0.0.jar`
