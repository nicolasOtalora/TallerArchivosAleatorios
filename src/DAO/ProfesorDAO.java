package DAO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import VO.*;

public class ProfesorDAO {

    private RandomAccessFile raf;
    private RandomAccessFile rafIndex;
    private RandomAccessFile rafAux;

    public ProfesorDAO() throws FileNotFoundException {
        this.raf = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Profesores.txt", "rw");//Apunta a los registros
        this.rafIndex = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Profesores.txt", "rw");//Apunta a los nodos del arbol
        this.rafAux = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Profesores.txt", "rw");

    }

    public void inscribirProfesor(ProfesorVO profe) throws IOException {

        long posRegistro = 280;
        boolean primero = false;

        if (profe.getId() > 0) {

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

            indexar(primero, posRegistro, profe);

            rafIndex.writeInt(profe.getId());//pk del registro
            rafIndex.skipBytes(16);//Se reservan 16 bytes para la posicion de registros futuros
            rafIndex.writeLong(raf.getFilePointer());//direccion del registro

            raf.writeInt(profe.getId());
            for (int i = 0; i < profe.getNombre().length; i++) {
                char letra = profe.getNombre()[i];
                raf.writeChar(letra);

            }
            for (int i = (int) raf.getFilePointer(); i < posRegistro + 44; i++) {
                raf.writeBytes("\u0000");
            }
            for (int i = 0; i < profe.getApellido().length; i++) {
                char letra = profe.getApellido()[i];
                raf.writeChar(letra);
            }
            for (int i = (int) raf.getFilePointer(); i < posRegistro + 84; i++) {
                raf.writeBytes("\u0000");
            }
            raf.writeInt(profe.getExt());

        } else {
            System.out.println("El profesor debe tener un id mayor a cero");
        }

    }
    
    //Los nodos en el arbol constan de: 4 bytes para la pk, 8 para la posicion del id menor, 8 para la posicion del id mayor
    //y 8 para la pisicion del id en los registros.
    private void indexar(boolean primero, long posRegistro, ProfesorVO prof) throws IOException {//metodo para generar los indices en el arbol

        if (primero) {

            rafIndex.seek(0);

        } else {

            rafIndex.seek(((posRegistro - 280) / 88) * 28);

            rafAux.seek(0);
            while (true) {
                if (rafAux.readInt() > prof.getId()) {

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

    public ArrayList<ProfesorVO> listarProfesores() throws IOException {//Retorna un arraylist con todos los resgistros que encuentre
        raf.seek(280);
        boolean hayRegistros = false;
        rafIndex.seek(20);//Para comparar si la posición no es = -1

        ArrayList<ProfesorVO> registros = new ArrayList<>();
        ProfesorVO profesor;
        int i = 0;

        while (raf.getFilePointer() != raf.length() && raf.length() != 0) {

            if (rafIndex.readLong() > -1) {

                hayRegistros = true;

                int id = raf.readInt();

                ArrayList<Character> charsNombre = new ArrayList<>();
                ArrayList<Character> charsApellido = new ArrayList<>();

                for (int k = 0; k < 40; k += 2) {

                    charsNombre.add(raf.readChar());

                }
                char[] nombre = new char[charsNombre.size()];

                for (int k = 0; k < charsNombre.size(); k++) {

                    nombre[k] = charsNombre.get(k);

                }
                for (int k = 0; k < 40; k += 2) {
                    charsApellido.add(raf.readChar());
                }

                char[] apellido = new char[charsApellido.size()];
                for (int k = 0; k < charsApellido.size(); k++) {
                    apellido[k] = charsApellido.get(k);
                }

                int telefono = raf.readInt();

                profesor = new ProfesorVO(nombre, apellido, id, telefono);
                registros.add(profesor);

            } else {
                raf.skipBytes(88);
            }
//            

            rafIndex.skipBytes(20);
            i++;
        }
        if (!hayRegistros) {

            System.out.println("No hay ningun profesor registrado");

        }

        return registros;
    }

    public long getPosicion(int clave) throws IOException {//retorna a posicion en bytes si lo encuentra. Si no, -1

        raf.seek(0);

        for (int i = 0; i < (raf.length() - 280) / 88; i++) {//la division se hace para saber la cantidad de registros

            if (raf.readInt() == clave) {

                raf.skipBytes(16);

                if (raf.readLong() == -1) {

                    return -1;

                } else {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();

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

    public void borrarProfesor(int clave) throws IOException {//Cuando encuentra el registro sobreescribe el campo que apunta a él con -1

        raf.seek(0);
        long pos = getPosicion(clave);

        if (pos == -1) {

            System.out.println("El profesor con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek((pos - 280) / 88 * 28);//Ir al nodo correspondiente del registro en el arbol
            raf.skipBytes(20);
            raf.writeLong(-1);//Se sobreescribe el campo con -1 para no volverlo a leer
            System.out.println("El profesor con id " + clave + " ha sido borrado de los resgistros");

        }

    }

    public void buscarProfesor(int clave) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El profesor con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek(pos);

            String nombre = "";
            String apellido = "";
            System.out.println("Id: " + raf.readInt());

            for (int k = 0; k < 40; k += 2) {
                nombre = nombre + raf.readChar();

            }
            System.out.println("Nombre: " + nombre);

            for (int k = 0; k < 40; k += 2) {

                apellido = apellido + raf.readChar();

            }

            System.out.println("Apellido: " + apellido);

            System.out.println("Extension: " + raf.readInt());

        }

    }

    public void modificarNombre(int clave, String nuevoNombre) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El profesor con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek(pos + 4);

            char[] letras = nuevoNombre.toCharArray();

            for (int i = 0; i < letras.length; i++) {
                raf.writeChar(letras[i]);
            }

            for (int i = (int) raf.getFilePointer(); i < pos + 44; i++) {
                raf.writeBytes("\u0000");
            }

        }

    }

    public void modificarApellido(int clave, String nuevoApellido) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El profesor con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek(pos + 44);

            char[] letras = nuevoApellido.toCharArray();

            for (int i = 0; i < letras.length; i++) {
                raf.writeChar(letras[i]);
            }

            for (int i = (int) raf.getFilePointer(); i < pos + 84; i++) {
                raf.writeBytes("\u0000");
            }

        }

    }

    public void modificarExtension(int clave, int nuevaExtension) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El profesor con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek(pos + 84);

            raf.writeInt(nuevaExtension);

        }

    }

    public ArrayList sonEstudiantes() throws IOException {//Retorna una lista con todos los profesores que tambien estan en la lista de estudiantes

        ArrayList<ProfesorVO> profes = listarProfesores();
        ArrayList<EstudianteVO> ests = new EstudianteDAO().listarEstudiantes();//Se listan los estudiantes para comparar cada uno

        ArrayList<ProfesorVO> profesores = new ArrayList<>();

        for (int i = 0; i < profes.size(); i++) {//ciclo para recorrer los profesores

            for (int j = 0; j < ests.size(); j++) {//ciclo para recorrer los estudiantes

                if (profes.get(i).getId() == ests.get(j).getId()) {

                    profesores.add(profes.get(i));//Si hay un estudiante con id igual a un profesor, se agrega a la lista
                    break;

                }

            }

        }
        return profesores;
    }

}
