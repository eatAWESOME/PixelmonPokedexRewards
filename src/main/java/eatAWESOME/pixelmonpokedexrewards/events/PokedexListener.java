package eatAWESOME.pixelmonpokedexrewards.events;

import eatAWESOME.pixelmonpokedexrewards.capabilities.IPokedexData;
import eatAWESOME.pixelmonpokedexrewards.capabilities.PokedexDataProvider;
import eatAWESOME.pixelmonpokedexrewards.commands.PokedexRewards;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.api.events.PokedexEvent;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PokedexListener {

	@SubscribeEvent
	public void onPokedexEvent(PokedexEvent.Post event) {
		if (event.getNewStatus() == PokedexRegistrationStatus.CAUGHT && event.getOldStatus() != PokedexRegistrationStatus.CAUGHT) {
			ServerPlayerEntity player = event.getPlayer();
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			float completionPercentage = event.getPokedex().getCaughtCompletionPercentage();
			int completionCount = event.getPokedex().countCaught();
			int totalCount = Pokedex.actualPokedex.length;
			TextComponent message = new StringTextComponent(event.getPokemon().getSpecies().getTranslatedName().getString() + " added to Pokédex. Completion: " + completionCount + "/" + totalCount + ", "+ decimalFormat.format(completionPercentage) + "%");
	        player.sendMessage(message, player.getUUID());
	        float previousCount = ((float) (completionCount - 1) / totalCount) * 100;
	        if (completionCount == 1) {
	        	LazyOptional<IPokedexData> cap = player.getCapability(PokedexDataProvider.pokedexLocation);
                cap.ifPresent(data -> {
                    data.setPokedexRewardsLevel(0);
                    data.setClaimedPokedexRewards(new ArrayList<Integer>());
                    JsonObject reward = PokedexRewards.getRewardData(0);
                    if (reward != null) {
                    	if (reward.has("message")) {
                			TextComponent rewardMessage = new StringTextComponent(reward.get("message").getAsString()
                					.replace("Pokemon", "Pokémon")
                                    .replace("Pokedex", "Pokédex")
                                    .replace("pokemon", "pokémon")
                                    .replace("pokedex", "pokédex"));
                			player.sendMessage(rewardMessage, player.getUUID());
                		}
                        PokedexRewards.reward(player, 0);
                        List<Integer> claimedRewards = data.getClaimedPokedexRewards();
                        claimedRewards.add(0);
                        data.setClaimedPokedexRewards(claimedRewards);
                    }
                });
	        } else {
	        	for (int i = 1; i <= 100; i += 1) {
		            if (completionPercentage >= i && previousCount < i) {
		            	final int rewardValue = i;
		            	LazyOptional<IPokedexData> cap = player.getCapability(PokedexDataProvider.pokedexLocation);
		                cap.ifPresent(data -> {
		                    data.setPokedexRewardsLevel(rewardValue);
		                    JsonObject reward = PokedexRewards.getRewardData(rewardValue);
		                    if (reward.has("message")) {
		            			TextComponent rewardMessage = new StringTextComponent(reward.get("message").getAsString()
		            					.replace("Pokemon", "Pokémon")
		                                .replace("Pokedex", "Pokédex")
		                                .replace("pokemon", "pokémon")
		                                .replace("pokedex", "pokédex"));
		            			player.sendMessage(rewardMessage, player.getUUID());
		            			player.sendMessage(new StringTextComponent("Use /pokedexrewards to claim your reward!"), player.getUUID());
		            		}
		                });
		                break;
		            }
		        }
	        }
		}
	}
}