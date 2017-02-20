package DAO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import VO.*;
public class CursoDAO {

    private RandomAccessFile raf;
    private RandomAccessFile rafIndex;
    private RandomAccessFile rafAux;

    public CursoDAO() throws FileNotFoundException {

        this.raf = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Cursos.txt", "rw");//Para apuntar a los registros
        this.rafIndex = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Cursos.txt", "rw");//Para apuntar a los nodos del arbol
        this.rafAux = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Cursos.txt", "rw");

    }

    public void inscribirCurso(CursoVO curso) throws IOException {

        long posRegistro = 280;
        boolean primero = false;

        if (curso.getCodigo() > 0) {

            if (raf.length() == 0) {//Si el archivo esta vacio 

                System.out.println("Este es el primer registro");

                for (int i = 0; i < 280; i++) {//Se reservan 280 bytes para la indexacion
                    raf.writeBytes("\u0000");
                    primero = true;
                }

            } else {
                posRegistro = raf.length();
            }

            raf.seek(posRegistro);

            indexar(primero, posRegistro, curso);

            rafIndex.writeInt(curso.getCodigo());//pk del registro
            rafIndex.skipBytes(16);//Se reservan 16 bytes para la posicion de registros futuros
            rafIndex.writeLong(raf.getFilePointer());//direccion del registro

            raf.writeInt(curso.getCodigo());

            for (int i = 0; i < curso.getNombre().length; i++) {
                char letra = curso.getNombre()[i];
                raf.writeChar(letra);

            }
            for (int i = (int) raf.getFilePointer(); i < posRegistro + 44; i++) {//Se llenan las siguientes posiciones con vacio para reservarlas

                raf.writeBytes("\u0000");
            }

            raf.writeInt(curso.getDuracion());
            raf.writeInt(curso.getIdProf());
            System.out.println("Curso inscrito correctamente");

        } else {

            System.out.println("No se pudo inscribir el curso");

        }

    }

    //Los nodos en el arbol constan de: 4 bytes para la pk, 8 para la posicion del id menor, 8 para la posicion del id mayor
    //y 8 para la pisicion del id en los registros.
    private void indexar(boolean primero, long posRegistro, CursoVO curso) throws IOException {//metodo para generar los indices en el arbol

        if (primero) {

            rafIndex.seek(0);

        } else {

            rafIndex.seek(((posRegistro - 280) / 52) * 28);//Ir al ultimo nodo en el arbol

            rafAux.seek(0);

            while (rafAux.getFilePointer() != 280) {

                if (rafAux.readInt() > curso.getCodigo()) {

                    if (rafAux.readLong() == 0) {
                        rafAux.seek(rafAux.getFilePointer() - 8);
                        rafAux.writeLong(rafIndex.getFilePointer());
                        break;
                    } else {
                        rafAux.skipBytes(16);
                    }

                } else {
                    rafAux.skipBytes(8);

                    if (rafAux.readLong() == 0) {
                        rafAux.seek(rafAux.getFilePointer() - 8);
                        rafAux.writeLong(rafIndex.getFilePointer());
                        break;
                    } else {
                        rafAux.skipBytes(8);
                    }

                }
            }

        }
    }

    public ArrayList<CursoVO> listarCursos() throws IOException {//Retorna arraylist con todos los registros que encuentre
        raf.seek(280);
        boolean hayRegistros = false;
        rafIndex.seek(20);     //Para verificar si la posición no es = -1(borrado)
        ArrayList<CursoVO> registros = new ArrayList<>();
        CursoVO curso;

        int i = 0;

        while (raf.getFilePointer() != raf.length() && raf.length() != 0) {

            if (rafIndex.readLong() > -1) {

                hayRegistros = true;

                int cod = raf.readInt();

                ArrayList<Character> charsNombre = new ArrayList<>();

                for (int k = 0; k < 40; k += 2) {

                    charsNombre.add(raf.readChar());

                }
                char[] nombre = new char[charsNombre.size()];

                for (int k = 0; k < charsNombre.size(); k++) {

                    nombre[k] = charsNombre.get(k);

                }

                int duracion = raf.readInt();
                int idProf = raf.readInt();

                curso = new CursoVO(cod, nombre, duracion, idProf);
                registros.add(curso);

            } else {
                raf.skipBytes(52);
            }

            rafIndex.skipBytes(20);
            i++;
        }

        if (!hayRegistros) {

            System.out.println("No hay ningun curso registrado");

        }

        return registros;
    }

    public long getPosicion(int clave) throws IOException {//retorna a posicion en bytes si lo encuentra. Si no, -1

        raf.seek(0);

        for (int i = 0; i < (raf.length() - 280) / 52; i++) {//la division se hace para saber la cantidad de registros

            if (raf.readInt() == clave) {

                raf.skipBytes(16);

                if (raf.readLong() == -1) {

                    return -1;

                } else {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();

                    rafAux.seek(pos);

                    return pos;
                }

            }
            raf.seek((int) raf.getFilePointer() - 4);

            if (raf.readInt() > clave) {

                if (raf.readLong() != 0) {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();
                    raf.seek(pos);
                    continue;

                } else {
                    raf.skipBytes(16);
                }

            }
            raf.seek((int) raf.getFilePointer() - 4);
            if (raf.readInt() < clave) {
                raf.skipBytes(8);

                if (raf.readLong() != 0) {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();
                    raf.seek(pos);

                } else {
                    raf.skipBytes(8);
                }

            }

        }
        return -1;
    }

