package ru.gb.gbchat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.gbchat.Command;

public class ClientHandler {
    private static final int TIMEOUT_AUTH = 120000;
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final AuthService authService;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private String nick;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            server.getExecutorService().execute(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            });

        } catch (IOException e) {
            logger.error("Произошла ошибка подключения клиента");
            throw new RuntimeException(e);

        }


    }

    private void closeConnection() {
        if (!nick.equals(""))
            sendMessage(Command.END);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        Thread timeToAuth = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_AUTH);
                closeConnection();
            } catch (InterruptedException e) {
            }
        });
        timeToAuth.setDaemon(true);
        timeToAuth.start();

        while (true) {
            try {
                final String str = in.readUTF();
                if (Command.isCommand(str)) {
                    final Command command;
                    try {
                        command = Command.getCommand(str);
                    } catch (RuntimeException e) {
                       sendMessage(e.getMessage());
                        continue;
                    }
                    final String[] params = command.parse(str);
                    if (command == Command.AUTH) {
                        final String login = params[0];
                        final String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast("Пользователь " + nick + " зашел в чат");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage(Command.ERROR, "Неверные логин и пароль");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Подключение потеряно для авторизации");
                break;
            }

        }

        timeToAuth.interrupt();
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    public void sendMessage(String message) {
        try {
            System.out.println("SERVER: Send message to " + nick);
            out.writeUTF(message);
            logger.info("Клиенту " + nick + "отправлено: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String msg = in.readUTF();
                System.out.println("Receive message: " + msg);
                if (Command.isCommand(msg)) {
                    final Command command;
                    try {
                        command = Command.getCommand(msg);
                    } catch (RuntimeException e) {
                        sendMessage(e.getMessage());
                        continue;
                    }
                    final String[] params = command.parse(msg);
                    if (command == Command.END) {
                        server.broadcast(nick + " вышел из чата");
                        server.unsubscribe(this);
                        break;
                    }
                    if (command == Command.PRIVATE_MESSAGE) {
                        server.sendMessageToClient(this, params[0], params[1]);
                        continue;
                    }

                    if (command == Command.RENAME) {
                        try {
                            authService.setNewNick(nick, params[0]);
                            server.broadcast(nick + " изменил ник на " + params[0]);
                            logger.info(nick + " изменил ник на " + params[0]);
                            server.unsubscribe(this);
                            nick=params[0];
                            server.subscribe(this);

                            continue;
                        } catch (SQLException e) {
                            sendMessage("Произошла ошибка или данный ник уже занят");
                            continue;
                        }

                    }


                }
                server.broadcast(nick + ": " + msg);
                logger.info(nick + ": " + msg);
            }
        } catch (IOException e) {
            System.out.println("Подключение потеряно для отправки сообщаения");
        }

    }

    public String getNick() {
        return nick;
    }
}