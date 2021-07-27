package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.ISoulWeapon;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemVileSword extends SwordItem implements ISoulWeapon {

    public static final Tier MATERIAL_VILE = new MaterialVile();

    public ItemVileSword() {
        super(MATERIAL_VILE, 3, -2.4F, new Properties().tab(SoulShards.TAB_SS));
    }

    @Override
    public int getSoulBonus(ItemStack stack, Player player, LivingEntity killedEntity) {
        return 1;
    }

    public static class MaterialVile implements Tier {

        private final LazyLoadedValue<Ingredient> ingredient;

        public MaterialVile() {
            this.ingredient = new LazyLoadedValue<>(() -> Ingredient.of(RegistrarSoulShards.CORRUPTED_INGOT));
        }

        @Override
        public int getUses() {
            return Tiers.IRON.getUses();
        }

        @Override
        public float getSpeed() {
            return Tiers.IRON.getSpeed();
        }

        @Override
        public float getAttackDamageBonus() {
            return Tiers.IRON.getAttackDamageBonus();
        }

        @Override
        public int getLevel() {
            return Tiers.IRON.getLevel();
        }

        @Override
        public int getEnchantmentValue() {
            return Tiers.IRON.getEnchantmentValue();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return ingredient.get();
        }

    }
}
