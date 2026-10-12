package eatAWESOME.pixelmonpokedexrewards.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PokedexDataAttachment implements IPokedexDataAttachment, INBTSerializable<CompoundTag> {
	private static final String POKEDEX_REWARDS_LEVEL = "pokedexRewardsLevel";
    private static final String CLAIMED_POKEDEX_REWARDS = "claimedPokedexRewards";

    private int pokedexRewardsLevel = 0;
    private List<Integer> claimedPokedexRewards = new ArrayList<Integer>();

    @Override
    public int getPokedexRewardsLevel() {
        return this.pokedexRewardsLevel;
    }

    @Override
    public void setPokedexRewardsLevel(int level) {
        this.pokedexRewardsLevel = level;
    }

    @Override
    public List<Integer> getClaimedPokedexRewards() {
        return this.claimedPokedexRewards;
    }

    @Override
    public void setClaimedPokedexRewards(List<Integer> rewards) {
        this.claimedPokedexRewards = rewards == null ? new ArrayList<Integer>() : rewards;
    }
    

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putInt(POKEDEX_REWARDS_LEVEL, pokedexRewardsLevel);

        int[] claimedRewards = claimedPokedexRewards.stream()
                .mapToInt(Integer::intValue)
                .toArray();

        tag.putIntArray(CLAIMED_POKEDEX_REWARDS, claimedRewards);

        return tag;
    }

    @Override
    public void deserializeNBT(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {
        pokedexRewardsLevel = tag.getInt(POKEDEX_REWARDS_LEVEL);

        int[] claimedRewards = tag.getIntArray(CLAIMED_POKEDEX_REWARDS);
        claimedPokedexRewards = new ArrayList<>(claimedRewards.length);

        for (int reward : claimedRewards) {
            claimedPokedexRewards.add(reward);
        }
    }
}