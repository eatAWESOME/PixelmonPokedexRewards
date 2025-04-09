package eatAWESOME.pixelmonpokedexrewards;

import eatAWESOME.pixelmonpokedexrewards.events.CapabilityListener;
import eatAWESOME.pixelmonpokedexrewards.events.PokedexListener;
import eatAWESOME.pixelmonpokedexrewards.capabilities.CapabilityHandler;
import eatAWESOME.pixelmonpokedexrewards.commands.PokedexCmd;
import eatAWESOME.pixelmonpokedexrewards.commands.PokedexRewards;
import eatAWESOME.pixelmonpokedexrewards.commands.PokedexTop;
import eatAWESOME.pixelmonpokedexrewards.config.RewardConfigLoader;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PixelmonPokedexRewards.MOD_ID)
@Mod.EventBusSubscriber(modid = PixelmonPokedexRewards.MOD_ID)
public class PixelmonPokedexRewards {

	public static final String MOD_ID = "pixelmonpokedexrewards";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	private static PixelmonPokedexRewards instance;
	
	private JsonObject rewardConfig;
	
    public PixelmonPokedexRewards() {
        instance = this;
    	reloadConfig();
        MinecraftForge.EVENT_BUS.register(this);
    	IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(PixelmonPokedexRewards::onModLoad);
    }

    public static void onModLoad(FMLCommonSetupEvent event) {
    	CapabilityHandler.register();
    	MinecraftForge.EVENT_BUS.register(new CapabilityListener());
    	Pixelmon.EVENT_BUS.register(new PokedexListener());
    }
    
    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
    }

    public void reloadConfig() {
    	this.rewardConfig = RewardConfigLoader.reloadRewardConfig();
    }
    
    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
    }
    
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        PokedexCmd.register(event.getDispatcher());
        PokedexRewards.register(event.getDispatcher());
        PokedexTop.register(event.getDispatcher());
    }
    
    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event) {
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event) {
    }
    
    public static PixelmonPokedexRewards getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static JsonObject getRewardConfig() {
        return instance.rewardConfig;
    }
}
