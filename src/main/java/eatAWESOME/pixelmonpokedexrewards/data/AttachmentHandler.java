package eatAWESOME.pixelmonpokedexrewards.data;

import eatAWESOME.pixelmonpokedexrewards.PixelmonPokedexRewards;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class AttachmentHandler {
    private AttachmentHandler() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    PixelmonPokedexRewards.MOD_ID
            );

    public static final Supplier<AttachmentType<PokedexDataAttachment>> POKEDEX_DATA =
            ATTACHMENT_TYPES.register(
                    "pokedex_data",
                    () -> AttachmentType.serializable(PokedexDataAttachment::new)
                            .copyOnDeath()
                            .build()
            );
}