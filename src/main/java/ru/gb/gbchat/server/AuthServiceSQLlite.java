package ru.gb.gbchat.server;

import java.io.IOException;


import java.sql.*;


public class AuthServiceSQLlite implements AuthService {
    private static Connection connection;

    public AuthServiceSQLlite() {
        run();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

