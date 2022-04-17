package ru.gb.gbchat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ClientController {

    private final ChatClient client;
    @FXML
    private HBox loginBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    @FXML
    private VBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;
    @FXML
    private Label welcomeText;

    public ClientController() {
        client = new ChatClient(this);

    }

    @FXML
    protected void authButtonClick() {
        try {
            client.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.sendMessage("/auth " + loginField.getText() + " " + passwordField.getText());
    }

    public void sendButtonClick() {
        String text = textField.getText();
        if (text.trim().isEmpty()) {
            return;
        }
        client.sendMessage(text);
        textField.clear();
        textField.requestFocus();

    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");
    }

    public void toggleBoxesVisibility(boolean isSuccess) {
        loginBox.setVisible(!isSuccess);
        messageBox.setVisible(isSuccess);
    }
}