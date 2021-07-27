package info.tehnut.soulshardsrespawn.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ISoulWeapon {

    int getSoulBonus(ItemStack stack, Player player, LivingEntity killedEntity);
}
