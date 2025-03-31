package eatAWESOME.pixelmonpokedexrewards;

import eatAWESOME.pixelmonpokedexrewards.events.PokedexListener;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod("pixelmonpokedexrewards")
public class PixelmonPokedexRewards {

    public PixelmonPokedexRewards() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        new PokedexListener();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        System.out.println("PixelmonPokedexRewards is loading!");
    }
}
