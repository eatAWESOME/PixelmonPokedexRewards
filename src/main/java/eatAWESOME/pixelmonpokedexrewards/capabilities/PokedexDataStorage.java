package eatAWESOME.pixelmonpokedexrewards.capabilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import java.util.List;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class PokedexDataStorage implements Capability.IStorage<IPokedexData> {

    private static final String pokedexRewardsLevelNBT = "pokedexRewardsLevel";
    private static final String claimedPokedexRewardsNBT = "claimedPokedexRewards";

    private static final Gson GSON = new Gson();

    @Nullable
    @Override
    public INBT writeNBT(Capability<IPokedexData> capability, IPokedexData instance, Direction side) {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(pokedexRewardsLevelNBT, instance.getPokedexRewardsLevel());
        nbt.putString(claimedPokedexRewardsNBT, GSON.toJson(instance.getClaimedPokedexRewards()));
        return nbt;
    }

    @Override
    public void readNBT(Capability<IPokedexData> capability, IPokedexData instance, Direction side, INBT nbt) {
        if(nbt instanceof CompoundNBT) {
            CompoundNBT compoundNBT = (CompoundNBT) nbt;
            instance.setPokedexRewardsLevel(compoundNBT.getInt(pokedexRewardsLevelNBT));
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            List<Integer> claimedPokedexRewards = GSON.fromJson(compoundNBT.getString(claimedPokedexRewardsNBT), listType);
            instance.setClaimedPokedexRewards(claimedPokedexRewards);
        }
    }
}
