package ru.jadegg2568.jadeservermessenger;

public class Configuration {

    private final boolean server;
    private final int port;

    public Configuration(boolean server, int port) {
        this.server = server;
        this.port = port;
    }

    public boolean isServer() {
        return server;
    }

    public int getPort() {
        return port;
    }
}
