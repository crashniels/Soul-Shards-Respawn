package info.tehnut.soulshards;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.architectury.injectables.annotations.ExpectPlatform;
import info.tehnut.soulshards.core.data.MultiblockPattern;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Set;

public class ConfigSoulShards {

    private static MultiblockPattern multiblock;

    private ConfigBalance balance;
    private ConfigClient client;
    private ConfigEntityList entityList;

    private ConfigSoulShards(ConfigBalance balance, ConfigClient client, ConfigEntityList entityList) {
        this.balance = balance;
        this.client = client;
        this.entityList = entityList;
    }

    public ConfigSoulShards() {
        this(new ConfigBalance(), new ConfigClient(), new ConfigEntityList());
    }

    public ConfigBalance getBalance() {
        return balance;
    }

    public ConfigClient getClient() {
        return client;
    }

    public ConfigEntityList getEntityList() {
        return entityList;
    }

    public static void handleMultiblock() {
        // FIXME parsing is currently broke
//        File multiblockFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), SoulShards.MODID + "/multiblock.json");
//        if (!multiblockFile.exists()) {
//            try {
//                FileUtils.copyInputStreamToFile(ConfigSoulShards.class.getResourceAsStream("/data/multiblock.json"), multiblockFile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        multiblock = JsonUtil.fromJson(TypeToken.get(MultiblockPattern.class), multiblockFile);
        if (multiblock == null)
            multiblock = MultiblockPattern.DEFAULT;
    }

    public static MultiblockPattern getMultiblock() {
        if (multiblock == null)
            handleMultiblock();

        return multiblock;
    }

    public static class ConfigBalance {

        private boolean allowSpawnerAbsorption;
        private boolean allowFakePlayers;
        private int absorptionBonus;
        private boolean allowBossSpawns;
        private boolean countCageBornForShard;
        private boolean requireOwnerOnline;
        private boolean requireRedstoneSignal;
        private boolean allowShardCombination;
        private int spawnCap;
        private boolean dropExperience;

        public ConfigBalance(boolean allowSpawnerAbsorption, boolean allowFakePlayers, int absorptionBonus, boolean allowBossSpawns,
                             boolean countCageBornForShard, boolean requireOwnerOnline, boolean requireRedstoneSignal, boolean allowShardCombination, int spawnCap,
                             boolean dropExperience) {
            this.allowSpawnerAbsorption = allowSpawnerAbsorption;
            this.allowFakePlayers = allowFakePlayers;
            this.absorptionBonus = absorptionBonus;
            this.allowBossSpawns = allowBossSpawns;
            this.countCageBornForShard = countCageBornForShard;
            this.requireOwnerOnline = requireOwnerOnline;
            this.requireRedstoneSignal = requireRedstoneSignal;
            this.allowShardCombination = allowShardCombination;
            this.spawnCap = spawnCap;
            this.dropExperience = dropExperience;
        }

        public ConfigBalance() {
            this(true, false, 200, false, false, false, false, true, 32, false);
        }

        public boolean allowSpawnerAbsorption() {
            return allowSpawnerAbsorption;
        }

        public boolean allowFakePlayers() {
            return allowFakePlayers;
        }

        public int getAbsorptionBonus() {
            return absorptionBonus;
        }

        public boolean allowBossSpawns() {
            return allowBossSpawns;
        }

        public boolean countCageBornForShard() {
            return countCageBornForShard;
        }

        public boolean requireOwnerOnline() {
            return requireOwnerOnline;
        }

        public boolean requireRedstoneSignal() {
            return requireRedstoneSignal;
        }

        public boolean allowShardCombination() {
            return allowShardCombination;
        }

        public int getSpawnCap() {
            return spawnCap;
        }

        public boolean shouldDropExperience() {
            return dropExperience;
        }

    }

    public static class ConfigClient {
        private boolean displayDurabilityBar;

        public ConfigClient(boolean displayDurabilityBar) {
            this.displayDurabilityBar = displayDurabilityBar;
        }

        public ConfigClient() {
            this(true);
        }

        public boolean displayDurabilityBar() {
            return displayDurabilityBar;
        }
    }

    public static class ConfigEntityList {
        private Map<String, Boolean> entities;

        public ConfigEntityList(Map<String, Boolean> entities) {
            this.entities = entities;
        }

        public ConfigEntityList() {
            this(getDefaults());
        }

        public boolean isEnabled(Identifier entityId) {
            return entities.getOrDefault(entityId.toString(), false);
        }

        @ExpectPlatform
        private static Map<String, Boolean> getDefaults() {
            throw new AssertionError();
        }
    }
}
