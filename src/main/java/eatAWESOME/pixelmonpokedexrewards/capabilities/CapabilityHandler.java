package eatAWESOME.pixelmonpokedexrewards.capabilities;

import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityHandler {
    public static void register() {
        CapabilityManager.INSTANCE.register(IPokedexData.class, new PokedexDataStorage(), PokedexData::new);
    }
}
