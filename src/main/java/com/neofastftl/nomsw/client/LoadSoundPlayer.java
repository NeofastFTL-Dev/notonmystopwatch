package com.neofastftl.nomsw.client;

import com.neofastftl.nomsw.Config;
import com.neofastftl.nomsw.timing.TimingLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Resolves the configured sound id and plays it as a UI sound on the client.
 */
public final class LoadSoundPlayer {

    private LoadSoundPlayer() {
    }

    public static void playWorldLoadComplete() {
        if (!Config.playWorldLoadSound) {
            return;
        }
        play(Config.worldLoadSoundId, Config.worldLoadSoundVolume, Config.worldLoadSoundPitch, "WORLD");
    }

    public static void playBootComplete() {
        if (!Config.playBootSound) {
            return;
        }
        play(Config.bootSoundId, Config.bootSoundVolume, Config.bootSoundPitch, "BOOT");
    }

    private static void play(String soundId, double volume, double pitch, String category) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }

        ResourceLocation id;
        try {
            id = new ResourceLocation(soundId);
        } catch (Exception e) {
            TimingLog.warn(category + " sound id is invalid: " + soundId);
            return;
        }

        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (sound == null) {
            TimingLog.warn(category + " sound not found in registry: " + soundId);
            return;
        }

        float vol = (float) Math.max(0.0, Math.min(1.0, volume));
        float pit = (float) Math.max(0.5, Math.min(2.0, pitch));
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pit, vol));
        TimingLog.note(category, "Played sound " + soundId + " (volume=" + vol + ", pitch=" + pit + ")");
    }
}
