package ru.gb.gbchat.client;


import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.gb.gbchat.Command;

public class Controller {

        @FXML
        private ListView<String> clientList;
        @FXML
        private HBox messageBox;
        @FXML
        private PasswordField passwordField;

    public TextField getLoginField() {
        return loginField;
    }

    @FXML
        private TextField loginField;
        @FXML
        private HBox loginBox;
        @FXML
        private TextField textField;
        @FXML
        private TextArea textArea;

        private final ChatClient client;

        public Controller() {
            client = new ChatClient(this);
            while (true) {
                try {
                    client.openConnection();
                    break;
                } catch (Exception e) {
                    showNotification();
                }
            }
        }

        public void btnSendClick(ActionEvent event) {
            final String message = textField.getText().trim();
            if (message.isEmpty()) {
                return;
            }
            client.sendMessage(message);
            textField.clear();
            textField.requestFocus();
        }

        public void addMessage(String message) {
            textArea.appendText(message + "\n");
        }


        public void btnAuthClick(ActionEvent actionEvent) {
            client.sendMessage(Command.AUTH, loginField.getText(), passwordField.getText());
        }

        public void setAuth(boolean success) {
            loginBox.setVisible(!success);
            messageBox.setVisible(success);
            if (!success)
                client.closeConnection();

        }

        public void showNotification() {
            final Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Не могу подключится к серверу.\n" +
                            "Проверьте, что сервер запущен",
                    new ButtonType("Попробовать еще", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.setTitle("Ошибка подключения");
            final Optional<ButtonType> buttonType = alert.showAndWait();
            final Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
            if (isExit) {
                System.exit(0);
            }
        }

        public void showError(String[] error) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, error[0], new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
            alert.setTitle("Ошибка!");
            alert.showAndWait();

        }

        public void selectClient(MouseEvent mouseEvent) {
            if (mouseEvent.getClickCount() == 2) { // /w nick1 private message
                final String message = textField.getText();
                final String nick = clientList.getSelectionModel().getSelectedItem();
                textField.setText(Command.PRIVATE_MESSAGE.collectMessage(nick, message));
                textField.requestFocus();
                textField.selectEnd();
            }
        }

        public void updateClientList(String[] params) {
            clientList.getItems().clear();
            clientList.getItems().addAll(params);
        }
    }