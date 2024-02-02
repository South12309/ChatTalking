package ru.gb.gbchat.server;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.sql.*;


public class SQLiteAuthService implements AuthService {
    private static Connection connection;
    private static final Logger logger=LogManager.getLogger(SQLiteAuthService.class);

    public SQLiteAuthService() {
        run();
    }

    @Override
    public void setNewNick(String oldNick, String newNick) throws SQLException {

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?;");
            preparedStatement.setString(1, newNick);
            preparedStatement.setString(2, oldNick);
            preparedStatement.executeUpdate();
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Nick from users WHERE login =? AND password=?;");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String nick = resultSet.getString(1);
            return nick;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:javadb.db");
            logger.debug("Подключение к базе выполнено.");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка при подключении к базе.");
        }

    }

    @Override
    public void close() throws IOException {

        try {
            if (connection != null)
                connection.close();
            logger.debug("Отключение от базы выполнено.");
        } catch (SQLException e) {
            logger.error("Ошибка при отключении от базе.");
            e.printStackTrace();
        }
    }

}

