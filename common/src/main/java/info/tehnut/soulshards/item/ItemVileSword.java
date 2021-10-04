package info.tehnut.soulshards.item;

import info.tehnut.soulshards.api.ISoulWeapon;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Lazy;

public class ItemVileSword extends SwordItem implements ISoulWeapon {

    public static final ToolMaterial MATERIAL_VILE = new MaterialVile();

    public ItemVileSword() {
        super(MATERIAL_VILE, 3, -2.4F, new Settings().group(RegistrarSoulShards.SoulShardsIG));
    }

    @Override
    public int getSoulBonus(ItemStack stack, PlayerEntity player, LivingEntity killedEntity) {
        return 1;
    }

    public static class MaterialVile implements ToolMaterial {

        private final Lazy<Ingredient> ingredient;

        public MaterialVile() {
            this.ingredient = new Lazy<>(() -> Ingredient.ofItems(RegistrarSoulShards.CORRUPTED_INGOT.get()));
        }

        @Override
        public int getDurability() {
            return ToolMaterials.IRON.getDurability();
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return ToolMaterials.IRON.getMiningSpeedMultiplier();
        }

        @Override
        public float getAttackDamage() {
            return ToolMaterials.IRON.getAttackDamage();
        }

        @Override
        public int getMiningLevel() {
            return ToolMaterials.IRON.getMiningLevel();
        }

        @Override
        public int getEnchantability() {
            return ToolMaterials.IRON.getEnchantability();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return ingredient.get();
        }

    }
}
