package info.tehnut.soulshardsrespawn.core.data;

import info.tehnut.soulshardsrespawn.api.IBinding;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class Binding implements IBinding, INBTSerializable<CompoundTag> {

    @Nullable
    private ResourceLocation boundEntity;
    @Nullable
    private UUID owner;
    private int kills;

    public Binding(ResourceLocation boundEntity, UUID owner, int kills) {
        this.boundEntity = boundEntity;
        this.owner = owner;
        this.kills = kills;
    }

    public Binding(ResourceLocation boundEntity, int kills) {
        this(boundEntity, null, kills);
    }

    public Binding(CompoundTag bindingTag) {
        deserializeNBT(bindingTag);
    }

    @Nullable
    @Override
    public ResourceLocation getBoundEntity() {
        return boundEntity;
    }

    public Binding setBoundEntity(@Nullable ResourceLocation boundEntity) {
        this.boundEntity = boundEntity;
        return this;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    public Binding setOwner(@Nullable UUID owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public int getKills() {
        return kills;
    }

    public Binding setKills(int kills) {
        this.kills = Math.min(Tier.maxKills, kills);
        return this;
    }

    @Override
    public Binding addKills(int kills) {
        this.kills = Math.min(Tier.maxKills, this.kills + kills);
        return this;
    }

    @Override
    public IShardTier getTier() {
        return Tier.TIERS.floorEntry(kills).getValue();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        if (boundEntity != null)
            tag.putString("bound", boundEntity.toString());
        if (owner != null)
            tag.putUUID("owner", owner);
        tag.putInt("kills", kills);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("bound"))
            this.boundEntity = new ResourceLocation(nbt.getString("bound"));
        if (nbt.hasUUID("owner"))
            this.owner = nbt.getUUID("owner");
        this.kills = nbt.getInt("kills");
    }

    @Nullable
    public static Binding fromNBT(ItemStack stack) {
        if (!stack.hasTag())
            return null;

        CompoundTag tag = stack.getTag();
        if (!tag.contains("binding"))
            return null;

        return new Binding(tag.getCompound("binding"));
    }
}
