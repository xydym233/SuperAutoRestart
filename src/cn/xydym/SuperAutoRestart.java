package cn.xydym;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperAutoRestart extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        logInfo("自动重启功能已开启");
        ensureConfigExists();
        scheduleShutdown();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        logInfo("插件完成卸载");
    }

    private void ensureConfigExists() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        reloadConfig();
    }

    private void scheduleShutdown() {
        logInfo("服务器将在配置的时间内完成关服");
        int shutdownHour = getConfig().getInt("shutdownHour", 3);
        int shutdownMinutes = getConfig().getInt("shutdownMinutes", 30);

        Calendar now = Calendar.getInstance();
        Calendar shutdownTime = Calendar.getInstance();
        shutdownTime.set(Calendar.HOUR_OF_DAY, shutdownHour);
        shutdownTime.set(Calendar.MINUTE, shutdownMinutes);
        shutdownTime.set(Calendar.SECOND, 0);

        if (now.after(shutdownTime)) {
            shutdownTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        long delay = shutdownTime.getTimeInMillis() - now.getTimeInMillis();
        long delayTicks = TimeUnit.MILLISECONDS.toSeconds(delay) * 20L;

        Bukkit.getScheduler().runTaskLater(this, this::performShutdown, delayTicks);
    }

    private void performShutdown() {
        logInfo("当前关服为自动关服");

        // 踢出所有在线玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§c●§e服务器正在重启,请尝试稍后进入§c●");
        }

        // 执行 stop 命令
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("stop")) {
            logInfo("检测到手动的 stop 指令运行，踢出所有玩家");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("§c●§e服务器正在重启,请尝试稍后进入§c●");
            }
        }
    }

    private void logInfo(String message) {
        getLogger().info("§9SuperAutoRestart §d>> " + message);
    }
}