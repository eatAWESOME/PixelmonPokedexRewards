package eatAWESOME.pixelmonpokedexrewards.events;

import eatAWESOME.pixelmonpokedexrewards.PixelmonPokedexRewards;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.pixelmonmod.pixelmon.api.enums.PositionTriState;
import com.pixelmonmod.pixelmon.api.events.PokedexEvent;
import com.pixelmonmod.pixelmon.api.drops.CustomDropScreenFactory;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

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
	        	reward(player, 0);
	        } else {
	        	for (int i = 1; i <= 100; i += 1) {
		            if (completionPercentage >= i && previousCount < i) {
		            	reward(player, i);
		                break;
		            }
		        }
	        }
		}
	}
	
    public void reward(ServerPlayerEntity player, int pokedexCompletion) {
    	JsonObject reward = getRewardData(pokedexCompletion);
    	TextComponent message = new StringTextComponent("");
    	if (reward != null) {
    		if (reward.has("message")) {
    			message = new StringTextComponent(reward.get("message").getAsString()
    					.replace("Pokemon", "Pokémon")
                        .replace("Pokedex", "Pokédex")
                        .replace("pokemon", "pokémon")
                        .replace("pokedex", "pokédex"));
    			player.sendMessage(message, player.getUUID());
    		}
    		if (reward.has("items")) {
    			List<ItemStack> itemStacks = new ArrayList<>();
    			JsonArray itemsArray = reward.getAsJsonArray("items");
				for (JsonElement itemElement : itemsArray) {
					JsonObject item = itemElement.getAsJsonObject();
					ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.get("itemID").getAsString())), 1);
					int count = item.get("count").getAsInt();
					if (item.has("nbt")) {
						CompoundNBT nbt = itemStack.getOrCreateTag();
						JsonObject nbtData = item.getAsJsonObject("nbt");
						for (Map.Entry<String, JsonElement> entry : nbtData.entrySet()) {
				            nbt.putString(entry.getKey(), entry.getValue().getAsString());
				        }
				        itemStack.setTag(nbt);
					}
			    	int maxCount = itemStack.getMaxStackSize();
					while (count > 0) {
						int interimCount = Math.min(count, maxCount);
						ItemStack interimItemStack = itemStack.copy(); 
						interimItemStack.setCount(interimCount);
						if (!interimItemStack.isEmpty()) {
							itemStacks.add(interimItemStack);
						} else {
							System.err.println("Invalid item. Skipping giving " + item.get("count").getAsInt() + " " + item.get("itemID").getAsString() + " to " + player.getDisplayName().getString());
						}
						count = count - interimCount;
					}
        		}
        		giveItems(player, itemStacks, message);
    		}
    		if (reward.has("pokemon")) {
    			JsonArray pokemonArray = reward.getAsJsonArray("pokemon");
				for (JsonElement pokemonElement : pokemonArray) {
					try {
						JsonObject pokemonArgs = pokemonElement.getAsJsonObject();
						String species = "random";
						if (pokemonArgs.has("species")) {
							species = pokemonArgs.get("species").getAsString();
						}
						boolean perfect = false;
						if (pokemonArgs.has("perfect")) {
							perfect = pokemonArgs.get("perfect").getAsBoolean();
						}
						boolean shiny = false;
						if (pokemonArgs.has("shiny")) {
							shiny = pokemonArgs.get("shiny").getAsBoolean();
						}
						givePokemon(player, species, perfect, shiny);
					} catch (Exception e) {
						System.err.println("Error while giving pokémon to " + player.getDisplayName().getString() + ": " + e.getMessage());
					    e.printStackTrace();
					}
				}
    		}
    	}
    }
    
    public void giveItems(ServerPlayerEntity player, List<ItemStack> itemStacks, ITextComponent message) {
    	CustomDropScreenFactory.Builder builder = new CustomDropScreenFactory.Builder()
    			.setMnemonic(message.getString())
    			.setButtonText(PositionTriState.CENTER, new StringTextComponent("Claim All"))
                .setTitle(message)
                .addItems(itemStacks)
                .escapeDoesNotClose();
    	builder.buttonHandler(clickButton -> {
    		List<ItemStack> itemStacks2 = new ArrayList<>(itemStacks);
    		for (ItemStack itemStack : itemStacks2) {
        		addItemsToInventory(player, itemStack);
        		itemStacks.remove(itemStack);
        	}
        });
    	builder.dropHandler((clickDrop, itemStack) -> {
    		addItemsToInventory(player, itemStack);
    		itemStacks.remove(itemStack);
        });
        builder.closeHandler(closeScreen -> {
        	List<ItemStack> itemStacks2 = new ArrayList<>(itemStacks);
    		for (ItemStack itemStack : itemStacks2) {
        		addItemsToInventory(player, itemStack);
        		itemStacks.remove(itemStack);
        	}
        });
        builder.sendTo(player);
    }
    
    public void addItemsToInventory(ServerPlayerEntity player, ItemStack itemStack) {
    	int count = itemStack.getCount();
    	int maxCount = itemStack.getMaxStackSize();
    	int spaceSlot = player.inventory.getSlotWithRemainingSpace(itemStack);
		while (spaceSlot >= 0 && count > 0) {
    		spaceSlot = player.inventory.getSlotWithRemainingSpace(itemStack);
    		if (spaceSlot < 0) {
    	        break;
    	    }
    		int spaceCount = player.inventory.getItem(spaceSlot).getCount();
    		int interimCount = Math.min(maxCount - spaceCount, count);
    		ItemStack interimItemStack = itemStack.copy(); 
			interimItemStack.setCount(interimCount);
			player.inventory.add(spaceSlot, interimItemStack);
    		count = count - interimCount;
    	}
		int freeSlot = player.inventory.getFreeSlot();
    	while (freeSlot >= 0 && count > 0) {
    		freeSlot = player.inventory.getFreeSlot();
    		if (freeSlot < 0) {
    	        break;
    	    }
    		int interimCount = Math.min(maxCount, count);
    		ItemStack interimItemStack = itemStack.copy(); 
			interimItemStack.setCount(interimCount);
			player.inventory.add(freeSlot, interimItemStack);
    		count = count - interimCount;
    	}
		if (count > 0) {
			ItemStack interimItemStack = itemStack.copy(); 
			interimItemStack.setCount(count);
			player.drop(interimItemStack, true);
            TextComponent message = new StringTextComponent("Inventory Full! Dropped " + count + " " + interimItemStack.getItem().getName(interimItemStack).getString() + "!");
            player.sendMessage(message, player.getUUID());
    	}
    }
    
    public void givePokemon(ServerPlayerEntity player, String species, boolean perfect, boolean shiny) {
		Random random = new Random();
    	PokemonBuilder pokemonBuilder = PokemonBuilder.builder();
    	if (species.equals("randomLegendaryMythicalUltraBeast")) {
    		List<Species> legendaryMythicalUltraBeastList = new ArrayList<>();
    		for (Species pokedexSpecies : Pokedex.actualPokedex) {
                if (pokedexSpecies != null && (pokedexSpecies.isLegendary() || pokedexSpecies.isMythical() || pokedexSpecies.isUltraBeast())) {
                	legendaryMythicalUltraBeastList.add(pokedexSpecies);
                }
            }
    		pokemonBuilder = pokemonBuilder.species(legendaryMythicalUltraBeastList.get(random.nextInt(legendaryMythicalUltraBeastList.size())));
    	} else if (!species.equals("random")) {
    		pokemonBuilder = pokemonBuilder.species(species);
    	} else {
    		pokemonBuilder = pokemonBuilder.randomSpecies(false, false, false);
    	}
    	if (pokemonBuilder.getSpecies().getTranslatedName().getString().equals("MissingNo")) {
    		System.out.println("MissingNo caught and set to random");
    		pokemonBuilder = pokemonBuilder.randomSpecies(false, false, false);
    	}
    	if (perfect) {
			pokemonBuilder = pokemonBuilder.ivs(31, 31, 31, 31, 31, 31);
		} else {
			pokemonBuilder = pokemonBuilder.ivs(random.nextInt(12) + 20, random.nextInt(12) + 20, random.nextInt(12) + 20, random.nextInt(12) + 20, random.nextInt(12) + 20, random.nextInt(12) + 20);
		}
    	Pokemon pokemon = pokemonBuilder
    			.caughtBall(PokeBallRegistry.CHERISH_BALL.getValueUnsafe())
    			.shiny(shiny)
    			.build();
    	pokemon.setLevel(random.nextInt(pokemon.getForm().maxLevel - pokemon.getForm().minLevel + 1) + pokemon.getForm().minLevel);
    	PlayerPartyStorage party = StorageProxy.getParty(player);
        TextComponent message = new StringTextComponent("");
        if (party != null) {
            party.add(pokemon);
            if (pokemon.isShiny()) {
            	message = new StringTextComponent("You received a shiny " + pokemon.getSpecies().getTranslatedName().getString() + "!");
            } else {
            	message = new StringTextComponent("You received a " + pokemon.getSpecies().getTranslatedName().getString() + "!");
            }
            player.sendMessage(message, player.getUUID());
        } else {
            System.err.println("Error giving " + pokemon.getSpecies().getTranslatedName().getString() + " to " + player.getDisplayName().getString());
        }
    }

    public static JsonObject getRewardData(int pokedexCompletion) {
    	JsonObject rewardConfigData = PixelmonPokedexRewards.getRewardConfig();
        return rewardConfigData.has(String.valueOf(pokedexCompletion)) ? rewardConfigData.getAsJsonObject(String.valueOf(pokedexCompletion)) : null;
    }
}