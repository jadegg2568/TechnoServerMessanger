package ru.jadegg2568.jadeservermessenger.messagesocket;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.jadeservermessenger.ServerMessengerAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class MessageClientHandler extends BukkitRunnable {

    private final ServerMessengerAPI main;
    private final MessageServer messageServer;
    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public MessageClientHandler(MessageServer messageServer, Socket socket) {
        this.main = messageServer.getMain();
        this.messageServer = messageServer;
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        main.getLogger().info("Client[port=" + socket.getPort() + "] has been connected! Total clients: " + messageServer.getConnectedSockets().size());
        super.runTaskAsynchronously(main);
    }

    public void write(String line) {
        main.getLogger().info("ME > Client[port=" + socket.getPort() + "] >>> " + line);
        writer.println(line);
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = reader.readLine()) != null) {
                main.getLogger().info("Client[port=" + socket.getPort() + "] > ME >>> " + line);
                main.sendRequestToListeners(line);
                messageServer.writeAllExcept(this, line);
            }
            close();
        } catch (SocketException e) {
            Bukkit.getLogger().info("Client[port=" + socket.getPort() + "]" + " has been finished");
        } catch (IOException e) {
            throw new RuntimeException("MessageClientHandler has been finished by an error", e);
        }
    }

    public void close() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try {
            super.cancel();
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        main.getLogger().info("Client[port=" + socket.getPort() + "] has been disconnected! Total clients: " + messageServer.getConnectedSockets().size());
    }

    public int getPort() {
        return socket.getPort();
    }
}
