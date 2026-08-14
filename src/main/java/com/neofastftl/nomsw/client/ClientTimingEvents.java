package com.neofastftl.nomsw.client;

import com.neofastftl.nomsw.Config;
import com.neofastftl.nomsw.Nomsw;
import com.neofastftl.nomsw.timing.BootTimer;
import com.neofastftl.nomsw.timing.TimingLog;
import com.neofastftl.nomsw.timing.WorldLoadTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side hooks for boot completion (first title screen) and world-load
 * timing (loading screens → fully interactive).
 */
@Mod.EventBusSubscriber(modid = Nomsw.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientTimingEvents {

    private ClientTimingEvents() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen next = event.getNewScreen();
        if (next == null) {
            return;
        }

        // Boot complete: first time the title screen is shown.
        if (next instanceof TitleScreen && Config.measureBoot && !BootTimer.isFinished()) {
            String duration = BootTimer.markFinished("TitleScreen shown");
            if (duration != null) {
                TimingLog.info("Game boot finished in " + duration);
                LoadSoundPlayer.playBootComplete();
            }
        }

        // World load start: any of the screens used while opening a world / joining.
        if (isWorldLoadingScreen(next) && Config.measureWorldLoad) {
            String name = next.getClass().getSimpleName();
            String context = describeLoadContext(next, name);
            // Only start if we are not already timing the same continuous load.
            if (!WorldLoadTimer.isActive()) {
                WorldLoadTimer.begin(context);
            }
        }

        // User backed out to the title screen — cancel an unfinished world load.
        if (next instanceof TitleScreen && WorldLoadTimer.isActive()) {
            TimingLog.note("WORLD", "Load cancelled (returned to title screen)");
            WorldLoadTimer.cancel();
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!Config.measureWorldLoad) {
            return;
        }
        // Ensure a timer is running even if we somehow missed the loading screen.
        if (!WorldLoadTimer.isActive()) {
            String world = "unknown";
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSingleplayerServer() != null) {
                world = mc.getSingleplayerServer().getWorldData().getLevelName();
            } else if (mc.getCurrentServer() != null) {
                world = mc.getCurrentServer().name;
            }
            WorldLoadTimer.begin("join player=" + event.getPlayer().getGameProfile().getName() + " world=" + world);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (WorldLoadTimer.isActive()) {
            TimingLog.note("WORLD", "Load cancelled (logging out)");
            WorldLoadTimer.cancel();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!WorldLoadTimer.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean interactive = isFullyInteractive(mc);
        String duration = WorldLoadTimer.tick(interactive);
        if (duration != null) {
            announceWorldLoad(mc, duration);
        }
    }

    /**
     * Draws load-time text underneath the main menu buttons on the title screen.
     */
    @SubscribeEvent
    public static void onTitleScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        if (!Config.showBootTimeOnTitle && !Config.showLastWorldLoadOnTitle) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = screen.width / 2;

        // Sit just under the usual main-menu button column (buttons start ~height/4+48).
        // Vanilla copyright sits at height - 10; keep clear of that.
        int y = Math.min(screen.height / 4 + 48 + 24 * 5 + 12, screen.height - 50);

        if (Config.showBootTimeOnTitle) {
            String boot = BootTimer.getFormattedDuration();
            if (boot != null) {
                drawCenteredShadow(graphics, font, "Boot load: " + boot, centerX, y, 0xFFE0E0E0);
                y += 12;
            }
        }

        if (Config.showLastWorldLoadOnTitle) {
            String world = WorldLoadTimer.getLastFormattedDuration();
            if (world != null) {
                drawCenteredShadow(graphics, font, "Last world load: " + world, centerX, y, 0xFFA0E0A0);
            }
        }
    }

    private static void drawCenteredShadow(GuiGraphics graphics, Font font, String text, int centerX, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, centerX - width / 2, y, color, true);
    }

    private static boolean isWorldLoadingScreen(Screen screen) {
        return screen instanceof LevelLoadingScreen
                || screen instanceof ReceivingLevelScreen
                || screen instanceof ConnectScreen
                || screen instanceof ProgressScreen
                || screen instanceof GenericDirtMessageScreen;
    }

    private static String describeLoadContext(Screen screen, String className) {
        Minecraft mc = Minecraft.getInstance();
        if (screen instanceof LevelLoadingScreen) {
            String levelName = "singleplayer";
            if (mc.getSingleplayerServer() != null) {
                levelName = mc.getSingleplayerServer().getWorldData().getLevelName();
            }
            return "singleplayer load screen world=" + levelName + " screen=" + className;
        }
        if (screen instanceof ConnectScreen || screen instanceof ReceivingLevelScreen) {
            String server = mc.getCurrentServer() != null ? mc.getCurrentServer().name : "multiplayer";
            return "multiplayer join server=" + server + " screen=" + className;
        }
        return "load screen=" + className;
    }

    /**
     * True when the local player can actually play: in a world, no loading UI,
     * no resource-reload overlay, and the spawn chunk is available.
     */
    private static boolean isFullyInteractive(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return false;
        }
        // Still on a loading / connecting screen.
        if (mc.screen != null && isWorldLoadingScreen(mc.screen)) {
            return false;
        }
        // Resource reload / loading overlay still covering the game.
        if (mc.getOverlay() != null) {
            return false;
        }
        // Don't finish while a generic dirt "Loading..." style screen is up.
        if (mc.screen instanceof GenericDirtMessageScreen || mc.screen instanceof ProgressScreen) {
            return false;
        }
        // Chunk under the player should exist so movement/interaction works.
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        if (!mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) {
            return false;
        }
        // Connection must be alive.
        if (mc.getConnection() == null) {
            return false;
        }
        return true;
    }

    private static void announceWorldLoad(Minecraft mc, String duration) {
        TimingLog.info("World ready for interaction in " + duration + " (" + WorldLoadTimer.getContext() + ")");
        LoadSoundPlayer.playWorldLoadComplete();
        if (Config.chatAnnounce && mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§6[NOMSW] §fWorld load: §a" + duration),
                    false
            );
        }
    }
}
