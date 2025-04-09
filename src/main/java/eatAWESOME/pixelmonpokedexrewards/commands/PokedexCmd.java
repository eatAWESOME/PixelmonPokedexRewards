package eatAWESOME.pixelmonpokedexrewards.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.pokedex.PlayerPokedex;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.Commands;
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
            .then(Commands.argument("target", StringArgumentType.word())
            	.executes(context -> {
	                String targetName = StringArgumentType.getString(context, "target");
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
            	})));
	}
}