package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.api.events.BossBarEvents;
import cc.reconnected.essentials.api.events.RccEvents;
import cc.reconnected.essentials.api.events.RestartEvents;
import cc.reconnected.essentials.util.Components;
import cc.reconnected.library.event.ReadyEvent;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoRestart {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static BossBarManager.TimeBar restartBar = null;
    private static Key notificationKey;
    private static ScheduledFuture<?> currentSchedule = null;


    public static void register() {

        var miniMessage = MiniMessage.miniMessage();

        ReadyEvent.EVENT.register((server, lib) -> {
            if (RccEssentials.CONFIG.autoRestart.enableAutoRestart) {
                scheduleNextRestart();
            }
        });

        BossBarEvents.PROGRESS.register((timeBar, server) -> {
            if (restartBar == null || !timeBar.getUuid().equals(restartBar.getUuid()))
                return;

            var notificationTimes = RccEssentials.CONFIG.autoRestart.restartNotifications;

            var remainingSeconds = restartBar.getRemainingSeconds();
            if (notificationTimes.contains(remainingSeconds)) {
                notifyRestart(server, restartBar);
            }

        });

        // Shutdown
        BossBarEvents.END.register((timeBar, server) -> {
            if (restartBar == null || !timeBar.getUuid().equals(restartBar.getUuid()))
                return;

            final var text = Components.toText(
                    miniMessage.deserialize(RccEssentials.CONFIG.autoRestart.restartKickMessage)
            );
            server.getPlayerManager().getPlayerList().forEach(player -> {
                player.networkHandler.disconnect(text);
            });
            scheduler.shutdownNow();
            server.stop(false);
        });

        setup();

        RccEvents.RELOAD.register(instance -> {
            setup();
        });
    }

    private static void setup() {
        var soundName = RccEssentials.CONFIG.autoRestart.restartSound;
        try {
            notificationKey = Key.key(soundName);
        } catch (InvalidKeyException e) {
            RccEssentials.LOGGER.error("Invalid restart notification sound name", e);
            notificationKey = Key.key("minecraft", "block.note_block.bell");
        }
    }

    public static void schedule(int seconds, String message) {
        restartBar = BossBarManager.getInstance().startTimeBar(
                message,
                seconds,
                BossBar.Color.RED,
                BossBar.Style.NOTCHED_20,
                true
        );

        RestartEvents.SCHEDULED.invoker().onSchedule(restartBar);
    }

    public static boolean isScheduled() {
        return restartBar != null || currentSchedule != null && !currentSchedule.isCancelled();
    }

    public static void cancel() {
        if (restartBar != null) {
            BossBarManager.getInstance().cancelTimeBar(restartBar);
            RestartEvents.CANCELED.invoker().onCancel(restartBar);
            restartBar = null;
        }

        if(currentSchedule != null) {
            currentSchedule.cancel(false);
            currentSchedule = null;
        }
    }

    private static void notifyRestart(MinecraftServer server, BossBarManager.TimeBar bar) {
        var rcc = RccEssentials.getInstance();
        var audience = rcc.adventure().players();
        var sound = Sound.sound(notificationKey, Sound.Source.MASTER, 10f, RccEssentials.CONFIG.autoRestart.restartSoundPitch);
        audience.playSound(sound, Sound.Emitter.self());

        var comp = bar.parseLabel(RccEssentials.CONFIG.autoRestart.restartChatMessage);
        rcc.broadcastComponent(server, comp);
    }

    @Nullable
    public static Long scheduleNextRestart() {
        var delay = getNextDelay();
        if (delay == null)
            return null;

        var barTime = 10 * 60;
        // start bar 10 mins earlier
        var barStartTime = delay - barTime;

        currentSchedule = scheduler.schedule(() -> {
            schedule(barTime, RccEssentials.CONFIG.autoRestart.restartBarLabel);
        }, barStartTime, TimeUnit.SECONDS);

        RccEssentials.LOGGER.info("Restart scheduled for in {} seconds", delay);
        return delay;
    }

    @Nullable
    private static Long getNextDelay() {
        var restartTimeStrings = RccEssentials.CONFIG.autoRestart.restartAt;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRunTime = null;
        long shortestDelay = Long.MAX_VALUE;

        for (var timeString : restartTimeStrings) {
            LocalTime targetTime = LocalTime.parse(timeString);
            LocalDateTime targetDateTime = now.with(targetTime);

            if (targetDateTime.isBefore(now)) {
                targetDateTime = targetDateTime.plusDays(1);
            }

            long delay = Duration.between(now, targetDateTime).toSeconds();
            if (delay < shortestDelay) {
                shortestDelay = delay;
                nextRunTime = targetDateTime;
            }
        }

        if (nextRunTime != null) {
            return shortestDelay;
        }
        return null;
    }
}