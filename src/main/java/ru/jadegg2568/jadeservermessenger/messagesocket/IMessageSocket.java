package ru.jadegg2568.jadeservermessenger.messagesocket;

public interface IMessageSocket {

    void start();

    void write(String line);

    void close();
}
