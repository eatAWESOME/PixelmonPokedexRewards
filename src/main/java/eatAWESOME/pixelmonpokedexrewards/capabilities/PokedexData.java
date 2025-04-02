package eatAWESOME.pixelmonpokedexrewards.capabilities;

import java.util.ArrayList;
import java.util.List;

public class PokedexData implements IPokedexData {
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
}