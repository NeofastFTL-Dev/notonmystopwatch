package com.neofastftl.nomsw.client;

import com.neofastftl.nomsw.Nomsw;
import com.neofastftl.nomsw.timing.BootTimer;
import com.neofastftl.nomsw.timing.TimingLog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Early client lifecycle: init log path and ensure the boot stopwatch is running.
 */
@Mod.EventBusSubscriber(modid = Nomsw.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModSetup {

    private ClientModSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            TimingLog.init(mc.gameDirectory.toPath());
            BootTimer.markStart("client");
            TimingLog.note("BOOT", "FMLClientSetup reached (boot still in progress until title screen)");
        });
    }
}
