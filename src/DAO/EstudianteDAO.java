package DAO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import VO.*;
public class EstudianteDAO {

    private RandomAccessFile raf;
    private RandomAccessFile rafIndex;
    private RandomAccessFile rafAux;

    public EstudianteDAO() throws FileNotFoundException {
        this.raf = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Estudiantes.txt", "rw");//va a apuntar a los registros, por lo general
        this.rafIndex = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Estudiantes.txt", "rw");//para apuntar en los nodos del arbol
        this.rafAux = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Estudiantes.txt", "rw");

    }

    public void inscribirEstudiante(EstudianteVO est) throws IOException {

        long posRegistro = 280;
        boolean primero = false;

        if (est.getId() > 0) {

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

            indexar(primero, posRegistro, est);

            rafIndex.writeInt(est.getId());//pk del registro
            rafIndex.skipBytes(16);
            rafIndex.writeLong(raf.getFilePointer());//direccion del registro

            raf.writeInt(est.getId());
            for (int i = 0; i < est.getNombre().length; i++) {
                char letra = est.getNombre()[i];
                raf.writeChar(letra);

            }
            for (int i = (int) raf.getFilePointer(); i < posRegistro + 44; i++) {
                raf.writeBytes("\u0000");
            }
            for (int i = 0; i < est.getApellido().length; i++) {
                char letra = est.getApellido()[i];
                raf.writeChar(letra);
            }
            for (int i = (int) raf.getFilePointer(); i < posRegistro + 84; i++) {
                raf.writeBytes("\u0000");
            }
            raf.writeInt(est.getTelefono());
        } else {

            System.out.println("El estudiante debe tener un id mayor a cero");

        }

    }

    //Los indices en el arbol son: 4 bytes para la pk, 8 para la posicion del id menor, 8 para la posicion del id mayor
    //y 8 para la pisicion del id en los registros.
    
    private void indexar(boolean primero, long posRegistro, EstudianteVO est) throws IOException {//metodo para generar los indices en el arbol

        if (primero) {

            rafIndex.seek(0);

        } else {

            rafIndex.seek(((posRegistro - 280) / 88) * 28);//Ir al ultimo nodo en el arbol

            rafAux.seek(0);

            while (rafAux.getFilePointer() != 280) {

                if (rafAux.readInt() > est.getId()) {//Si es mayor guarda la posicion justo a su derecha

                    if (rafAux.readLong() == 0) {
                        rafAux.seek(rafAux.getFilePointer() - 8);
                        rafAux.writeLong(rafIndex.getFilePointer());
                        break;
                    } else {
                        rafAux.skipBytes(16);
                    }

                } else {//Si es menor guarda la posicion 16 bytes después
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

    public ArrayList<EstudianteVO> listarEstudiantes() throws IOException {//Retorna un arraylist con todos los registros que encuentre

        raf.seek(280);
        boolean hayRegistros = false;
        rafIndex.seek(20);//Para comparar si la posición no es = -1

        ArrayList<EstudianteVO> registros = new ArrayList<>();
        EstudianteVO estudiante;

        int i = 0;

        while (raf.getFilePointer() != raf.length() && raf.length() != 0) {

            if (rafIndex.readLong() > -1) {

                hayRegistros = true;

                int id = raf.readInt();

                ArrayList<Character> charsNombre = new ArrayList<>();//Para agregar cada caracter en el campo del nombre
                ArrayList<Character> charsApellido = new ArrayList<>();//Para agregar cada caracter en el campo del apellido

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

                estudiante = new EstudianteVO(nombre, apellido, id, telefono);
                registros.add(estudiante);

            } else {
                raf.skipBytes(88);
            }
//            

            rafIndex.skipBytes(20);
            i++;
        }

        if (!hayRegistros) {

            System.out.println("No hay ningun estudiante registrado");

        }

        return registros;
    }

    public long getPosicion(int clave) throws IOException {//Este metodo retorna la posicion del registro, si existe. Si no, retorna -1

        raf.seek(0);

        for (int i = 0; i < (raf.length() - 280) / 88; i++) {//la division se hace para saber la cantidad de registros

            if (raf.readInt() == clave) {

                raf.skipBytes(16);

                if (raf.readLong() == -1) {

                    return -1;

                } else {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();
                    //rafAux.seek(pos);

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

    public void borrarEstudiante(int clave) throws IOException {//Cuando encuentra el registro sobreescribe el campo que apunta a él con -1

        raf.seek(0);
        long pos = getPosicion(clave);

        if (pos == -1) {

            System.out.println("El estudiante con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek((pos - 280) / 88 * 28);//Ir al nodo que pertenece al registro en el arbol
            raf.skipBytes(20);
            raf.writeLong(-1);//Se sobreescribe el campo con -1 para no volverlo a leer
            System.out.println("El estudiante con id " + clave + " ha sido borrado de los resgistros");

        }

    }

    public void modificarNombre(int clave, String nuevoNombre) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El estudiante con id " + clave + " no se encuentra en los registros.");

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

            System.out.println("El estudiante con id " + clave + " no se encuentra en los registros.");

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
    public void modificarTelefono(int clave, int nuevoTelefono) throws IOException {

        long pos = getPosicion(clave);
        if (pos == -1) {

            System.out.println("El estudiante con id " + clave + " no se encuentra en los registros.");

        } else {

            raf.seek(pos + 84);

            raf.writeInt(nuevoTelefono);

        }

    }

    public void buscarEstudiante(int clave) throws IOException {

        long pos = getPosicion(clave);
        
        if (pos == -1) {

            System.out.println("El estudiante con id " + clave + " no se encuentra en los registros.");

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

            System.out.println("Telefono: " + raf.readInt());

        }

    }
    

}
