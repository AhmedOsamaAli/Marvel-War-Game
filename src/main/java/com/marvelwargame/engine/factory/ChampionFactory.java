package com.marvelwargame.engine.factory;

import com.marvelwargame.model.abilities.Ability;
import com.marvelwargame.model.world.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory that parses champions from CSV and associates their abilities.
 * CSV format: TYPE,Name,MaxHP,Mana,MaxAP,Speed,AttackRange,AttackDamage,Ability1,Ability2,Ability3
 */
public final class ChampionFactory {

    private ChampionFactory() {}

    public static List<Champion> loadFromCsv(InputStream in, List<Ability> availableAbilities)
            throws IOException {
        List<Champion> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] col = line.split(",");
                String type    = col[0].trim();
                String name    = col[1].trim();
                int maxHP      = Integer.parseInt(col[2].trim());
                int mana       = Integer.parseInt(col[3].trim());
                int maxAP      = Integer.parseInt(col[4].trim());
                int speed      = Integer.parseInt(col[5].trim());
                int atkRange   = Integer.parseInt(col[6].trim());
                int atkDamage  = Integer.parseInt(col[7].trim());

                Champion champion = switch (type) {
                    case "H" -> new Hero(name, maxHP, mana, maxAP, speed, atkRange, atkDamage);
                    case "V" -> new Villain(name, maxHP, mana, maxAP, speed, atkRange, atkDamage);
                    case "A" -> new AntiHero(name, maxHP, mana, maxAP, speed, atkRange, atkDamage);
                    default  -> throw new IllegalArgumentException("Unknown champion type: " + type);
                };

                for (int i = 8; i <= 10 && i < col.length; i++) {
                    String abilityName = col[i].trim();
                    Ability a = findAbility(abilityName, availableAbilities);
                    if (a != null) champion.getAbilities().add(a);
                }

                list.add(champion);
            }
        }
        return list;
    }

    private static Ability findAbility(String name, List<Ability> abilities) {
        return abilities.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
