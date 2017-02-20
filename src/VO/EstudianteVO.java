package VO;

public class EstudianteVO {
    
    private char[]nombre;//de 40 bytes(20 caracteres)
    private char []apellido;//de 40 bytes(20 caracteres)
    private int id;
    private int telefono;

    public EstudianteVO(char[] nombre, char[] apellido, int id, int telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.telefono = telefono;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
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
    
    
    
    
    
    
    
}
