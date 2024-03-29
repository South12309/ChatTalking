package ru.gb.gbchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.apache.logging.log4j.LogManager;
import ru.gb.gbchat.Command;
import org.apache.logging.log4j.*;


public class ChatServer {


    private static final Logger logger = LogManager.getLogger(ChatServer.class);

    private final Map<String, ClientHandler> clients;
    final private ExecutorService executorService= Executors.newCachedThreadPool();

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new SQLiteAuthService()) {
            logger.info("Сервер запущен");
            while (true) {
                System.out.println("Wait client connection...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            logger.error("Ошибка запуска сервера" +e.getMessage());
            e.printStackTrace();
        }
        executorService.shutdownNow();
        logger.info("Сервер остановлен");
    }

    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
        logger.info(client.getNick() + " подключен");

    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
        logger.info(client.getNick() + " отключен");
    }


    private void broadcastClientList() {
        StringBuilder nicks = new StringBuilder();
        for (ClientHandler value : clients.values()) {
            nicks.append(value.getNick()).append(" ");
        }
//        final String nicks = clients.values().stream()
//                .map(ClientHandler::getNick)
//                .collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broadcast(Command command, String nicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, nicks);
        }
    }

    public void broadcast(String msg) {
        clients.values().forEach(client -> client.sendMessage(msg));
    }

    public void sendMessageToClient(ClientHandler sender, String to, String message) {
        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("от " + sender.getNick() + ": " + message);
            sender.sendMessage("участнику " + to + ": " + message);
            logger.info("Личное сообщение от " + sender.getNick() + " участнику " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
        }
    }
}