package negocio;

public class Departamento {
    private int codigo;
    private String nombre;
    private String oDosis;
    private String sexo;
    private String vacuna;
    private String dpto;


    public Departamento(int codigo, String nombre, String oDosis, String sexo, String vacuna) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.oDosis = oDosis;
        this.sexo = sexo;
        this.vacuna = vacuna;
    }

    @Override
    public String toString() {
        return "Córdoba{" +
                "votante =" + codigo +
                ", Departamento='" + nombre + '\'' +
                ", oDosis='" + oDosis + '\'' +
                ", sexo='" + sexo + '\'' +
                ", vacuna='" + vacuna + '\'' +
                '}';
    }

    public int getCodigo() {
        return codigo;
    }
}
