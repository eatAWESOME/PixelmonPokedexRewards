package eatAWESOME.pixelmonpokedexrewards.data;

import java.util.List;

public interface IPokedexDataAttachment {
    int getPokedexRewardsLevel();
    void setPokedexRewardsLevel(int level);
    List<Integer> getClaimedPokedexRewards();
    void setClaimedPokedexRewards(List<Integer> rewards);
}
