import java.util.*;

public class StrassenThread {
    public static void main(String[] args) {
        int L = 1000;
        int Lpow2 = 1;
        while (Lpow2 < L) {
            Lpow2 *= 2;
        }
        double[][] A = new double[Lpow2][Lpow2];
        double[][] B = new double[Lpow2][Lpow2];
        double[][] C = new double[Lpow2][Lpow2];
        for (int i = 0; i < Lpow2; i++) {
            for (int j = 0; j < Lpow2; j++) {
                if (i < L && j < L) {
                    A[i][j] = new Random().nextInt(10) + 1;
                    B[i][j] = new Random().nextInt(10) + 1;
                } 
                else {
                    A[i][j] = 0;
                    B[i][j] = 0;
                }
            }
        }
        double start = System.currentTimeMillis();
        C = strassen(A, B);
        double end = System.currentTimeMillis();
        // System.out.println("Matrix A:");
        // printMatrix(A,L);
        // System.out.println("Matrix B:");
        // printMatrix(B,L);
        // System.out.println("Matrix C:");
        // printMatrix(C,L);
        System.out.println("Order: " + L + "\nStrassen order : " + Lpow2);
        System.out.println("Time: " + (end - start) + " ms");
    }

    public static void printMatrix(double[][] matrix,int L) {
        for (int i = 0; i < L; i++) {
            for (int j = 0; j < L; j++)
                System.out.print((((float)matrix[i][j])) + " ");
            System.out.println();
        }
    }

    public static double[][] strassen(double[][] a, double[][] b) {
        int n = a.length;
        double[][] result = new double[n][n];
        if (n == 1) {
            result[0][0] = a[0][0] * b[0][0];
        }
        else if (n <= 64){
            return multiplyDirect(a, b);
        }
        double[][] A11 = new double[n/2][n/2];
        double[][] A12 = new double[n/2][n/2];
        double[][] A21 = new double[n/2][n/2];
        double[][] A22 = new double[n/2][n/2];
        double[][] B11 = new double[n/2][n/2];
        double[][] B12 = new double[n/2][n/2];
        double[][] B21 = new double[n/2][n/2];
        double[][] B22 = new double[n/2][n/2];

        // Dividimos las matrices en 4 partes
        split(a, A11, 0, 0);
        split(a, A12, 0, n/2);
        split(a, A21, n/2, 0);
        split(a, A22, n/2, n/2);
        split(b, B11, 0, 0);
        split(b, B12, 0, n/2);
        split(b, B21, n/2, 0);
        split(b, B22, n/2, n/2);

        // Pasos del algortimo de Strassen recursivo, se crean 7 threads para cada paso
        double[][][] Ms = new double[7][][];
        Thread[] threads = new Thread[7];

        // M1 = (A11 + A22) * (B11 + B22)
        threads[0] = new Thread(() -> Ms[0] = strassen(add(A11, A22), add(B11, B22)));
        // M2 = (A21 + A22) * B11
        threads[1] = new Thread(() -> Ms[1] = strassen(add(A21, A22), B11));
        // M3 = A11 * (B12 - B22)
        threads[2] = new Thread(() -> Ms[2] = strassen(A11, sub(B12, B22)));
        // M4 = A22 * (B21 - B11)
        threads[3] = new Thread(() -> Ms[3] = strassen(A22, sub(B21, B11)));
        // M5 = (A11 + A12) * B22
        threads[4] = new Thread(() -> Ms[4] = strassen(add(A11, A12), B22));
        // M6 = (A21 - A11) * (B11 + B12)
        threads[5] = new Thread(() -> Ms[5] = strassen(sub(A21, A11), add(B11, B12)));
        // M7 = (A12 - A22) * (B21 + B22)
        threads[6] = new Thread(() -> Ms[6] = strassen(sub(A12, A22), add(B21, B22)));

        // Iniciamos los threads
        for (Thread t : threads) t.start();
        // Esperamos a que terminen
        try {
            for (Thread t : threads) t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // Calculamos los valores de C
        
        double[][] C11 = add(sub(add(Ms[0], Ms[3]), Ms[4]), Ms[6]);
        double[][] C12 = add(Ms[2], Ms[4]);
        double[][] C21 = add(Ms[1], Ms[3]);
        double[][] C22 = add(sub(add(Ms[0], Ms[2]), Ms[1]), Ms[5]);

        join(C11, result, 0, 0);
        join(C12, result, 0, n/2);
        join(C21, result, n/2, 0);
        join(C22, result, n/2, n/2);

        return result;
        }
    public static void split(double[][] P, double[][] C, int iB, int jB) {
        int i2 = iB;
        for (int i1 = 0; i1 < C.length; i1++) {
            int j2 = jB;
            for (int j1 = 0; j1 < C.length; j1++) {
                C[i1][j1] = P[i2][j2];
                j2++;
            }
            i2++;
        }
    }
    
    public static double[][] add(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                c[i][j] = a[i][j] + b[i][j];
        }
        return c;
    }
    
    public static double[][] sub(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                c[i][j] = a[i][j] - b[i][j];
        }
        return c;
    }
    
    public static void join(double[][] P, double[][] C, int iB, int jB) {
        int i2 = iB;
        for (int i1 = 0; i1 < P.length; i1++) {
            int j2 = jB;
            for (int j1 = 0; j1 < P.length; j1++) {
                C[i2][j2] = P[i1][j1];
                j2++;
            }
            i2++;
        }
    }
    public static double[][] multiplyDirect(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;
    
        if (colsA != rowsB) {
            throw new IllegalArgumentException("Las matrices no son multiplicables");
        }
    
        double[][] result = new double[rowsA][colsB];
        
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
    
        return result;
    }
}
