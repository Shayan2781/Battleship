module com.battleship {
    requires javafx.controls;
    requires javafx.fxml;


    exports Datas;
    opens Datas to javafx.fxml;
    exports com.battleship.Client;
    opens com.battleship.Client to javafx.fxml;
}