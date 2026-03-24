package com.marvelwargame.engine.factory;

import com.marvelwargame.model.abilities.*;
import com.marvelwargame.model.effects.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory that parses abilities from CSV and constructs the right subclass.
 * CSV format: TYPE,Name,ManaCost,BaseCooldown,CastRange,AreaOfEffect,ActionPoints,[extra]
 */
public final class AbilityFactory {

    private AbilityFactory() {}

    public static List<Ability> loadFromCsv(InputStream in) throws IOException {
        List<Ability> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] col = line.split(",");
                String type = col[0].trim();
                String name = col[1].trim();
                int mana      = Integer.parseInt(col[2].trim());
                int cooldown  = Integer.parseInt(col[3].trim());
                int range     = Integer.parseInt(col[4].trim());
                AreaOfEffect aoe = AreaOfEffect.valueOf(col[5].trim());
                int ap        = Integer.parseInt(col[6].trim());

                switch (type) {
                    case "DMG" -> list.add(new DamagingAbility(name, mana, cooldown, range, aoe, ap,
                            Integer.parseInt(col[7].trim())));
                    case "HEL" -> list.add(new HealingAbility(name, mana, cooldown, range, aoe, ap,
                            Integer.parseInt(col[7].trim())));
                    case "CC"  -> list.add(new CrowdControlAbility(name, mana, cooldown, range, aoe, ap,
                            buildEffect(col[7].trim(), Integer.parseInt(col[8].trim()))));
                }
            }
        }
        return list;
    }

    private static Effect buildEffect(String effectName, int duration) {
        return switch (effectName) {
            case "Disarm"  -> new Disarm(duration);
            case "Dodge"   -> new Dodge(duration);
            case "Embrace" -> new Embrace(duration);
            case "PowerUp" -> new PowerUp(duration);
            case "Root"    -> new Root(duration);
            case "Shield"  -> new Shield(duration);
            case "Shock"   -> new Shock(duration);
            case "Silence" -> new Silence(duration);
            case "SpeedUp" -> new SpeedUp(duration);
            case "Stun"    -> new Stun(duration);
            default -> throw new IllegalArgumentException("Unknown effect: " + effectName);
        };
    }
}
