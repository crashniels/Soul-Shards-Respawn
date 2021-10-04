package info.tehnut.soulshards;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import info.tehnut.soulshards.api.BindingEvent;
import info.tehnut.soulshards.api.ISoulWeapon;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.MultiblockPattern;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.CageBornTagHandler;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.Set;

public class EventHandler implements InteractionEvent.RightClickBlock {

    public static void init() {

    }

    @Override
    public EventResult click(PlayerEntity playerEntity, Hand hand, BlockPos blockPos, Direction direction) {
        MultiblockPattern pattern = ConfigSoulShards.getMultiblock();

        ItemStack held = playerEntity.getStackInHand(hand);
        if (!ItemStack.areItemsEqual(pattern.getCatalyst(), held))
            return EventResult.pass();

        BlockState worldState = playerEntity.getBlockStateAtPos();
        if (!pattern.isOriginBlock(worldState))
            return EventResult.pass();

        TypedActionResult<Set<BlockPos>> match = pattern.match(playerEntity.getEntityWorld(), blockPos);
        if (match.getResult() == ActionResult.FAIL)
            return EventResult.pass();

        match.getValue().forEach(matchedPos -> playerEntity.getEntityWorld().breakBlock(matchedPos, false));
        held.decrement(1);
        ItemStack shardStack = new ItemStack(RegistrarSoulShards.SOUL_SHARD.get());
        if (!playerEntity.getInventory().insertStack(shardStack))
            ItemScatterer.spawn(playerEntity.getEntityWorld(), playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), shardStack);
        return EventResult.pass();
    }


    public static void onEntityDeath(LivingEntity killed, DamageSource source) {
        // Using canUsePortals because it appears to be MCP's isNonBoss().
        // Only returns false for Wither and Ender Dragon
        if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !killed.canUsePortals())
            return;

        if (!SoulShards.CONFIG.getBalance().countCageBornForShard() && 
        //killed.getDataTracker().get(MixinEntityLiving.cageBornTag))
        CageBornTagHandler.getCageBornTag(killed))
            return;

        if (source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getAttacker();
            Identifier entityId = getEntityId(killed);

            if (!SoulShards.CONFIG.getEntityList().isEnabled(entityId))
                return;

            ItemStack shardStack = getFirstShard(player, entityId);
            if (shardStack.isEmpty())
                return;

            ItemSoulShard shard = (ItemSoulShard) shardStack.getItem();
            Binding binding = shard.getBinding(shardStack);
            if (binding == null)
                binding = getNewBinding(killed);

            if (binding == null)
                return;

            ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
            int soulsGained = 1 + EnchantmentHelper.getLevel(RegistrarSoulShards.SOUL_STEALER.get(), mainHand);
            if (mainHand.getItem() instanceof ISoulWeapon)
                soulsGained += ((ISoulWeapon) mainHand.getItem()).getSoulBonus(mainHand, player, killed);

            soulsGained = BindingEvent.GAIN_SOULS.invoker().getGainedSouls(killed, binding, soulsGained);

            if (binding.getBoundEntity() == null)
                binding.setBoundEntity(entityId);

            if (binding.getOwner() == null)
                binding.setOwner(player.getGameProfile().getId());

            shard.updateBinding(shardStack, binding.addKills(soulsGained));
        }
    }

    private static ItemStack getFirstShard(PlayerEntity player, Identifier entityId) {
        // Checks the offhand first
        ItemStack shardItem = player.getStackInHand(Hand.OFF_HAND);
        // If offhand isn't a shard, loop through the hotbar
        if (shardItem.isEmpty() || !(shardItem.getItem() instanceof ItemSoulShard)) {
            for (int i = 0; i < 9; i++) {
                shardItem = player.getInventory().getStack(i);
                if (!shardItem.isEmpty() && shardItem.getItem() instanceof ItemSoulShard) {
                    if (checkBinding(entityId, shardItem)) return shardItem;
                }
            }
        } else { // If offhand is a shard, check it it
            if (checkBinding(entityId, shardItem))
                return shardItem;
        }

        return ItemStack.EMPTY; // No shard found
    }

    private static boolean checkBinding(Identifier entityId, ItemStack shardItem) {
        Binding binding = ((ItemSoulShard) shardItem.getItem()).getBinding(shardItem);

        // If there's no binding or no bound entity, this is a valid shard
        if (binding == null || binding.getBoundEntity() == null)
            return true;

        // If there is a bound entity and we're less than the max kills, this is a valid shard
        return binding.getBoundEntity().equals(entityId) && binding.getKills() < Tier.maxKills;

    }

    private static Identifier getEntityId(LivingEntity entity) {
        Identifier id = Registry.ENTITY_TYPE.getId(entity.getType());
        return BindingEvent.GET_ENTITY_ID.invoker().getEntityName(entity, id);
    }

    private static Binding getNewBinding(LivingEntity entity) {
        Binding binding = new Binding(null, 0);
        return (Binding) BindingEvent.NEW_BINDINGS.invoker().onNewBinding(entity, binding).getValue();
    }

}
