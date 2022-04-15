package ru.gb.gbchat;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ClientController controller;

    public ChatClient(ClientController controller) {
        this.controller = controller;

    }

    public void openConnection() throws IOException {

        socket = new Socket(SERVER_IP, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(()->{
            try {
                waitAuth();
                readMessage();
            } finally {
                closeConnection();
            }
        }).start();
    }

    private void readMessage() {
        while (true) {
            try {
                String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    controller.toggleBoxesVisibility(false);
                    break;
                }

                controller.addMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void waitAuth() {
        while (true) {
            try {
                String msg = in.readUTF();
                if (msg.startsWith("/authok")) {
                    String[] split = msg.split("\\s");
                    String nick = split[1];
                    controller.toggleBoxesVisibility(true);
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    break;
                }
//                else {
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Пользователь не найден", ButtonType.OK);
//                    alert.showAndWait();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
