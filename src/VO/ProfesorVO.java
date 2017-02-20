package VO;

public class ProfesorVO {
    private char[]nombre;//de 40 bytes(20 caracteres)
    private char []apellido;//de 40 bytes(20 caracteres)
    private int id;
    private int ext;

    public ProfesorVO(char[] nombre, char[] apellido, int id, int ext) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.ext = ext;
    }

    public char[] getNombre() {
        return nombre;
    }

    public void setNombre(char[] nombre) {
        this.nombre = nombre;
    }

    public char[] getApellido() {
        return apellido;
    }

    public void setApellido(char[] apellido) {
        this.apellido = apellido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExt() {
        return ext;
    }

    public void setExt(int ext) {
        this.ext = ext;
    }
    
}
