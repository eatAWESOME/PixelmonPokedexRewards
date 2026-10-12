package eatAWESOME.pixelmonpokedexrewards.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.pokedex.PokeDexStorageProxy;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexStorage;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PokedexTop {

	public PokedexTop() {
    }
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pokedextop")
	            .executes(context -> {
	            	File dataDir = new File("");
	            	
	            	if (context.getSource().getServer().isDedicatedServer()) {
	            		dataDir = context.getSource().getServer().getServerDirectory().resolve(context.getSource().getServer().getWorldData().getLevelName()).resolve("playerdata").toFile();
	            	} else {
	            		dataDir = context.getSource().getServer().getServerDirectory().resolve("saves").resolve(context.getSource().getServer().getWorldData().getLevelName()).resolve("playerdata").toFile();
	            	}
	            	File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
	            	List<PlayerPokedexData> pokedexData = new ArrayList<>();
	            	if (files == null) {
	                	context.getSource().sendFailure(Component.literal("Error loading player data"));
	                    return 0;
	                }
	                
	            	for (File file : files) {
	                    try {
	                        String uuidStr = file.getName().replace(".dat", "");
	                        UUID uuid = UUID.fromString(uuidStr);
	                        PokedexStorage playerPokedex = PokeDexStorageProxy.getStorage(uuid).join();
	                        double seenPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.SEEN) * 100.0;
	    	            	double caughtPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0;
	                        String playerName = playerPokedex.getOwner().getDisplayName().getString();
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
	                	ServerPlayer requester = context.getSource().getPlayerOrException();
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
	                context.getSource().sendSuccess(() -> Component.literal(leaderboardMessage.toString()), false);
	                return Command.SINGLE_SUCCESS;
	            }));
	}
	
	public static class PlayerPokedexData {
        String playerName;
        double caughtPercentage;
        double seenPercentage;

        public PlayerPokedexData(String playerName, double caughtPercentage, double seenPercentage) {
            this.playerName = playerName;
            this.caughtPercentage = caughtPercentage;
            this.seenPercentage = seenPercentage;
        }
    }
}