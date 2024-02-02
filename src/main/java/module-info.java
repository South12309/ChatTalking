module ru.gb.chattalking {
    requires javafx.controls;
    requires javafx.fxml;
  //  requires jfxmessagebox;
    requires java.desktop;
    requires java.sql;
    requires org.apache.logging.log4j;


    exports ru.gb.gbchat;
    opens ru.gb.gbchat to javafx.fxml;
    exports ru.gb.gbchat.server;
    opens ru.gb.gbchat.server to javafx.fxml;
    exports ru.gb.gbchat.client;
    opens ru.gb.gbchat.client to javafx.fxml;

}