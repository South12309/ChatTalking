package ru.gb.gbchat.server;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

public interface AuthService extends Closeable {

    void setNewNick(String oldNick, String newNick) throws SQLException;
    String getNickByLoginAndPassword(String login, String password);

    void run();

    @Override
    void close() throws IOException;
}