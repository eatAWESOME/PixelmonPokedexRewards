package eatawesome.pixelmonpokedexrewards.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.pokedex.PokeDexStorageProxy;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexStorage;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBase;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PokedexCmd {

    public PokedexCmd() {
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pokedex")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PokedexStorage playerPokedex = PokeDexStorageProxy.getStorage(player).join();
                    double seenPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.SEEN) * 100.0;
                    double caughtPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0;
                    Component message = Component.literal(String.format("Your Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
                    context.getSource().sendSuccess(() -> message, false);
                    return Command.SINGLE_SUCCESS;
                })
            .then(Commands.argument("targetPlayer", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        List<String> suggestions = new ArrayList<>();
                        File dataDir = new File("");
                        if (context.getSource().getServer().isDedicatedServer()) {
                            dataDir = context.getSource().getServer().getServerDirectory().resolve(context.getSource().getServer().getWorldData().getLevelName()).resolve("playerdata").toFile();
                        } else {
                            dataDir = context.getSource().getServer().getServerDirectory().resolve("saves").resolve(context.getSource().getServer().getWorldData().getLevelName()).resolve("playerdata").toFile();
                        }
                        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
                        for (File file : files) {
                            try {
                                String uuidStr = file.getName().replace(".dat", "");
                                suggestions.add(context.getSource().getServer().getProfileCache().get(UUID.fromString(uuidStr)).get().getName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return SharedSuggestionProvider.suggest(suggestions, builder);
                    })
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, "targetPlayer");
                        UUID targetPlayerUUID = context.getSource().getServer().getProfileCache().get(targetName).get().getId();
                        PokedexStorage playerPokedex = PokeDexStorageProxy.getStorage(targetPlayerUUID).join();
                        double seenPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.SEEN) * 100.0;
                        double caughtPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0;
                        if (caughtPercentage == 0) {
                            context.getSource().sendFailure(Component.literal(targetName + " not found."));
                            return 0;
                        }
                        Component message = Component.literal(String.format(targetName + "'s Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
                        if (targetName.equals(context.getSource().getPlayerOrException().getDisplayName().getString())) {
                            message = Component.literal(String.format("Your Pokédex Progress: Caught: %.2f%%, Seen: %.2f%%", caughtPercentage, seenPercentage));
                        }
                        final Component final_message = message;
                        context.getSource().sendSuccess(() -> final_message, false);
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("targetPokemon", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                List<String> suggestions = new ArrayList<>();
                                suggestions.add("disable");
                                for (Species pokedexSpecies : PixelmonSpecies.getAll()) {
                                    suggestions.add(pokedexSpecies.getStrippedName());
                                }
                                return SharedSuggestionProvider.suggest(suggestions, builder);
                            })
                            .executes(context -> {
                                String targetName = StringArgumentType.getString(context, "targetPlayer");
                                String targetPokemon = StringArgumentType.getString(context, "targetPokemon");
                                UUID targetPlayerUUID = context.getSource().getServer().getProfileCache().get(targetName).get().getId();
                                PokedexStorage playerPokedex = PokeDexStorageProxy.getStorage(targetPlayerUUID).join();
                                double caughtPercentage = playerPokedex.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0;
                                if (caughtPercentage == 0) {
                                    context.getSource().sendFailure(Component.literal(targetName + " not found."));
                                    return 0;
                                }
                                if (!validateSpecies(targetPokemon)) {
                                    context.getSource().sendFailure(Component.literal("Invalid target (" + targetPokemon + "). Entry must be pokémon species."));
                                    return 0;
                                }
                                Species targetSpecies = getSpecies(targetPokemon);
                                PokemonBase targetBase = new PokemonBase(targetSpecies);
                                String status = "";
                                if (playerPokedex.hasCaught(targetBase)) {
                                    status = "caught";
                                } else if (playerPokedex.hasSeen(targetBase)) {
                                    status = "seen";
                                } else {
                                    status = "not seen";
                                }
                                String prefix = getPrefix(targetSpecies);
                                Component message = Component.literal(targetName + " has " + status + prefix+ targetPokemon + ".");
                                if (targetName.equals(context.getSource().getPlayerOrException().getDisplayName().getString())) {
                                    message = Component.literal("You have " + status + prefix + targetPokemon + ".");
                                }
                                final Component final_message = message;
                                context.getSource().sendSuccess(() -> final_message, false);
                                return Command.SINGLE_SUCCESS;
                            }))));
    }
        
    public static boolean validateSpecies(String targetPokemon) {
        for (Species pokedexSpecies : PixelmonSpecies.getAll()) {
            if (pokedexSpecies.getStrippedName().equals(targetPokemon)) {
                return true;
            }
        }
        return false;
    }
    
    public static Species getSpecies(String targetPokemon) {
        for (Species pokedexSpecies : PixelmonSpecies.getAll()) {
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