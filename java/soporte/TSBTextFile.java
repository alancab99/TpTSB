package soporte;

import negocio.Departamento;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TSBTextFile {
    private File file;

    public TSBTextFile(String path) {
        file = new File(path);
    }


    public TPUHashtable identificarDepartamentos() {
        String linea = "", campos[];
        int cod = 0;
        TPUHashtable table = new TPUHashtable();
        Departamento departamento;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                linea = scanner.nextLine();
                campos = linea.split(",");

                if (campos[6].compareTo('"'+"Córdoba"+'"')==0) {
                    //Si es córdoba, creo un objeto Departamento con sus atributos y lo agrego a una tabla Hash con el código
                    //que luego se sumara para que sea unico.
                    departamento = new Departamento(cod, campos[4], campos[13] ,campos[0], campos[11]);
                    table.put(departamento.getCodigo(), departamento);
                    cod += 1;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo" + file);
        }
        return table;
    }
}
