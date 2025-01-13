package ru.jadegg2568.jadeservermessenger.messagesocket;

import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.jadeservermessenger.ServerMessengerAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class MessageClient extends BukkitRunnable implements IMessageSocket {

    private final ServerMessengerAPI main;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private MessageListener listener;

    public MessageClient(ServerMessengerAPI main, int port) {
        this.main = main;
        this.port = port;
    }

    @Override
    public void start() {
        if (socket != null) {
            return;
        }
        try {
            socket = new Socket("localhost", port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            runTaskAsynchronously(main);
            main.getLogger().info("MessageClient has been connected to port " + socket.getPort());
        } catch (IOException e) {
            throw new RuntimeException("MessageClient couldn't start and has been finished by an error", e);
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while (!isCancelled() && (line = reader.readLine()) != null) {
                main.getLogger().info("Server > ME >>> " + line);
                main.sendRequestToListeners(line);
            }
            close();
        } catch (SocketException e) {
            main.getLogger().info("MessageClient has been finished");
        } catch (IOException e) {
            throw new RuntimeException("MessageClient has been finished by an error", e);
        }
        cancel();
    }

    @Override
    public void write(String line) {
        main.getLogger().info("ME > Server >>> " + line);
        writer.println(line);
    }

    public void close() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        super.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        main.getLogger().info("MessageClient has been closed");
    }

    public MessageListener getListener() {
        return listener;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
}
