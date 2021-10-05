package info.tehnut.soulshards.fabric;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Set;

public class ConfigSoulShardsConfigEntityListImpl {

    private static final Set<String> DEFAULT_DISABLES = Sets.newHashSet(
            "minecraft:armor_stand",
            "minecraft:elder_guardian",
            "minecraft:ender_dragon",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:player"
    );

    public static Map<String, Boolean> getDefaults() {
        Map<String, Boolean> defaults = Maps.newHashMap();

        DefaultedRegistry.ENTITY_TYPE.stream()
                .filter(e -> e.getSpawnGroup() != SpawnGroup.MISC)
                .forEach(e -> {
                    String entityId = Registry.ENTITY_TYPE.getId(e).toString();
                    defaults.put(entityId, !DEFAULT_DISABLES.contains(entityId));
                });

        return defaults;
    }

}
