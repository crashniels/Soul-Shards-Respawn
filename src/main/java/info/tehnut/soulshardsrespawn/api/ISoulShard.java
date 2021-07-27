package info.tehnut.soulshardsrespawn.api;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ISoulShard {

    @Nullable
    IBinding getBinding(ItemStack stack);
}
