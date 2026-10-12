package eatAWESOME.pixelmonpokedexrewards.events;

import eatAWESOME.pixelmonpokedexrewards.commands.PokedexRewards;
import eatAWESOME.pixelmonpokedexrewards.data.AttachmentHandler;
import eatAWESOME.pixelmonpokedexrewards.data.PokedexDataAttachment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.api.pokedex.PokeDexStorageProxy;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexStorage;
import com.pixelmonmod.pixelmon.api.pokedex.event.PokedexEvent;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.network.chat.Component;

public class PokedexListener {

	@SubscribeEvent
	public void onPokedexEvent(PokedexEvent.Post event) {
		if (event.getNewStatus() == PokedexRegistrationStatus.CAUGHT && event.getOldStatus() != PokedexRegistrationStatus.CAUGHT) {
			ServerPlayer player = event.getPlayer();
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			PokedexStorage playerPokedex = PokeDexStorageProxy.getStorage(player).join();
        	double caughtPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0;
            int caughtCount = playerPokedex.countCaught();
			int totalCount = PixelmonSpecies.getAll().size();
			Component message = Component.literal(event.getPokemon().getSpecies().getTranslatedName().getString() + " added to Pokédex. Completion: " + caughtCount + "/" + totalCount + ", "+ decimalFormat.format(caughtPercentage) + "%");
	        player.sendSystemMessage(message);
	        float previousCount = ((float) (caughtCount - 1) / totalCount) * 100;
	        if (caughtCount == 1) {
	        	PokedexDataAttachment data = player.getData(AttachmentHandler.POKEDEX_DATA);
                data.setPokedexRewardsLevel(0);
                data.setClaimedPokedexRewards(new ArrayList<Integer>());
                JsonObject reward = PokedexRewards.getRewardData(0);
                if (reward != null) {
                	if (reward.has("message")) {
            			Component rewardMessage = Component.literal(reward.get("message").getAsString()
            					.replace("Pokemon", "Pokémon")
                                .replace("Pokedex", "Pokédex")
                                .replace("pokemon", "pokémon")
                                .replace("pokedex", "pokédex"));
            			player.sendSystemMessage(rewardMessage);
            		}
                    PokedexRewards.reward(player, 0);
                    List<Integer> claimedRewards = data.getClaimedPokedexRewards();
                    claimedRewards.add(0);
                    data.setClaimedPokedexRewards(claimedRewards);
                }
	        } else {
	        	for (int i = 1; i <= 100; i += 1) {
		            if (caughtPercentage >= i && previousCount < i) {
		            	final int rewardValue = i;
		            	PokedexDataAttachment data = player.getData(AttachmentHandler.POKEDEX_DATA);
		            	data.setPokedexRewardsLevel(rewardValue);
	                    JsonObject reward = PokedexRewards.getRewardData(rewardValue);
	                    if (reward != null) {
		                    if (reward.has("message")) {
		            			Component rewardMessage = Component.literal(reward.get("message").getAsString()
		            					.replace("Pokemon", "Pokémon")
		                                .replace("Pokedex", "Pokédex")
		                                .replace("pokemon", "pokémon")
		                                .replace("pokedex", "pokédex"));
		            			player.sendSystemMessage(rewardMessage);
		            			player.sendSystemMessage(Component.literal("Use /pokedexrewards to claim your reward!"));
		            		}
	                    }
		                break;
		            }
		        }
	        }
		}
	}
}