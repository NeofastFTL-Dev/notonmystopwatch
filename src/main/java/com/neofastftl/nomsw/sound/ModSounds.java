package com.neofastftl.nomsw.sound;

import com.neofastftl.nomsw.Nomsw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom sound events shipped by this mod. Defaults are defined in
 * {@code assets/nomsw/sounds.json}; the active sound can be overridden in config.
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Nomsw.MODID);

    /** Played when a world finishes loading and becomes interactive. */
    public static final RegistryObject<SoundEvent> WORLD_LOAD_COMPLETE =
            register("world_load_complete");

    /** Played when game boot finishes (title screen / dedicated server ready). */
    public static final RegistryObject<SoundEvent> BOOT_COMPLETE =
            register("boot_complete");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Nomsw.MODID, name)));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
