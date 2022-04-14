module ru.gb.chattalking {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.gb.gbchat to javafx.fxml;
    exports ru.gb.gbchat;
    exports ru.gb.gbchat.server;
    opens ru.gb.gbchat.server to javafx.fxml;
}