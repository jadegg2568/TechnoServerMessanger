package ru.jadegg2568.jadeservermessenger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.jadegg2568.jadeservermessenger.command.ServerMessengerCommand;
import ru.jadegg2568.jadeservermessenger.messagesocket.IMessageSocket;
import ru.jadegg2568.jadeservermessenger.messagesocket.MessageListener;
import ru.jadegg2568.jadeservermessenger.messagesocket.MessageClient;
import ru.jadegg2568.jadeservermessenger.messagesocket.MessageServer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class ServerMessengerAPI extends JavaPlugin {

    private static ServerMessengerAPI instance;
    private Configuration configuration;
    private IMessageSocket messageSocket;
    private final List<MessageListener> listeners = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        setupConfiguration();
        setupCommands();
        setupMechanics();
    }

    public void setupConfiguration() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        boolean server = config.getBoolean("server", false);
        int port = config.getInt("port", 8081);

        configuration = new Configuration(server, port);

        getLogger().info("----------------");
        getLogger().info("Configuration:");
        getLogger().info("server: " + server);
        getLogger().info("port: " + port);
        getLogger().info("----------------");
    }

    public void setupCommands() {
        getCommand("servermessenger").setExecutor(new ServerMessengerCommand(this));
    }

    public void setupMechanics() {
        int port = configuration.getPort();
        messageSocket = (configuration.isServer() ? new MessageServer(this, port)
                : new MessageClient(this, port));

        Bukkit.getScheduler().runTaskLater(this, () -> messageSocket.start(), 20L * 5);

    }

    @Override
    public void onDisable() {
        if (messageSocket != null) {
            messageSocket.close();
        }

        instance = null;
    }

    public IMessageSocket getMessageSocket() {
        return messageSocket;
    }

    public void sendRequestToListeners(String request) {
        for (MessageListener listener : listeners) {
            getLogger().fine("Calling " + listener.getClass().getName() + " with a request \"" + request + "\"...");
            listener.onRequest(request);
        }
    }

    public void registerListener(MessageListener listener) {
        getLogger().fine(listener.getClass().getName() + " has been registered as a message listener.");
        listeners.add(listener);
    }

    public static ServerMessengerAPI getInstance() {
        return instance;
    }
}
