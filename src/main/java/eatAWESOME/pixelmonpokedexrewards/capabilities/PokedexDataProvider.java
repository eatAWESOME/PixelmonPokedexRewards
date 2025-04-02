package eatAWESOME.pixelmonpokedexrewards.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PokedexDataProvider implements ICapabilitySerializable<INBT> {

    @CapabilityInject(IPokedexData.class)
    public static final Capability<IPokedexData> pokedexLocation = null;

    private LazyOptional<IPokedexData> instance = LazyOptional.of(pokedexLocation::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == pokedexLocation ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return pokedexLocation.getStorage().writeNBT(pokedexLocation, this.instance.orElse(null), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        pokedexLocation.getStorage().readNBT(pokedexLocation, this.instance.orElse(null), null, nbt);
    }
}