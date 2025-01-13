package ru.jadegg2568.jadeservermessenger.messagesocket;

import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.jadeservermessenger.ServerMessengerAPI;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

public class MessageServer extends BukkitRunnable implements IMessageSocket {

    private final ServerMessengerAPI main;
    private final int port;
    private ServerSocket serverSocket;
    private final LinkedList<MessageClientHandler> connectedSockets = new LinkedList<>();

    public MessageServer(ServerMessengerAPI main, int port) {
        this.main = main;
        this.port = port;
    }

    @Override
    public void start() {
        if (serverSocket != null) {
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            runTaskAsynchronously(main);
            main.getLogger().info("MessageServer has been started");
        } catch (BindException e) {
            throw new RuntimeException("MessageServer couldn't start because the port is already in use", e);
        } catch (IOException e) {
            throw new RuntimeException("MessageServer couldn't start and has been finished by an error", e);
        }
    }

    @Override
    public void run() {
        try {
            while (!isCancelled()) {
                Socket socket = serverSocket.accept();
                MessageClientHandler handler = new MessageClientHandler(this, socket);
                connectedSockets.add(handler);
                handler.start();
            }
        } catch (SocketException e) {
            main.getLogger().info("MessageServer has been finished");
        } catch (IOException e) {
            throw new RuntimeException("MessageServer has been finished by an error", e);
        }
    }

    @Override
    public void write(String line) {
        for (MessageClientHandler socket1 : connectedSockets) {
            socket1.write(line);
        }
    }

    public void writeAllExcept(MessageClientHandler socket, String line) {
        for (MessageClientHandler socket1 : connectedSockets) {
            if (socket1.getPort() == socket.getPort()) continue;
            socket1.write(line);
        }
    }

    public void close() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        super.cancel();
        try {
            for (MessageClientHandler connectedSocket : connectedSockets) {
                connectedSocket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        main.getLogger().info("MessageServer has been closed");
    }

    public LinkedList<MessageClientHandler> getConnectedSockets() {
        return connectedSockets;
    }

    public ServerMessengerAPI getMain() {
        return main;
    }
}
