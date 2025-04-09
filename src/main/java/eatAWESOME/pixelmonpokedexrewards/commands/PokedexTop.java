package eatAWESOME.pixelmonpokedexrewards.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.pokedex.PlayerPokedex;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.Commands;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class PokedexTop {

	public PokedexTop() {
    }
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("pokedextop")
	            .executes(context -> {
	            	File dataDir = new File("");
	            	if (context.getSource().getServer().isDedicatedServer()) {
		            	dataDir = new File(context.getSource().getServer().getServerDirectory(), context.getSource().getServer().getWorldData().getLevelName() + "/playerdata");
	            	} else {
	            		dataDir = new File(context.getSource().getServer().getServerDirectory(), "saves/" + context.getSource().getServer().getWorldData().getLevelName() + "/playerdata");
	            	}
	            	File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
	            	List<PlayerPokedexData> pokedexData = new ArrayList<>();
	            	if (files == null) {
	                	context.getSource().sendFailure(new StringTextComponent("Error loading player data"));
	                    return 0;
	                }
	                
	            	for (File file : files) {
	                    try {
	                        String uuidStr = file.getName().replace(".dat", "");
	                        UUID uuid = UUID.fromString(uuidStr);
	                        PlayerPartyStorage partyStorage = StorageProxy.getParty(uuid);
	    	            	PlayerPokedex playerPokedex = partyStorage.playerPokedex;
	    	            	float seenPercentage = playerPokedex.getSeenCompletionPercentage();
	    	                float caughtPercentage = playerPokedex.getCaughtCompletionPercentage();
	                        String playerName = partyStorage.getPlayerName();
	                        pokedexData.add(new PlayerPokedexData(playerName, caughtPercentage, seenPercentage));
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	                pokedexData.sort(Comparator.comparingDouble((PlayerPokedexData data) -> data.caughtPercentage)
                            .thenComparingDouble(data -> data.seenPercentage)
                            .reversed());
	                
	                StringBuilder leaderboardMessage = new StringBuilder();
	                leaderboardMessage.append("Pokédex Progress Leaderboard:");
	                
	                int maxEntries = Math.min(10, pokedexData.size());
	                for (int i = 0; i < maxEntries; i++) {
	                    PlayerPokedexData data = pokedexData.get(i);
	                    leaderboardMessage.append(String.format("\n%d. %s, %.2f%%", i + 1, data.playerName, data.caughtPercentage));
	                }
	                
	                if (context.getSource().getEntity() != null) {
	                	ServerPlayerEntity requester = context.getSource().getPlayerOrException();
		                String requesterName = requester.getGameProfile().getName();
		                int requesterRank = -1;
		                for (int i = 0; i < pokedexData.size(); i++) {
		                    if (pokedexData.get(i).playerName.equalsIgnoreCase(requesterName)) {
		                        requesterRank = i + 1;
		                        break;
		                    }
		                }
		                if (requesterRank > 10) {
		                    PlayerPokedexData data = pokedexData.get(requesterRank - 1);
		                    leaderboardMessage.append("\n...");
		                    leaderboardMessage.append(String.format("\n%d. %s, %.2f%%", requesterRank, data.playerName, data.caughtPercentage));
		                }
	                }
	                context.getSource().sendSuccess(new StringTextComponent(leaderboardMessage.toString()), false);
	                return Command.SINGLE_SUCCESS;
	            }));
	}
	
	public static class PlayerPokedexData {
        String playerName;
        float caughtPercentage;
        float seenPercentage;

        public PlayerPokedexData(String playerName, float caughtPercentage, float seenPercentage) {
            this.playerName = playerName;
            this.caughtPercentage = caughtPercentage;
            this.seenPercentage = seenPercentage;
        }
    }
}