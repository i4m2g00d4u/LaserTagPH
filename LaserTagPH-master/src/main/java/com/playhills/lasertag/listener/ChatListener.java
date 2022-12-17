package com.playhills.lasertag.listener;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final LaserTag sys = Main.plugin.getLaserTag();

    @EventHandler
    void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        LaserTag.ROLE team = sys.roles.get(p.getName());
        if (sys.game_gamestate == LaserTag.GAME.RUNNING || sys.game_gamestate == LaserTag.GAME.DEATHMATCH) {
            if (msg.toLowerCase().startsWith("@a ") || msg.toLowerCase().startsWith("@all ") ) {
                String[] gmsg = msg.split(" ");
                msg = "";
                for (int i = 1; i < gmsg.length; i++) {
                    msg += gmsg[i] + " ";
                }
                if (msg.equals("")) return;
                e.setCancelled(true);
                BaseComponent b = new TextComponent("");
                TextComponent prefix = new TextComponent();
                TextComponent txt = new TextComponent();
                switch (team) {
                    case YELLOW:
                        prefix.setText("§8[§3@all§8] ");
                        txt.setText("§8[§eGelb§8] §e" + p.getName() + " §8» §f" + msg);
                        break;
                    case PURPLE:
                        prefix.setText("§8[§3@all§8] ");
                        txt.setText("§8[§5Lila§8] §5" + p.getName() + " §8» §f" + msg);
                        break;
                    case WAITING:
                        e.setCancelled(true);
                        txt.setText("§8[§7Zuschauer§8] §7" + p.getName() + " §8» §f" + msg);
                        b.addExtra(txt);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            if (sys.roles.get(all.getName()) != LaserTag.ROLE.WAITING) continue;
                            all.spigot().sendMessage(b);
                        }
                        return;
                }
                b.addExtra(prefix);
                b.addExtra(txt);
                for (Player all: Bukkit.getOnlinePlayers()) {
                    all.spigot().sendMessage(b);
                }
            } else {
                e.setCancelled(true);
                BaseComponent b = new TextComponent("");
                TextComponent prefix = new TextComponent();
                TextComponent txt = new TextComponent();
                switch (team) {
                    case YELLOW:
                        e.setCancelled(true);
                        prefix.setText("§8[§eTEAM§8] ");
                        txt.setText("§8[§eGelb§8] §e" + p.getName() + " §8» §f" + msg);
                        b.addExtra(prefix);
                        b.addExtra(txt);

                        for (Player all : Bukkit.getOnlinePlayers()) {
                            if (!sys.roles.get(all.getName()).equals(LaserTag.ROLE.YELLOW)) continue;
                            all.spigot().sendMessage(b);
                        }
                        return;
                    case PURPLE:
                        e.setCancelled(true);
                        prefix.setText("§8[§eTEAM§8] ");
                        txt.setText("§8[§5Lila§8] §5" + p.getName() + " §8» §f" + msg);
                        b.addExtra(prefix);
                        b.addExtra(txt);

                        for (Player all : Bukkit.getOnlinePlayers()) {
                            if (!sys.roles.get(all.getName()).equals(LaserTag.ROLE.PURPLE)) continue;
                            all.spigot().sendMessage(b);
                        }
                        return;
                    case WAITING:
                        e.setCancelled(true);
                        txt.setText("§8[§7Zuschauer§8] §7" + p.getName() +  " §8» §f" + msg);

                        b.addExtra(txt);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            if (!sys.roles.get(all.getName()).equals(LaserTag.ROLE.WAITING)) continue;
                            all.spigot().sendMessage(b);
                        }
                        return;
                }

            }
        } else {
            e.setCancelled(true);
            BaseComponent b = new TextComponent("");
            TextComponent txt = new TextComponent();
            switch (team) {
                case PURPLE:
                    txt.setText("§8[§5Lila§8] §5" + p.getName() + " §8» §f" + msg);
                    break;
                case YELLOW:
                    txt.setText("§8[§eGelb§8] §e" + p.getName() + " §8» §f" + msg);
                    break;
                case WAITING:
                    txt.setText("§8[§7Wartend§8] §7" + p.getName() +  " §8» §f" + msg);
                    break;
            }

            b.addExtra(txt);

            for (Player all: Bukkit.getOnlinePlayers()) {
                all.spigot().sendMessage(b);
            }
        }
        System.out.println(p.getName() + " > " + msg);
    }
}
