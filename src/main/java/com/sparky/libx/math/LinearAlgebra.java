package com.sparky.libx.math;

import java.util.Arrays;

/**
 * утиліти для лінійної алгебри
 * включає операції з матрицями, векторами, вирішення систем лінійних рівнянь і власні значення
 * @author Андрій Будильников
 */
public class LinearAlgebra {
    
    /**
     * обчислити визначник матриці 2x2
     * @param matrix матриця 2x2
     * @return визначник
     */
    public static double determinant2x2(double[][] matrix) {
        validateMatrix(matrix, 2, 2);
        return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
    }
    
    /**
     * обчислити визначник матриці 3x3
     * @param matrix матриця 3x3
     * @return визначник
     */
    public static double determinant3x3(double[][] matrix) {
        validateMatrix(matrix, 3, 3);
        
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
             - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
             + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }
    
    /**
     * обчислити визначник матриці методом розкладання за рядком
     * @param matrix квадратна матриця
     * @return визначник
     */
    public static double determinant(double[][] matrix) {
        validateSquareMatrix(matrix);
        
        int n = matrix.length;
        if (n == 1) {
            return matrix[0][0];
        }
        if (n == 2) {
            return determinant2x2(matrix);
        }
        if (n == 3) {
            return determinant3x3(matrix);
        }
        
        // для більших матриць використовуємо розкладання за першим рядком
        double det = 0;
        for (int j = 0; j < n; j++) {
            double[][] minor = getMinor(matrix, 0, j);
            det += Math.pow(-1, j) * matrix[0][j] * determinant(minor);
        }
        return det;
    }
    
    /**
     * отримати мінор матриці (матриця без i-го рядка і j-го стовпця)
     * @param matrix матриця
     * @param i індекс рядка для видалення
     * @param j індекс стовпця для видалення
     * @return мінор
     */
    public static double[][] getMinor(double[][] matrix, int i, int j) {
        int n = matrix.length;
        double[][] minor = new double[n-1][n-1];
        
        int minorRow = 0;
        for (int row = 0; row < n; row++) {
            if (row == i) continue;
            
            int minorCol = 0;
            for (int col = 0; col < n; col++) {
                if (col == j) continue;
                minor[minorRow][minorCol] = matrix[row][col];
                minorCol++;
            }
            minorRow++;
        }
        
        return minor;
    }
    
    /**
     * обчислити обернену матрицю
     * @param matrix квадратна матриця
     * @return обернена матриця
     */
    public static double[][] inverse(double[][] matrix) {
        validateSquareMatrix(matrix);
        
        int n = matrix.length;
        double det = determinant(matrix);
        
        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Матриця сингулярна (визначник = 0), обернена матриця не існує");
        }
        
        double[][] inverse = new double[n][n];
        
        if (n == 1) {
            inverse[0][0] = 1.0 / matrix[0][0];
            return inverse;
        }
        
        if (n == 2) {
            inverse[0][0] = matrix[1][1] / det;
            inverse[0][1] = -matrix[0][1] / det;
            inverse[1][0] = -matrix[1][0] / det;
            inverse[1][1] = matrix[0][0] / det;
            return inverse;
        }
        
