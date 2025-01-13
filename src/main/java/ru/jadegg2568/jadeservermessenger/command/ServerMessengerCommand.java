package ru.jadegg2568.jadeservermessenger.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.jadegg2568.jadeservermessenger.ServerMessengerAPI;
import ru.jadegg2568.jadeservermessenger.messagesocket.MessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerMessengerCommand implements CommandExecutor {

    private final ServerMessengerAPI main;
    private final List<Player> playersListeners;

    public ServerMessengerCommand(ServerMessengerAPI main) {
        this.main = main;
        this.playersListeners = new ArrayList<>();

        main.registerListener(request -> sendMessageForPlayersListeners(ChatColor.AQUA + "[СООБЩЕНИЕ] " + request));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("servermessenger.commands.servermessenger")) {
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        if (args[0].equals("send")) {
            send(sender, args);
        } else if (args[0].equals("chat")) {
            chat(sender, args);
        } else {
            help(sender);
        }
        return true;
    }

    public void help(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "/servermessenger send <сообщение> - отправить сообщение.");
        sender.sendMessage(ChatColor.AQUA + "/servermessenger chat - включение/выключение отправки в чата вам, если вы игрок.");
    }

    public void send(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.AQUA + "Пожалуйста, укажите сообщение.");
            return;
        }
        List<String> messageParts = Lists.newArrayList(args).subList(1, args.length);
        String message = String.join(" ", messageParts);

        sendMessageForPlayersListeners(ChatColor.AQUA + "[ОТПРАВЛЕНИЕ] " + message);
        main.getMessageSocket().write(message);
    }

    public void chat(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You're not a player.");
            return;
        }
        Player p = (Player) sender;
        if (playersListeners.contains(p)) {
            playersListeners.remove(p);
            p.sendMessage(ChatColor.AQUA + "Слушание мессенджера в чате успешно " + ChatColor.RED + "выключено" + ChatColor.AQUA + ".");
        } else {
            playersListeners.add(p);
            p.sendMessage(ChatColor.AQUA + "Слушание мессенджера в чате успешно " + ChatColor.GREEN + "включено" + ChatColor.AQUA + ".");
        }
    }

    public void sendMessageForPlayersListeners(String message) {
        for (Player p : playersListeners) {
            if (p.isOnline()) {
                playersListeners.remove(p);
                continue;
            }
            p.sendMessage(message);
        }
    }
}
