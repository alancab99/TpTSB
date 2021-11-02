package negocio;

import soporte.TPUHashtable;
import soporte.TSBTextFile;

import java.util.Collection;

public class Departamentos {
    private TSBTextFile fileDepartamentos;
    private TPUHashtable table;

    public Departamentos(){
        //Creo un TSBTextFile que se encargará de crear c/objeto departamento que corresponda a la ciudad de Córdoba
        fileDepartamentos =  new TSBTextFile("datosPrueba.csv");
        //Invoco el método que crea una tabla de departamentos y lo guardo en la Table
        table = fileDepartamentos.identificarDepartamentos();
    }


    public Collection getResultados(){
        return table.values();
    }

}
