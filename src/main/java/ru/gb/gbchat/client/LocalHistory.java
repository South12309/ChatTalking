package ru.gb.gbchat.client;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LocalHistory implements Closeable {

    private BufferedWriter fileOutput;
    final private Controller controller;

    public LocalHistory(Controller controller) {

        this.controller = controller;
    }

    public void loadHistory() {
        try {
            fileOutput = new BufferedWriter(new FileWriter("local_" + controller.getLoginField().getText() + ".txt", StandardCharsets.UTF_8, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                char ch = (char) randomAccessFile.read();

                if (ch == '\n') {
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

    @Override
    public void close() throws IOException {
        if (fileOutput != null) {
            fileOutput.close();
        }
    }
}
