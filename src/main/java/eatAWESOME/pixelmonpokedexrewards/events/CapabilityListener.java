package eatAWESOME.pixelmonpokedexrewards.events;

import eatAWESOME.pixelmonpokedexrewards.PixelmonPokedexRewards;
import eatAWESOME.pixelmonpokedexrewards.capabilities.PokedexDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PixelmonPokedexRewards.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityListener {

	public static final ResourceLocation pokedexLocation = new ResourceLocation(PixelmonPokedexRewards.MOD_ID, "pokedexData");

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(pokedexLocation, new PokedexDataProvider());
        }
    }
}