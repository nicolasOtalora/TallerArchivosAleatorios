package DAO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import VO.*;

public class InscripcionDAO {

    private RandomAccessFile raf;
    private RandomAccessFile rafIndex;
    private RandomAccessFile rafAux;

    public InscripcionDAO() throws FileNotFoundException {

        this.raf = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Inscripciones.txt", "rw");//Para apuntar a los registros
        this.rafIndex = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Inscripciones.txt", "rw");//Para apuntar a los nodos del arbol
        this.rafAux = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Inscripciones.txt", "rw");

    }

    public void realizarInscripcion(InscripcionVO ins) throws IOException {

        long posRegistro = 320;//Para el arbol:
        //4 bytes para idEst + 4 para IdCurso + 20 para fechaIni + 20 para fechaFinf + 4 para nota.
        boolean primero = false;

        if (raf.length() == 0) {//Si el archivo esta vacio 
            System.out.println("Este es el primer registro");

            for (int i = 0; i < 320; i++) {//Se reservan 320 bytes para la indexacion (el arbol)
                raf.writeBytes("\u0000");
                primero = true;
            }

        } else {
            posRegistro = raf.length();
        }

        raf.seek(posRegistro);

        indexar(primero, posRegistro, ins);

        rafIndex.writeInt(ins.getIdEst());//Id del estudiante
        rafIndex.writeInt(ins.getIdCurso());//Id del curso
        rafIndex.skipBytes(16);//Se reservan 16 bytes para la posicion de registros futuros
        rafIndex.writeLong(raf.getFilePointer());//direccion del registro

        raf.writeInt(ins.getIdEst());
        raf.writeInt(ins.getIdCurso());

        for (int i = 0; i < ins.getFechaIni().length; i++) {

            char caracter = ins.getFechaIni()[i];
            raf.writeChar(caracter);

        }
        for (int i = (int) raf.getFilePointer(); i < posRegistro + 28; i++) {
            raf.writeBytes("\u0000");
        }

        for (int i = 0; i < ins.getFechaFin().length; i++) {

            char caracter = ins.getFechaFin()[i];
            raf.writeChar(caracter);

        }
        for (int i = (int) raf.getFilePointer(); i < posRegistro + 48; i++) {
            raf.writeBytes("\u0000");
        }

        raf.writeFloat(ins.getNota());

    }
//Los nodos en el arbol constan de: 4 bytes para el id del estudiante, 4 para el codigo del curso, 8 para la posicion del id menor, 8 para la posicion del id mayor
//y 8 para la posicion del id en los registros.

    private void indexar(boolean primero, long posRegistro, InscripcionVO ins) throws IOException {//metodo para generar los indices en el arbol

        if (primero) {

            rafIndex.seek(0);

        } else {

            rafIndex.seek(((posRegistro - 320) / 52) * 32);

            rafAux.seek(0);

            while (true) {

                if (rafAux.readInt() < ins.getIdEst()) {

                    rafAux.skipBytes(12);//Salta el id del curso y el long de la posicion del nodo menor

                    if (rafAux.readLong() == 0) {
                        rafAux.seek(rafAux.getFilePointer() - 8);
                        rafAux.writeLong(rafIndex.getFilePointer());
                        break;
                    } else {

                        rafAux.skipBytes(8);

                    }

                } else {

                    rafAux.skipBytes(4);//Salta el id del curso

                    if (rafAux.readLong() == 0) {
                        rafAux.seek(rafAux.getFilePointer() - 8);
                        rafAux.writeLong(rafIndex.getFilePointer());
                        break;

                    } else {

                        rafAux.skipBytes(16);

                    }

                }
            }

        }
    }

    public ArrayList<InscripcionVO> listarInscripciones() throws IOException {//Retorna un arraylist con todos los resgistros que encuentre
        raf.seek(320);
        boolean hayRegistros = false;
        rafIndex.seek(24);//Para comparar si la posición no es = -1

        ArrayList<InscripcionVO> registros = new ArrayList<>();
        InscripcionVO inscripcion;
        int i = 0;

        while (raf.getFilePointer() != raf.length() && raf.length() != 0) {

            if (rafIndex.readLong() > -1) {

                hayRegistros = true;

                int idEst = raf.readInt();
                int idCurso = raf.readInt();

                ArrayList<Character> charsfechaIni = new ArrayList<>();
                ArrayList<Character> charsFechaFin = new ArrayList<>();

                for (int k = 0; k < 20; k += 2) {

                    charsfechaIni.add(raf.readChar());

                }
                char[] fechaIni = new char[charsfechaIni.size()];

                for (int k = 0; k < charsfechaIni.size(); k++) {

                    fechaIni[k] = charsfechaIni.get(k);

                }
                for (int k = 0; k < 20; k += 2) {
                    charsFechaFin.add(raf.readChar());
                }

                char[] fechaFin = new char[charsFechaFin.size()];

                for (int k = 0; k < charsFechaFin.size(); k++) {
                    fechaFin[k] = charsFechaFin.get(k);
                }

                float nota = raf.readFloat();

                inscripcion = new InscripcionVO(idEst, idCurso, fechaIni, fechaFin, nota);
                registros.add(inscripcion);

            } else {
                raf.skipBytes(52);
            }
//            

            rafIndex.skipBytes(24);
            i++;
        }
        if (!hayRegistros) {

            System.out.println("No hay ningun inscripcion realizada");

        }

        return registros;
    }

