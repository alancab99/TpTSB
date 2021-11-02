module interfaz.p13_elecciones {
    requires javafx.controls;
    requires javafx.fxml;


    opens interfaz to javafx.fxml;
    exports interfaz;
}