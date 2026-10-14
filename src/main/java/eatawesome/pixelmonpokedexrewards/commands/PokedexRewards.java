package eatawesome.pixelmonpokedexrewards.commands;

import eatawesome.pixelmonpokedexrewards.PixelmonPokedexRewards;
import eatawesome.pixelmonpokedexrewards.data.AttachmentHandler;
import eatawesome.pixelmonpokedexrewards.data.PokedexDataAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.drops.CustomDropScreenFactory;
import com.pixelmonmod.pixelmon.api.enums.PositionTriState;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.init.registry.PixelmonDataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;



public class PokedexRewards {

    public PokedexRewards() {
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pokedexrewards")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    PokedexDataAttachment data = player.getData(AttachmentHandler.POKEDEX_DATA);
                    int pokedexLevel = data.getPokedexRewardsLevel();
                    List<Integer> claimedRewards = data.getClaimedPokedexRewards();
                    JsonObject rewardConfigData = PixelmonPokedexRewards.getRewardConfig();
                    List<Integer> rewards = rewardConfigData.entrySet().stream()
                            .map(entry -> Integer.parseInt(entry.getKey()))
                            .collect(Collectors.toList());
                    int nextReward = rewards.stream()
                            .filter(reward -> !claimedRewards.contains(reward))
                            .sorted()
                            .findFirst()
                            .orElse(-1);
                    if (nextReward <= pokedexLevel) {
                        context.getSource().sendSuccess(() -> Component.literal("Current Pokédex Completion: " + pokedexLevel + "%"), false);
                        context.getSource().sendSuccess(() -> Component.literal("Claiming " + nextReward + "% Rewards"), false);
                        reward(player, nextReward);
                        claimedRewards.add(nextReward);
                        data.setClaimedPokedexRewards(claimedRewards);
                        int newNextReward = rewards.stream()
                                .filter(reward -> !claimedRewards.contains(reward))
                                .sorted()
                                .findFirst()
                                .orElse(-1);
                        if (newNextReward < pokedexLevel) {
                            context.getSource().sendSuccess(() -> Component.literal("Another reward level is ready to claim: " + newNextReward + "%"), false);
                            context.getSource().sendSuccess(() -> Component.literal("Repeat this command to claim the next reward."), false);
                        } else {
                            context.getSource().sendSuccess(() -> Component.literal("Next Reward Level: " + newNextReward + "%"), false);
                        }
                    } else {
                        context.getSource().sendSuccess(() -> Component.literal("Current Pokédex Completion: " + pokedexLevel + "%"), false);
                        context.getSource().sendSuccess(() -> Component.literal("Next Reward Level: " + nextReward + "%"), false);
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
    
    public static void reward(ServerPlayer player, int pokedexCompletion) {
        JsonObject reward = getRewardData(pokedexCompletion);
        Component message = Component.literal("");
        if (reward != null) {
            if (reward.has("items")) {
                List<ItemStack> itemStacks = new ArrayList<>();
                JsonArray itemsArray = reward.getAsJsonArray("items");
                for (JsonElement itemElement : itemsArray) {
                    JsonObject item = itemElement.getAsJsonObject();
                    ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(item.get("itemID").getAsString())), 1);
                    int count = item.get("count").getAsInt();
                    if (item.has("nbt")) {
                        CompoundTag nbt = new CompoundTag();
                        JsonObject nbtData = item.getAsJsonObject("nbt");
                        for (Map.Entry<String, JsonElement> entry : nbtData.entrySet()) {
                            if (entry.getKey().equals("pixelmon:poke_ball")) {
                                itemStack.set(PixelmonDataComponents.POKE_BALL.get(), PokeBallRegistry.getPokeBall(entry.getValue().getAsString()));
                            } else {
                                nbt.putString(entry.getKey(), entry.getValue().getAsString());
                                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                            }
                        }
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
    
    public static void giveItems(ServerPlayer player, List<ItemStack> itemStacks, Component message) {
        CustomDropScreenFactory.Builder builder = new CustomDropScreenFactory.Builder()
                .setMnemonic(message.getString())
                .setButtonText(PositionTriState.CENTER, Component.literal("Claim All"))
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
    
    public static void addItemsToInventory(ServerPlayer player, ItemStack itemStack) {
        int count = itemStack.getCount();
        int maxCount = itemStack.getMaxStackSize();
        int spaceSlot = player.getInventory().getSlotWithRemainingSpace(itemStack);
        while (spaceSlot >= 0 && count > 0) {
            spaceSlot = player.getInventory().getSlotWithRemainingSpace(itemStack);
            if (spaceSlot < 0) {
                break;
            }
            int spaceCount = player.getInventory().getItem(spaceSlot).getCount();
            int interimCount = Math.min(maxCount - spaceCount, count);
            ItemStack interimItemStack = itemStack.copy(); 
            interimItemStack.setCount(interimCount);
            player.getInventory().add(spaceSlot, interimItemStack);
            count = count - interimCount;
        }
        int freeSlot = player.getInventory().getFreeSlot();
        while (freeSlot >= 0 && count > 0) {
            freeSlot = player.getInventory().getFreeSlot();
            if (freeSlot < 0) {
                break;
            }
            int interimCount = Math.min(maxCount, count);
            ItemStack interimItemStack = itemStack.copy(); 
            interimItemStack.setCount(interimCount);
            player.getInventory().add(freeSlot, interimItemStack);
            count = count - interimCount;
        }
        if (count > 0) {
            ItemStack interimItemStack = itemStack.copy(); 
            interimItemStack.setCount(count);
            player.drop(interimItemStack, true);
            Component message = Component.literal("Inventory Full! Dropped " + count + " " + interimItemStack.getItem().getName(interimItemStack).getString() + "!");
            player.sendSystemMessage(message);
        }
    }
    
    public static void givePokemon(ServerPlayer player, String species, boolean perfect, boolean shiny) {
        Random random = new Random();
        PokemonBuilder pokemonBuilder = PokemonBuilder.builder();
        List<Species> legendaryMythicalUltraBeastList = new ArrayList<>();
        List<Species> normalRandomList = new ArrayList<>();
        for (Species pokedexSpecies : PixelmonSpecies.getAll()) {
            if (pokedexSpecies != null && (pokedexSpecies.isLegendary() || pokedexSpecies.isMythical() || pokedexSpecies.isUltraBeast())) {
                legendaryMythicalUltraBeastList.add(pokedexSpecies);
            } else if (pokedexSpecies != null) {
                normalRandomList.add(pokedexSpecies);
            }
        }
        if (species.equals("randomLegendaryMythicalUltraBeast")) {
            pokemonBuilder = pokemonBuilder.species(legendaryMythicalUltraBeastList.get(random.nextInt(legendaryMythicalUltraBeastList.size())));
        } else if (!species.equals("random")) {
            pokemonBuilder = pokemonBuilder.species(species);
        } else {
            pokemonBuilder = pokemonBuilder.species(normalRandomList.get(random.nextInt(normalRandomList.size())));
        }
        if (pokemonBuilder.getSpecies().getTranslatedName().getString().equals("MissingNo")) {
            System.out.println("MissingNo caught and set to random");
            pokemonBuilder = pokemonBuilder.species(normalRandomList.get(random.nextInt(normalRandomList.size())));
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
        PlayerPartyStorage party = StorageProxy.getParty(player).join();
        Component message = Component.literal("");
        if (party != null) {
            party.add(pokemon);
            if (pokemon.isShiny()) {
                message = Component.literal("You received a shiny " + pokemon.getSpecies().getTranslatedName().getString() + "!");
            } else {
                message = Component.literal("You received a " + pokemon.getSpecies().getTranslatedName().getString() + "!");
            }
            player.sendSystemMessage(message);
        } else {
            System.err.println("Error giving " + pokemon.getSpecies().getTranslatedName().getString() + " to " + player.getDisplayName().getString());
        }
    }

    public static JsonObject getRewardData(int pokedexCompletion) {
        JsonObject rewardConfigData = PixelmonPokedexRewards.getRewardConfig();
        return rewardConfigData.has(String.valueOf(pokedexCompletion)) ? rewardConfigData.getAsJsonObject(String.valueOf(pokedexCompletion)) : null;
    }
}