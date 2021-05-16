package info.tehnut.soulshards.core.mixin;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class MixinAnvilContainer {

    @Shadow
    @Final
    private Inventory inventory;
    @Shadow
    @Final
    private Property levelCost;
    @Shadow
    @Final
    private CraftingResultInventory result;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void soulshards$updateResult(CallbackInfo callbackInfo) {
        if (!SoulShards.CONFIG.getBalance().allowShardCombination())
            return;
        
        ItemStack leftStack = inventory.getStack(0);
        ItemStack rightStack = inventory.getStack(1);

        if (leftStack.getItem() instanceof ItemSoulShard && rightStack.getItem() instanceof ItemSoulShard) {
            Binding left = ((ItemSoulShard) leftStack.getItem()).getBinding(leftStack);
            Binding right = ((ItemSoulShard) rightStack.getItem()).getBinding(rightStack);

            if (left == null || right == null)
                return;

            if (left.getBoundEntity() != null && left.getBoundEntity().equals(right.getBoundEntity())) {
                ItemStack output = new ItemStack(RegistrarSoulShards.SOUL_SHARD);
                ((ItemSoulShard) output.getItem()).updateBinding(output, left.addKills(right.getKills()));
                result.setStack(0, output);
                levelCost.set(left.getTier().getIndex() * 6);
                callbackInfo.cancel();
            }
        }
    }
}
