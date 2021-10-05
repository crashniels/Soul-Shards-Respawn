package info.tehnut.soulshards.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;

@Mixin(ForgingScreenHandler.class)
public class MixinForgingScreenHandler {

    @Shadow
    @Final
    protected CraftingResultInventory output;

    @Shadow
    @Final
    protected Inventory input;

}