        // для більших матриць використовуємо алгебраїчні доповнення
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double[][] minor = getMinor(matrix, i, j);
                double cofactor = Math.pow(-1, i + j) * determinant(minor);
                inverse[j][i] = cofactor / det; // транспонуємо
            }
        }
        
        return inverse;
    }
    
    /**
     * перемножити дві матриці
     * @param a перша матриця
     * @param b друга матриця
     * @return результат множення
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        validateMatrix(a);
        validateMatrix(b);
        
        if (a[0].length != b.length) {
            throw new IllegalArgumentException("Кількість стовпців першої матриці повинна дорівнювати кількості рядків другої матриці");
        }
        
        int rows = a.length;
        int cols = b[0].length;
        int common = a[0].length;
        
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < common; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        
        return result;
    }
    
    /**
     * додати дві матриці
     * @param a перша матриця
     * @param b друга матриця
     * @return результат додавання
     */
    public static double[][] add(double[][] a, double[][] b) {
        validateSameSizeMatrices(a, b);
        
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * відняти одну матрицю від іншої
     * @param a перша матриця
     * @param b друга матриця
     * @return результат віднімання
     */
    public static double[][] subtract(double[][] a, double[][] b) {
        validateSameSizeMatrices(a, b);
        
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * помножити матрицю на скаляр
     * @param matrix матриця
     * @param scalar скаляр
     * @return результат множення
     */
    public static double[][] multiplyScalar(double[][] matrix, double scalar) {
        validateMatrix(matrix);
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        
        return result;
    }
    
    /**
     * транспонувати матрицю
     * @param matrix матриця
     * @return транспонована матриця
     */
    public static double[][] transpose(double[][] matrix) {
        validateMatrix(matrix);
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * вирішити систему лінійних рівнянь Ax = b методом Крамера
     * @param a коефіцієнти (квадратна матриця)
     * @param b вектор правих частин
     * @return вектор розв'язків
     */
    public static double[] solveCramer(double[][] a, double[] b) {
        validateSquareMatrix(a);
        validateVector(b, a.length);
        
        int n = a.length;
        double detA = determinant(a);
        
        if (Math.abs(detA) < 1e-10) {
            throw new IllegalArgumentException("Система не має єдиного розв'язку (визначник = 0)");
        }
        
        double[] solution = new double[n];
        
        for (int i = 0; i < n; i++) {
            double[][] modifiedMatrix = replaceColumn(a, b, i);
            solution[i] = determinant(modifiedMatrix) / detA;
        }
        
        return solution;
    }
    
    /**
     * замінити стовпець матриці вектором
     * @param matrix матриця
     * @param vector вектор
     * @param columnIndex індекс стовпця для заміни
     * @return нова матриця з заміненим стовпцем
     */
    public static double[][] replaceColumn(double[][] matrix, double[] vector, int columnIndex) {
        validateMatrix(matrix);
        validateVector(vector, matrix.length);
        validateColumnIndex(matrix, columnIndex);
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j == columnIndex) {
                    result[i][j] = vector[i];
                } else {
                    result[i][j] = matrix[i][j];
                }
            }
        }
        
        return result;
    }
    
    /**
     * обчислити скалярний добуток двох векторів
     * @param a перший вектор
     * @param b другий вектор
     * @return скалярний добуток
     */
    public static double dotProduct(double[] a, double[] b) {
        validateVector(a);
        validateVector(b);
        
        if (a.length != b.length) {
            throw new IllegalArgumentException("Вектори повинні мати однакову довжину");
        }
        
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        
        return result;
    }
    
    /**
     * обчислити векторний добуток двох 3D векторів
     * @param a перший вектор
     * @param b другий вектор
     * @return векторний добуток
     */
    public static double[] crossProduct(double[] a, double[] b) {
        validateVector(a, 3);
        validateVector(b, 3);
        
        double[] result = new double[3];
        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];
        
        return result;
    }
    
    /**
     * обчислити магнітуду (довжину) вектора
     * @param vector вектор
     * @return магнітуда
     */
    public static double magnitude(double[] vector) {
        validateVector(vector);
        
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        
        return Math.sqrt(sum);
    }
    
    /**
     * нормалізувати вектор (зробити одиничної довжини)
     * @param vector вектор
     * @return нормалізований вектор
     */
    public static double[] normalize(double[] vector) {
        validateVector(vector);
        
        double mag = magnitude(vector);
        if (mag == 0) {
            throw new IllegalArgumentException("Неможливо нормалізувати нульовий вектор");
        }
        
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] / mag;
        }
        
        return result;
    }
    
    /**
     * обчислити кут між двома векторами в радіанах
     * @param a перший вектор
     * @param b другий вектор
     * @return кут в радіанах
     */
    public static double angleBetween(double[] a, double[] b) {
        validateVector(a);
        validateVector(b);
        
        if (a.length != b.length) {
            throw new IllegalArgumentException("Вектори повинні мати однакову довжину");
        }
        
        double dot = dotProduct(a, b);
        double magA = magnitude(a);
        double magB = magnitude(b);
        
        if (magA == 0 || magB == 0) {
            throw new IllegalArgumentException("Неможливо обчислити кут з нульовим вектором");
        }
        
        double cosTheta = dot / (magA * magB);
        // обмежити значення діапазоном [-1, 1] через можливі помилки округлення
        cosTheta = Math.max(-1, Math.min(1, cosTheta));
        
        return Math.acos(cosTheta);
    }
    
    /**
     * створити одиничну матрицю
     * @param size розмір матриці
     * @return одинична матриця
     */
    public static double[][] identityMatrix(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Розмір матриці повинен бути додатним");
        }
        
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1;
        }
        
        return matrix;
    }
    
    /**
     * створити нульову матрицю
     * @param rows кількість рядків
     * @param cols кількість стовпців
     * @return нульова матриця
     */
    public static double[][] zeroMatrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Розміри матриці повинні бути додатними");
        }
        
        return new double[rows][cols];
    }
    
    /**
     * перевірити чи матриця є симетричною
     * @param matrix матриця
     * @return true якщо матриця симетрична
     */
    public static boolean isSymmetric(double[][] matrix) {
        validateSquareMatrix(matrix);
        
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(matrix[i][j] - matrix[j][i]) > 1e-10) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * перевірити чи матриця є ортогональною
     * @param matrix матриця
     * @return true якщо матриця ортогональна
     */
    public static boolean isOrthogonal(double[][] matrix) {
        validateSquareMatrix(matrix);
        
        double[][] transpose = transpose(matrix);
        double[][] product = multiply(matrix, transpose);
        double[][] identity = identityMatrix(matrix.length);
        
        return matricesEqual(product, identity, 1e-10);
    }
    
    /**
     * перевірити чи дві матриці рівні з точністю до epsilon
     * @param a перша матриця
     * @param b друга матриця
     * @param epsilon точність
     * @return true якщо матриці рівні
     */
    public static boolean matricesEqual(double[][] a, double[][] b, double epsilon) {
        validateSameSizeMatrices(a, b);
        
        int rows = a.length;
        int cols = a[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // валідаційні методи
    
    private static void validateMatrix(double[][] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Матриця не може бути null");
        }
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Матриця не може бути порожньою");
        }
        if (matrix[0] == null) {
            throw new IllegalArgumentException("Рядки матриці не можуть бути null");
        }
        int cols = matrix[0].length;
        if (cols == 0) {
            throw new IllegalArgumentException("Рядки матриці не можуть бути порожніми");
        }
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("Рядки матриці не можуть бути null");
            }
            if (matrix[i].length != cols) {
                throw new IllegalArgumentException("Всі рядки матриці повинні мати однакову довжину");
            }
        }
    }
    
    private static void validateMatrix(double[][] matrix, int expectedRows, int expectedCols) {
        validateMatrix(matrix);
        if (matrix.length != expectedRows) {
            throw new IllegalArgumentException("Матриця повинна мати " + expectedRows + " рядків");
        }
        if (matrix[0].length != expectedCols) {
            throw new IllegalArgumentException("Матриця повинна мати " + expectedCols + " стовпців");
        }
    }
    
    private static void validateSquareMatrix(double[][] matrix) {
        validateMatrix(matrix);
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Матриця повинна бути квадратною");
        }
    }
    
    private static void validateSameSizeMatrices(double[][] a, double[][] b) {
        validateMatrix(a);
        validateMatrix(b);
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("Матриці повинні мати однакові розміри");
        }
    }
    
    private static void validateVector(double[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не може бути null");
        }
        if (vector.length == 0) {
            throw new IllegalArgumentException("Вектор не може бути порожнім");
        }
    }
    
    private static void validateVector(double[] vector, int expectedLength) {
        validateVector(vector);
        if (vector.length != expectedLength) {
            throw new IllegalArgumentException("Вектор повинен мати " + expectedLength + " елементів");
        }
    }
    
    private static void validateColumnIndex(double[][] matrix, int columnIndex) {
        validateMatrix(matrix);
        if (columnIndex < 0 || columnIndex >= matrix[0].length) {
            throw new IllegalArgumentException("Індекс стовпця поза межами діапазону");
        }
    }
}