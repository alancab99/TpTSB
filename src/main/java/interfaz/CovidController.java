package interfaz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import negocio.Departamentos;
import soporte.TSBTextFile;

import java.io.File;

public class CovidController {
    public ListView lvwResultados;

    public void onCargarClick(ActionEvent actionEvent) {
        //Creo el Objeto Departamentos que contiene el conjunto de departamentos de Córdoba
        //Todos los objetos 'departamento' que creamos los guardaremos en Departamentos para asi mantener una comunicacion
        //Entre la interfaz y el negocio

        Departamentos departamentos = new Departamentos();
        ObservableList ol = FXCollections.observableArrayList(departamentos.getResultados());
        lvwResultados.setItems(ol);


        //ObservableList ol;
        //Cargar en una lista desplegable los distritos (provincias)
        //Regiones regiones = new Regiones(lblUbicacion.getText() + "\\descripcion_regiones.dsv");
        //Collection distritos = regiones.getDistritos();
        //ol = FXCollections.observableArrayList(distritos);
        //cboDistritos.setItems(ol);
        //Cargar en el list view la cantidad de votos total por agrupacion politica
        //Agrupaciones agrupaciones = new Agrupaciones();
        //Collection resultado = agrupaciones.generarEscrutinio(lblUbicacion.getText() + "\\descripcion_postulaciones.dsv", lblUbicacion.getText() + "\\mesas_totales_agrp_politica.dsv");
        //ol = FXCollections.observableArrayList(resultado);
        //lvwResultados.setItems(ol);
    }


}