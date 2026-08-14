package com.neofastftl.nomsw;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Nomsw.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue MEASURE_BOOT = BUILDER
            .comment("Measure time from JVM process start until the game is fully booted (title screen / server started).")
            .define("measureBoot", true);

    private static final ForgeConfigSpec.BooleanValue MEASURE_WORLD_LOAD = BUILDER
            .comment("Measure time from opening/joining a world until you can fully interact with it.")
            .define("measureWorldLoad", true);

    private static final ForgeConfigSpec.BooleanValue WRITE_LOG_FILE = BUILDER
            .comment("Append timing results to logs/notonmystopwatch/timings.log")
            .define("writeLogFile", true);

    private static final ForgeConfigSpec.BooleanValue CHAT_ANNOUNCE = BUILDER
            .comment("Show a client chat message when world load timing completes.")
            .define("chatAnnounce", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_BOOT_TIME_ON_TITLE = BUILDER
            .comment("Show boot load time as text underneath the main menu on the title screen.")
            .define("showBootTimeOnTitle", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_LAST_WORLD_LOAD_ON_TITLE = BUILDER
            .comment("Also show the last world-load time on the title screen (under the boot time), if available.")
            .define("showLastWorldLoadOnTitle", true);

    // --- Sounds ---
    static {
        BUILDER.push("sounds");
    }

    private static final ForgeConfigSpec.BooleanValue PLAY_WORLD_LOAD_SOUND = BUILDER
            .comment("Play a sound when the world finishes loading and you can fully interact.")
            .define("playWorldLoadSound", true);

    private static final ForgeConfigSpec.ConfigValue<String> WORLD_LOAD_SOUND_ID = BUILDER
            .comment(
                    "Sound event id to play on world load complete.",
                    "Default is the mod sound nomsw:world_load_complete (aliases ui.toast.challenge_complete).",
                    "Any registered sound works, e.g. minecraft:entity.player.levelup or minecraft:block.note_block.chime."
            )
            .define("worldLoadSoundId", "nomsw:world_load_complete");

    private static final ForgeConfigSpec.DoubleValue WORLD_LOAD_SOUND_VOLUME = BUILDER
            .comment("Volume for the world-load complete sound (0.0 – 1.0).")
            .defineInRange("worldLoadSoundVolume", 1.0D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue WORLD_LOAD_SOUND_PITCH = BUILDER
            .comment("Pitch for the world-load complete sound (0.5 – 2.0).")
            .defineInRange("worldLoadSoundPitch", 1.0D, 0.5D, 2.0D);

    private static final ForgeConfigSpec.BooleanValue PLAY_BOOT_SOUND = BUILDER
            .comment("Play a sound when game boot finishes (title screen on client).")
            .define("playBootSound", false);

    private static final ForgeConfigSpec.ConfigValue<String> BOOT_SOUND_ID = BUILDER
            .comment(
                    "Sound event id to play on boot complete.",
                    "Default is nomsw:boot_complete (aliases entity.experience_orb.pickup)."
            )
            .define("bootSoundId", "nomsw:boot_complete");

    private static final ForgeConfigSpec.DoubleValue BOOT_SOUND_VOLUME = BUILDER
            .comment("Volume for the boot complete sound (0.0 – 1.0).")
            .defineInRange("bootSoundVolume", 0.8D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue BOOT_SOUND_PITCH = BUILDER
            .comment("Pitch for the boot complete sound (0.5 – 2.0).")
            .defineInRange("bootSoundPitch", 1.0D, 0.5D, 2.0D);

    static {
        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean measureBoot = true;
    public static boolean measureWorldLoad = true;
    public static boolean writeLogFile = true;
    public static boolean chatAnnounce = true;
    public static boolean showBootTimeOnTitle = true;
    public static boolean showLastWorldLoadOnTitle = true;

    public static boolean playWorldLoadSound = true;
    public static String worldLoadSoundId = "nomsw:world_load_complete";
    public static double worldLoadSoundVolume = 1.0D;
    public static double worldLoadSoundPitch = 1.0D;

    public static boolean playBootSound = false;
    public static String bootSoundId = "nomsw:boot_complete";
    public static double bootSoundVolume = 0.8D;
    public static double bootSoundPitch = 1.0D;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        measureBoot = MEASURE_BOOT.get();
        measureWorldLoad = MEASURE_WORLD_LOAD.get();
        writeLogFile = WRITE_LOG_FILE.get();
        chatAnnounce = CHAT_ANNOUNCE.get();
        showBootTimeOnTitle = SHOW_BOOT_TIME_ON_TITLE.get();
        showLastWorldLoadOnTitle = SHOW_LAST_WORLD_LOAD_ON_TITLE.get();

        playWorldLoadSound = PLAY_WORLD_LOAD_SOUND.get();
        worldLoadSoundId = WORLD_LOAD_SOUND_ID.get();
        worldLoadSoundVolume = WORLD_LOAD_SOUND_VOLUME.get();
        worldLoadSoundPitch = WORLD_LOAD_SOUND_PITCH.get();

        playBootSound = PLAY_BOOT_SOUND.get();
        bootSoundId = BOOT_SOUND_ID.get();
        bootSoundVolume = BOOT_SOUND_VOLUME.get();
        bootSoundPitch = BOOT_SOUND_PITCH.get();
    }
}
