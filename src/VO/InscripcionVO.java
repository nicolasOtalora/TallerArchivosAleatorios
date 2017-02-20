package VO;

import java.io.FileNotFoundException;
import java.io.IOException;
import DAO.*;

public class InscripcionVO {

    private int idCurso;
    private int idEst;
    private char[] fechaIni;//de 20 bytes (10 caracteres)
    private char[] fechaFin;//de 20 bytes (10 caracteres)
    private float nota;

    public InscripcionVO(int idEst,int idCurso,  char[] fechaIni, char[] fechaFin, float nota) throws IOException {

        
        
        if (esEstudiante(idEst) && esCurso(idCurso)) {

            this.idCurso = idCurso;
            this.idEst = idEst;
            this.fechaIni = fechaIni;
            this.fechaFin = fechaFin;
            this.nota = nota;

        }else{
            System.out.println("No se pudo realizar la inscripción");
        }

    }

    public int getIdEst() {
        return idEst;
    }

    public void setIdEst(int idEst) {
        this.idEst = idEst;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public char[] getFechaIni() {
        return fechaIni;
    }

    public void setFechaIni(char[] fechaIni) {
        this.fechaIni = fechaIni;
    }

    public char[] getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(char[] fechaFin) {
        this.fechaFin = fechaFin;
    }

    public float getNota() {
        return nota;
    }

    public void setNota(float nota) {
        this.nota = nota;
    }

    private boolean esEstudiante(int id) throws FileNotFoundException, IOException {//Este metodo comprueba si existe un estudiante con ese id para poder construirlo

        long pos = new EstudianteDAO().getPosicion(id);

        return pos != -1;

    }

    private boolean esCurso(int id) throws FileNotFoundException, IOException {//Este metodo comprueba si existe un curso con ese id para poder construirlo

        long pos = new CursoDAO().getPosicion(id);

        return pos != -1;

    }
}
