package eatAWESOME.pixelmonpokedexrewards.capabilities;

import java.util.List;

public interface IPokedexData {
    int getPokedexRewardsLevel();
    void setPokedexRewardsLevel(int level);
    List<Integer> getClaimedPokedexRewards();
    void setClaimedPokedexRewards(List<Integer> rewards);
}
