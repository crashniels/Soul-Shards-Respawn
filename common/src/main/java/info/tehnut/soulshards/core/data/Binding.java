package info.tehnut.soulshards.core.data;

import info.tehnut.soulshards.api.IBinding;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.core.util.INBTSerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class Binding implements IBinding, INBTSerializable<NbtElement> {

    private Identifier boundEntity;
    private UUID owner;
    private int kills;

    public Binding(Identifier boundEntity, UUID owner, int kills) {
        this.boundEntity = boundEntity;
        this.owner = owner;
        this.kills = kills;
    }

    public Binding(Identifier boundEntity, int kills) {
        this(boundEntity, null, kills);
    }

    public Binding(NbtCompound bindingTag) {
        deserializeNBT(bindingTag);
    }

    @Override
    public Identifier getBoundEntity() {
        return boundEntity;
    }

    public Binding setBoundEntity(Identifier boundEntity) {
        this.boundEntity = boundEntity;
        return this;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public Binding setOwner(UUID owner) {
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
    public NbtCompound serializeNBT() {
        NbtCompound tag = new NbtCompound();

        if (boundEntity != null)
            tag.putString("bound", boundEntity.toString());
        if (owner != null)
            tag.putString("owner", owner.toString());
        tag.putInt("kills", kills);
        return tag;
    }

    @Override
    public void deserializeNBT(NbtElement nbt) {
        if (((NbtCompound) nbt).contains("bound"))
            this.boundEntity = new Identifier(((NbtCompound) nbt).getString("bound"));
        if (((NbtCompound) nbt).contains("owner"))
            this.owner = UUID.fromString(((NbtCompound) nbt).getString("owner"));
        this.kills = ((NbtCompound) nbt).getInt("kills");
    }

    public static Binding fromNBT(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("binding"))
            return null;

        return new Binding(tag.getCompound("binding"));
    }
}