    private long getPosicion(int idEst, int idCurso) throws IOException {

        raf.seek(0);

        for (int i = 0; i < (raf.length() - 320) / 52; i++) {//la division se hace para saber la cantidad de registros

            rafAux.seek((int) raf.getFilePointer() + 4);//para leer los id de los cursos

            if (raf.readInt() == idEst && rafAux.readInt() == idCurso) {

                raf.skipBytes(20);

                if (raf.readLong() == -1) {

                    return -1;

                } else {

                    raf.seek(raf.getFilePointer() - 8);
                    long pos = raf.readLong();
                    return pos;
                }

            }
            raf.seek((int) raf.getFilePointer() - 4);

            if (raf.readInt() > idEst) {

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

            if (raf.readInt() < idEst) {
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

    public void borrarInscripcion(int idEst, int idCurso) throws IOException {//Cuando encuentra el registro sobreescribe el campo que apunta a él con -1

        raf.seek(0);
        long pos = getPosicion(idEst, idCurso);

        if (pos == -1) {

            System.out.println("No hay ningun estudiante con id " + idEst + " inscrito en el curso con #" + idCurso);

        } else {

            raf.seek((pos - 320) / 52 * 32);//Ir al nodo correspondiente del registro en el arbol
            raf.skipBytes(24);
            raf.writeLong(-1);//Se sobreescribe el campo con -1 para no volverlo a leer
            System.out.println("La inscripcion del estudiante con id " + idEst + " en el curso "
                    + idCurso + " ha sido borrada de los resgistros");

        }

    }

    public ArrayList<Integer> buscarInscripciones(int idEst) throws IOException {//Retorna lista con los codigos de los cursos a los que esta inscrito el estudiante

        ArrayList<Integer> codigos = new ArrayList<>();
        raf.seek(0);

        for (int i = 0; i < (raf.length() - 320) / 52; i++) {//la division se hace para saber la cantidad de registros

            if (raf.readInt() == idEst) {

                codigos.add(raf.readInt());

            } else {
                raf.skipBytes(28);
                continue;
            }

            raf.skipBytes(24);
        }
        return codigos;
    }

    public void modificarPromedio(int idEst, int idCurso, int nuevoProm) throws IOException {

        long pos = getPosicion(idEst, idCurso);

        if (pos == -1) {

            System.out.println("No hay ningun estudiante con id " + idEst + " inscrito en el curso con #" + idCurso);

        } else {

            raf.seek(pos + 48);
            raf.writeFloat(nuevoProm);

        }

    }

    public void listarEstudiantesPorNota(float nota) throws IOException {//Muestra la lista de los estudiantes con nota mayor o igual al parametro.
//Pueden repetirse estudiantes porque alguno puede estar inscrito en mas de un curso
        ArrayList<InscripcionVO> ins = listarInscripciones();//Para comparar con la lista de estudiantes
        ArrayList<Integer> ids = new ArrayList<>();//Para guardas los ids de los estudiantes temporalmente
        ArrayList<Float> notas = new ArrayList<>();//Para guardar cada nota

        rafAux.seek(320);//Apunta a los ids de los estudiantes
        raf.seek(368);//Apunta a los campos de las notas

        for (int i = 0; i < ins.size(); i++) {

            if (raf.readFloat() >= nota) {

                ids.add(rafAux.readInt());

                raf.seek(raf.getFilePointer() - 4);
                notas.add(raf.readFloat());

                raf.skipBytes(48);
                rafAux.skipBytes(48);

            } else {

                rafAux.skipBytes(52);
                raf.skipBytes(48);

            }

        }

        for (int i = 0; i < ids.size(); i++) {

            EstudianteDAO estDao = new EstudianteDAO();
            estDao.buscarEstudiante(ids.get(i));//Este metodo ya imprime los datos del estudiante buscado
            System.out.println("Nota :" + notas.get(i));

        }

    }

    public ArrayList listarEstudiantesPorProfesor(int idProf) throws FileNotFoundException, IOException {//Retorna la lista de los estudiantes inscritos en cursos del profesor especificado.

        ArrayList<Integer> idEsts = new ArrayList<>();//para los ids de los estudiantes

        ArrayList<InscripcionVO> insc = listarInscripciones();
        ArrayList<CursoVO> cursos = new CursoDAO().listarCursosPorProfesor(idProf);

        for (int j = 0; j < cursos.size(); j++) {

            for (int i = 0; i < insc.size(); i++) {

                if (insc.get(i).getIdCurso() == cursos.get(j).getCodigo()) {

                    idEsts.add(insc.get(i).getIdEst());

                }

            }

        }
        return idEsts;
    }

    public void cantidadEstudiantes(int idCurso) throws IOException {

        rafIndex.seek(4);//Apunta a las posiciones en el arbol en que esta el id del curso
        int limite = (int) (rafIndex.length() - 320) / 52 * 32;//La division se hace para saber el tamaño del arbol
        int cantidad = 0;

        for (int i = 0; rafIndex.getFilePointer() < limite; i++) {
            System.out.println("pointeeeeer " + rafIndex.getFilePointer());

            if (rafIndex.readInt() == idCurso) {
                cantidad++;
                rafIndex.skipBytes(28);

            } else {
                rafIndex.skipBytes(28);
            }

        }

        System.out.println("En el curso " + idCurso + " hay " + cantidad + " estudiantes inscritos.");

    }

    public void buscarEstudianteConPromedio(int idEst) throws IOException {

        ArrayList<Integer> cods = buscarInscripciones(idEst);
        ArrayList<InscripcionVO> insc = listarInscripciones();
        float suma = 0;

        for (int i = 0; i < cods.size(); i++) {

            for (int j = 0; j < insc.size(); j++) {

                if (cods.get(i) == insc.get(j).getIdCurso() && idEst == insc.get(j).getIdEst()) {

                    suma += insc.get(j).getNota();

                }
            }

        }
        float promedio = suma / cods.size();
        System.out.println("El promedio del estudiante " + idEst + " es: " + promedio);
    }
}
