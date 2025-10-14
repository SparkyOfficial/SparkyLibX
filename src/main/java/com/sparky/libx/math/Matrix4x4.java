package com.sparky.libx.math;

/**
 * Класс для работы с матрицами 4x4
 * Используется для трехмерных преобразований
 */
public class Matrix4x4 {
    private final double[][] matrix;
    
    /**
     * Создает единичную матрицу 4x4
     */
    public Matrix4x4() {
        matrix = new double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
    }
    
    /**
     * Создает матрицу из двумерного массива
     * @param values массив 4x4
     * @throws IllegalArgumentException если массив не 4x4
     */
    public Matrix4x4(double[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Матрица должна быть размером 4x4");
        }
        this.matrix = new double[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, this.matrix[i], 0, 4);
        }
    }
    
    /**
     * Умножение матриц
     * @param other другая матрица 4x4
     * @return новая матрица - результат умножения
     */
    public Matrix4x4 multiply(Matrix4x4 other) {
        double[][] result = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        return new Matrix4x4(result);
    }
    
    /**
     * Умножение матрицы на вектор
     * @param vector вектор [x, y, z, w]
     * @return новый вектор - результат умножения
     */
    public double[] multiplyVector(double[] vector) {
        if (vector.length != 4) {
            throw new IllegalArgumentException("Вектор должен иметь длину 4");
        }
        
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }
    
    /**
     * Создает матрицу поворота вокруг оси X
     * @param angle угол в радианах
     * @return матрица поворота
     */
    public static Matrix4x4 rotationX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        return new Matrix4x4(new double[][]{
            {1, 0, 0, 0},
            {0, cos, -sin, 0},
            {0, sin, cos, 0},
            {0, 0, 0, 1}
        });
    }
    
    /**
     * Создает матрицу поворота вокруг оси Y
     * @param angle угол в радианах
     * @return матрица поворота
     */
    public static Matrix4x4 rotationY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        return new Matrix4x4(new double[][]{
            {cos, 0, sin, 0},
            {0, 1, 0, 0},
            {-sin, 0, cos, 0},
            {0, 0, 0, 1}
        });
    }
    
    /**
     * Создает матрицу поворота вокруг оси Z
     * @param angle угол в радианах
     * @return матрица поворота
     */
    public static Matrix4x4 rotationZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        return new Matrix4x4(new double[][]{
            {cos, -sin, 0, 0},
            {sin, cos, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        });
    }
    
    /**
     * Создает матрицу масштабирования
     * @param scaleX масштаб по X
     * @param scaleY масштаб по Y
     * @param scaleZ масштаб по Z
     * @return матрица масштабирования
     */
    public static Matrix4x4 scale(double scaleX, double scaleY, double scaleZ) {
        return new Matrix4x4(new double[][]{
            {scaleX, 0, 0, 0},
            {0, scaleY, 0, 0},
            {0, 0, scaleZ, 0},
            {0, 0, 0, 1}
        });
    }
    
    /**
     * Создает матрицу переноса
     * @param dx смещение по X
     * @param dy смещение по Y
     * @param dz смещение по Z
     * @return матрица переноса
     */
    public static Matrix4x4 translate(double dx, double dy, double dz) {
        return new Matrix4x4(new double[][]{
            {1, 0, 0, dx},
            {0, 1, 0, dy},
            {0, 0, 1, dz},
            {0, 0, 0, 1}
        });
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%8.4f", matrix[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]");
            if (i < 3) sb.append("\n");
        }
        return sb.toString();
    }
    

    public double get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индексы должны быть в диапазоне [0, 3]");
        }
        return matrix[row][col];
    }
    
    public void set(int row, int col, double value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индексы должны быть в диапазоне [0, 3]");
        }
        matrix[row][col] = value;
    }
}
