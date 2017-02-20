package VO;

import java.io.FileNotFoundException;
import java.io.IOException;
import DAO.*;
public class CursoVO {

    private int codigo;
    private char[] nombre;//de 40 bytes(20 caracteres)
    private int duracion;
    private int idProf;

    public CursoVO(int codigo, char[] nombre, int duracion, int idProf) throws FileNotFoundException, IOException {

        if (!esProfesor(idProf)) {// Si no hay un profesor con ese id, no se le asigna niguno.  

            this.idProf = 0;
            System.out.println("No hay ningún profesor con id " + idProf + " en los registros.");
            this.codigo = codigo;
            this.nombre = nombre;
            this.duracion = duracion;

        } else {

            this.codigo = codigo;
            this.nombre = nombre;
            this.duracion = duracion;
            this.idProf = idProf;

        }

    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public char[] getNombre() {
        return nombre;
    }

    public void setNombre(char[] nombre) {
        this.nombre = nombre;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public int getIdProf() {
        return idProf;
    }

    public void setIdProf(int idProf) {
        this.idProf = idProf;
    }

    private boolean esProfesor(int id) throws FileNotFoundException, IOException {//Este metodo comprueba si existe un profesor con ese id para poder construirlo

        long pos = new ProfesorDAO().getPosicion(id);

        return pos != -1;

    }

}
