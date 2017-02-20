package Ejecucion;

import DAO.EstudianteDAO;
import VO.EstudianteVO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Test {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        EstudianteDAO dao = new EstudianteDAO();

        RandomAccessFile raf = new RandomAccessFile("/Volumes/NICOLAS/Archivo/Estudiantes.txt", "rw");
//      

        char[] nombre = {'n', 'i', 'c', 'o','l','a','s'};
        char[] apell = {'o', 't', 'a', 'l','o','r','a'};

        char[] n = {'c', 'a', 'm', 'i','l','o'};
        char[] a = {'l', 'o', 'p', 'e', 'z'};

        char[] nn = {'l', 'u', 'i', 's'};
        char[] aa = {'p', 'e', 'r', 'e', 'z'};

        EstudianteVO est1 = new EstudianteVO(nombre, apell, 1018, 321338);
        EstudianteVO est2 = new EstudianteVO(n, a, 1019, 314442);
        EstudianteVO est3 = new EstudianteVO(nn, aa, 1020, 311665);
        
        dao.inscribirEstudiante(est1);
        dao.inscribirEstudiante(est2);
        dao.inscribirEstudiante(est3);
        
        ArrayList<EstudianteVO> ests = dao.listarEstudiantes();
        
        for (EstudianteVO est : ests) {
            
            System.out.println("Id: "+est.getId());
            System.out.println("Nombre: "+String.valueOf(est.getNombre()));
            System.out.println("Apellido: "+String.valueOf(est.getApellido()));
            System.out.println("Telefono: "+est.getTelefono());
            
        }
        
        
    }
    
    
    
    
}
