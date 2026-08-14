package com.neofastftl.nomsw;

import com.neofastftl.nomsw.sound.ModSounds;
import com.neofastftl.nomsw.timing.BootTimer;
import com.neofastftl.nomsw.timing.TimingLog;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * NotOnMyStopWatch — measures game boot time and world-load time,
 * logging results to the console and to {@code logs/notonmystopwatch/timings.log}.
 */
@Mod(Nomsw.MODID)
public class Nomsw {

    public static final String MODID = "nomsw";

    public Nomsw() {
        // Earliest point in our mod: start boot stopwatch from JVM process start.
        String side = FMLEnvironment.dist.isClient() ? "client" : "dedicated_server";
        TimingLog.init(FMLPaths.GAMEDIR.get());
        BootTimer.markStart(side);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        TimingLog.note("BOOT", "FMLCommonSetup reached");
    }

    /**
     * On a dedicated server there is no title screen, so treat
     * {@link ServerStartedEvent} as "boot complete". Integrated servers are
     * covered by the client title-screen / world-load path instead.
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (FMLEnvironment.dist.isClient()) {
            // Integrated server: boot was already (or will be) finished via TitleScreen;
            // world-load timing is handled on the client.
            return;
        }
        if (!Config.measureBoot || BootTimer.isFinished()) {
            return;
        }
        String duration = BootTimer.markFinished("ServerStartedEvent");
        if (duration != null) {
            TimingLog.info("Dedicated server boot finished in " + duration);
        }
    }
}
