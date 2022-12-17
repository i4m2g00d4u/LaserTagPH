package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.map.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ForcemapCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.forcemap")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        if (args.length != 1) {
            p.sendMessage(Main.prefix+"Du musst eine Map angeben§8:");
            Main.plugin.getMapManager().getMaps().forEach(maps -> {
                BaseComponent base = new TextComponent("§8» ");
                TextComponent component = new TextComponent("§e"+maps.getName());
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/forcemap "+maps.getName()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klicke, um auf der Map §e"+ maps.getName()+"§7 zu spielen.")));
                base.addExtra(component);
                p.spigot().sendMessage(base);
            });
            return false;
        }

        if (Main.plugin.getMapManager().getCurrentMap() != null) {
            p.sendMessage(Main.prefix+"Es wurde bereits eine Map ausgewählt.");
            return false;
        }

        if (Main.plugin.getLaserTag().game_gamestate != LaserTag.GAME.WAITING || Main.plugin.getLaserTag().countINT <= 10) {
            p.sendMessage(Main.prefix+"Das Spiel ist bereits gestartet.");
            return false;
        }

        for (Map map: Main.plugin.getMapManager().getMaps()) {
            if (!args[0].equalsIgnoreCase(map.getName())) continue;
            Main.plugin.getMapManager().forceMap(map.getName());
            p.sendMessage(Main.prefix+"Du hast die Map §e"+map.getName()+"§7 erzwungen!");
            return false;
        }
        p.sendMessage(Main.prefix+"Diese Map gibt es nicht§8:");
        Main.plugin.getMapManager().getMaps().forEach(maps -> {
            BaseComponent base = new TextComponent("§8» §e");
            TextComponent component = new TextComponent(maps.getName());
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/forcemap "+maps.getName()));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klicke, um auf der Map §e"+ maps.getName()+"§7 zu spielen.")));
            base.addExtra(component);
            p.spigot().sendMessage(base);
        });

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        for (Map map: Main.plugin.getMapManager().getMaps()) {
            if (args.length != 1) continue;
            list.add(map.getName());
        }
        return list;
    }
}
