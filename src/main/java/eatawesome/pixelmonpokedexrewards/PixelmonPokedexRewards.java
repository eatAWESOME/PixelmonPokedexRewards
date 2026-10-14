package eatawesome.pixelmonpokedexrewards;

import eatawesome.pixelmonpokedexrewards.commands.PokedexCmd;
import eatawesome.pixelmonpokedexrewards.commands.PokedexRewards;
import eatawesome.pixelmonpokedexrewards.commands.PokedexTop;
import eatawesome.pixelmonpokedexrewards.config.RewardConfigLoader;
import eatawesome.pixelmonpokedexrewards.data.AttachmentHandler;
import eatawesome.pixelmonpokedexrewards.events.PokedexListener;

import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PixelmonPokedexRewards.MOD_ID)
public class PixelmonPokedexRewards {

    public static final String MOD_ID = "pixelmonpokedexrewards";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    private static PixelmonPokedexRewards instance;
    
    private JsonObject rewardConfig;
    
    public PixelmonPokedexRewards(IEventBus modEventBus) {
        instance = this;
        reloadConfig();

        AttachmentHandler.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(PixelmonPokedexRewards::onModLoad);
        NeoForge.EVENT_BUS.register(this);
    }

    public static void onModLoad(FMLCommonSetupEvent event) {
        Pixelmon.EVENT_BUS.register(new PokedexListener());
    }

    public void reloadConfig() {
        this.rewardConfig = RewardConfigLoader.reloadRewardConfig();
    }
    
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        PokedexCmd.register(event.getDispatcher());
        PokedexRewards.register(event.getDispatcher());
        PokedexTop.register(event.getDispatcher());
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
