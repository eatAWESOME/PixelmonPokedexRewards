package eatAWESOME.pixelmonpokedexrewards.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.pokedex.PlayerPokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;

import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class PokedexCmd {

	public PokedexCmd() {
    }
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("pokedex")
	            .executes(context -> {
	            	ServerPlayerEntity player = context.getSource().getPlayerOrException();
	            	PlayerPartyStorage partyStorage = StorageProxy.getParty(player);
	            	PlayerPokedex playerPokedex = partyStorage.playerPokedex;
	            	float seenPercentage = playerPokedex.getSeenCompletionPercentage();
	                float caughtPercentage = playerPokedex.getCaughtCompletionPercentage();
	                TextComponent message = new StringTextComponent(String.format("Your Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
	                context.getSource().sendSuccess(message, false);
	                return Command.SINGLE_SUCCESS;
	            })
            .then(Commands.argument("targetPlayer", StringArgumentType.word())
	        		.suggests((context, builder) -> {
	                    List<String> suggestions = new ArrayList<>();
	                    File dataDir = new File("");
		            	if (context.getSource().getServer().isDedicatedServer()) {
			            	dataDir = new File(context.getSource().getServer().getServerDirectory(), context.getSource().getServer().getWorldData().getLevelName() + "/playerdata");
		            	} else {
		            		dataDir = new File(context.getSource().getServer().getServerDirectory(), "saves/" + context.getSource().getServer().getWorldData().getLevelName() + "/playerdata");
		            	}
		            	File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
		            	for (File file : files) {
		                    try {
		                        String uuidStr = file.getName().replace(".dat", "");
		                        suggestions.add(context.getSource().getServer().getProfileCache().get(UUID.fromString(uuidStr)).getName());
		                    } catch (Exception e) {
		                        e.printStackTrace();
		                    }
		                }
	                    return ISuggestionProvider.suggest(suggestions, builder);
	            	})
	            	.executes(context -> {
		                String targetName = StringArgumentType.getString(context, "targetPlayer");
		                PlayerPartyStorage partyStorage = StorageProxy.getParty(context.getSource().getServer().getProfileCache().get(targetName).getId());
		            	PlayerPokedex playerPokedex = partyStorage.playerPokedex;
		            	float seenPercentage = playerPokedex.getSeenCompletionPercentage();
		                float caughtPercentage = playerPokedex.getCaughtCompletionPercentage();
		                if (caughtPercentage == 0) {
		                	context.getSource().sendFailure(new StringTextComponent(targetName + " not found."));
		                    return 0;
		                }
		                TextComponent message = new StringTextComponent(String.format(targetName + "'s Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
		                if (targetName.equals(context.getSource().getPlayerOrException().getDisplayName().getString())) {
		                	message = new StringTextComponent(String.format("Your Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
		                }
		                context.getSource().sendSuccess(message, false);
		                return Command.SINGLE_SUCCESS;
	            	})
		            .then(Commands.argument("targetPokemon", StringArgumentType.word())
		            		.suggests((context, builder) -> {
		            			List<String> suggestions = new ArrayList<>();
		                        suggestions.add("disable");
		                        for (Species pokedexSpecies : Pokedex.actualPokedex) {
		                            suggestions.add(pokedexSpecies.getStrippedName());
		                        }
		                        return ISuggestionProvider.suggest(suggestions, builder);
		                    })
		                	.executes(context -> {
		    	                String targetName = StringArgumentType.getString(context, "targetPlayer");
		    	                String targetPokemon = StringArgumentType.getString(context, "targetPokemon");
		    	                PlayerPartyStorage partyStorage = StorageProxy.getParty(context.getSource().getServer().getProfileCache().get(targetName).getId());
		    	            	PlayerPokedex playerPokedex = partyStorage.playerPokedex;
		    	                float caughtPercentage = playerPokedex.getCaughtCompletionPercentage();
		    	                if (caughtPercentage == 0) {
		    	                	context.getSource().sendFailure(new StringTextComponent(targetName + " not found."));
		    	                    return 0;
		    	                }
		    	                if (!validateSpecies(targetPokemon)) {
		    	                	context.getSource().sendFailure(new StringTextComponent("Invalid target (" + targetPokemon + "). Entry must be pokémon species."));
		    	                    return 0;
		    	                }
		    	                Species targetSpecies = getSpecies(targetPokemon);
		    	                String status = "";
		    	                if (playerPokedex.hasCaught(targetSpecies)) {
		    	                	status = "caught";
		    	                } else if (playerPokedex.hasSeen(targetSpecies)) {
		    	                	status = "seen";
		    	                } else {
		    	                	status = "not seen";
		    	                }
		    	                String prefix = getPrefix(targetSpecies);
		    	                TextComponent message = new StringTextComponent(targetName + " has " + status + prefix+ targetPokemon + ".");
		    	                if (targetName.equals(context.getSource().getPlayerOrException().getDisplayName().getString())) {
		    	                	message = new StringTextComponent("You have " + status + prefix + targetPokemon + ".");
		    	                }
		    	                context.getSource().sendSuccess(message, false);
		    	                return Command.SINGLE_SUCCESS;
		                	}))));
	}
	
	public static boolean validateSpecies(String targetPokemon) {
		for (Species pokedexSpecies : Pokedex.actualPokedex) {
            if (pokedexSpecies.getStrippedName().equals(targetPokemon)) {
            	return true;
            }
		}
        return false;
	}
	
	public static Species getSpecies(String targetPokemon) {
		for (Species pokedexSpecies : Pokedex.actualPokedex) {
            if (pokedexSpecies.getStrippedName().equals(targetPokemon)) {
            	return pokedexSpecies;
            }
		}
        return null;
	}
	
	public static String getPrefix(Species species) {
		String prefix = "";
		char firstChar = species.getTranslatedName().getString().toLowerCase().charAt(0);
		if (firstChar == 'a' || firstChar == 'e' || firstChar == 'i' || firstChar == 'o' || firstChar == 'u' || firstChar == 'y') {
			prefix = " an ";
		} else {
			prefix = " a ";
		}
		return prefix;
	}
}