    public void borrarCurso(int clave) throws IOException {//Cuando encuentra el registro sobreescribe el campo que apunta a él con -1
        raf.seek(0);
        long pos = getPosicion(clave);

        if (pos == -1) {

            System.out.println("El curso con codigo " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek((pos - 280) / 52 * 28);//Ir al nodo que pertenece al registro en el arbol
            raf.skipBytes(20);
            raf.writeLong(-1);//Se sobreescribe el campo con -1 para no volverlo a leer
            System.out.println("El curso con codigo " + clave + " ha sido borrado");
        }
    }

    public void buscarCurso(int clave) throws IOException {

        long pos = getPosicion(clave);

        if (pos == -1) {

            System.out.println("El curso con codigo " + clave + " no se encuentra en los registros.");

        } else {
            raf.seek(pos);
            System.out.println("Codigo: " + raf.readInt());
            String nombre = "";

            for (int i = 0; i < 40; i += 2) {
                nombre = nombre + raf.readChar();
            }

            System.out.println("Nombre: " + nombre);
            System.out.println("Duración: " + raf.readInt());
            System.out.println("ID del profesor " + raf.readInt());

        }

    }

    public void modificarNombre(int clave, String nombre) throws IOException {

        long pos = getPosicion(clave);

        if (pos == -1) {
            System.out.println("No hay ningun curso con codigo " + clave);
        } else {

            raf.seek(pos + 4);
            char[] letras = nombre.toCharArray();

            for (int i = 0; i < letras.length; i++) {

                raf.writeChar(letras[i]);

            }
            for (int i = (int) raf.getFilePointer(); i < pos + 44; i++) {
                raf.writeBytes("\u0000");
            }

        }

    }

    public void modificarDuracion(int clave, int nuevaD) throws IOException {

        long pos = getPosicion(clave);

        if (pos == -1) {
            System.out.println("No hay ningun curso con codigo " + clave);
        } else {

            raf.seek(pos + 44);
            raf.writeInt(nuevaD);

        }
    }

    public void modificarProfesor(int clave, int nuevoId) throws IOException {

        long pos = getPosicion(clave);

        if (pos == -1) {
            System.out.println("No hay ningun curso con codigo " + clave);

        } else {
            ProfesorDAO daoP = new ProfesorDAO();
            long pos2 = daoP.getPosicion(nuevoId);

            if (pos2 == -1) {
                System.out.println("No hay ningun profesor con id " + nuevoId);

            } else {

                raf.seek(pos + 48);
                raf.writeInt(nuevoId);

            }

        }
    }

    public ArrayList listarCursosExcepto(String nombre) throws IOException {//Sirve para retornar lista de todos los cursos que no son, por ejemplo, de ingles

        ArrayList<CursoVO> cursos = listarCursos();

        for (int i = 0; i < cursos.size(); i++) {

            String c = "";

            for (int j = 0; j < cursos.get(i).getNombre().length; j++) {

                if (String.valueOf(cursos.get(i).getNombre()[j]).contentEquals("\u0000")) {
                    break;
                } else {
                    c += cursos.get(i).getNombre()[j];
                }

            }
            if (c.equalsIgnoreCase(nombre)) {
                cursos.remove(i);
                i--;
            }

        }
        return cursos;

    }

    public ArrayList listarCursosConDuracion(String nombre, int duracion) throws IOException {//Listar cursos de mas de 10 dias, por ejemplo, y que no sean de fotografia.

        ArrayList<CursoVO> cursos = listarCursosExcepto(nombre);

        for (int i = 0; i < cursos.size(); i++) {

            if (cursos.get(i).getDuracion() <= duracion) {

                cursos.remove(i);
                i--; //Se retrocede i por que el tamaño del arreglo disminuyó
            }

        }
        return cursos;

    }

    public ArrayList cursosSinProfesor() throws IOException {//retorna lista con los codigos de los cursos sin profesor asignado

        ArrayList<Integer> codigos = new ArrayList<>();
        ArrayList<CursoVO> cursos = listarCursos();

        for (CursoVO curso : cursos) {

            if (curso.getIdProf() == 0) {//Si el id del profesor en el curso es cero es porque aun no se le ha asignado alguno

                codigos.add(curso.getCodigo());

            }

        }
        return codigos;

    }

    public ArrayList listarCursosPorProfesor(int id) throws IOException {//Retorna la lista de todos los cursos que dicta un profesor.

        ArrayList<CursoVO> registros = listarCursos();

        ArrayList<CursoVO> cursos = new ArrayList<>();

        for (CursoVO registro : registros) {

            if (registro.getIdProf() == id) {

                cursos.add(registro);

            }

        }
        return cursos;
    }

}
