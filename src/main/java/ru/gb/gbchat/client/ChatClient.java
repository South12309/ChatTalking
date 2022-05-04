package ru.gb.gbchat.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.swing.*;

import javafx.application.Platform;
import ru.gb.gbchat.Command;


public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private BufferedWriter fileOutput;

    private final Controller controller;

    public ChatClient(Controller controller) {
        this.controller = controller;
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Таймаут авторизации или подключение с сервером закрыто",
                        "Ошибка",
                        JOptionPane.INFORMATION_MESSAGE);

            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();

    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            System.out.println("Receive message: " + message);
            if (Command.isCommand(message)) {
                final Command command = Command.getCommand(message);
                final String[] params = command.parse(message);
                if (command == Command.END) {
                    controller.setAuth(false);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            controller.updateClientList(params);
                        }
                    });
                    continue;
                }
            }
            controller.addMessage(message, true);
        }
    }

    private void waitAuthenticate() throws IOException {
        while (true) {

            final String msgAuth = in.readUTF();
            if (Command.isCommand(msgAuth)) {
                final Command command = Command.getCommand(msgAuth);
                final String[] params = command.parse(msgAuth);
                if (command == Command.AUTHOK) {
                    final String nick = params[0];
                    fileOutput = new BufferedWriter(new FileWriter("local_" + controller.getLoginField().getText() + ".txt", StandardCharsets.UTF_8, true));
                    loadHistory();
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    controller.setAuth(true);

                    break;
                }
                if (Command.ERROR.equals(command)) {
                    Platform.runLater(() -> controller.showError(params));
                }
            }
        }
    }

    private void loadHistory() {

        File file = new File("local_" + controller.getLoginField().getText() + ".txt");

        int lines = 100;
        int readLines = 0;
        StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;

            randomAccessFile.seek(fileLength);
            for (long pointer = fileLength; pointer >= 0; pointer--) {
                randomAccessFile.seek(pointer);
                char ch  = (char)randomAccessFile.read();

                if (ch=='\n') {
                    readLines++;
                    if (readLines == lines)
                        break;
                }

                builder.append(ch);
            }
            builder.reverse();


            String historyLog = new String(builder.toString().getBytes("ISO-8859-1"), "UTF-8");

            controller.addMessage(historyLog.trim());


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }


    }

    public void saveMessageInHistoryFile(String message) {
        try {
            fileOutput.write(message);
            fileOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutput != null) {
            try {
                fileOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);

    }

    public void sendMessage(String message) {
        try {
            System.out.println("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}