package info.tehnut.soulshards.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;

@Mixin(AnvilScreenHandler.class)
public class MixinAnvilScreenHandler extends MixinForgingScreenHandler {

    @Shadow
    @Final
    private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void soulshards$updateResult(CallbackInfo callbackInfo) {
        if (!SoulShards.CONFIG.getBalance().allowShardCombination())
            return;

        ItemStack leftStack = this.input.getStack(0);
        ItemStack rightStack = this.input.getStack(1);

        if (leftStack.getItem() instanceof ItemSoulShard && rightStack.getItem() instanceof ItemSoulShard) {
            Binding left = ((ItemSoulShard) leftStack.getItem()).getBinding(leftStack);
            Binding right = ((ItemSoulShard) rightStack.getItem()).getBinding(rightStack);

            if (left == null || right == null)
                return;

            if (left.getBoundEntity() != null && left.getBoundEntity().equals(right.getBoundEntity())) {
                ItemStack output = new ItemStack(RegistrarSoulShards.SOUL_SHARD.get());
                ((ItemSoulShard) output.getItem()).updateBinding(output, left.addKills(right.getKills()));
                this.output.setStack(0, output);
                levelCost.set(left.getTier().getIndex() * 6);
                callbackInfo.cancel();
            }
        }
    }
}
