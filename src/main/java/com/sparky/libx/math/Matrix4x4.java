package com.sparky.libx.math;

/**
 * 4x4 матрица для 3D преобразований
 */
public class Matrix4x4 {
    private final double[][] elements;
    
    /**
     * Создает новую единичную матрицу
     */
    public Matrix4x4() {
        elements = new double[4][4];
        setIdentity();
    }
    
    /**
     * Создает новую матрицу с заданными элементами
     */
    public Matrix4x4(double[][] elements) {
        if (elements.length != 4 || elements[0].length != 4) {
            throw new IllegalArgumentException("Матрица должна быть 4x4");
        }
        
        this.elements = new double[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(elements[i], 0, this.elements[i], 0, 4);
        }
    }
    
    /**
     * Устанавливает матрицу как единичную
     */
    public void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                elements[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
    }
    
    /**
     * Получает элемент матрицы по индексам
     */
    public double get(int row, int col) {
        return elements[row][col];
    }
    
    /**
     * Устанавливает элемент матрицы по индексам
     */
    public void set(int row, int col, double value) {
        elements[row][col] = value;
    }
    
    /**
     * Складывает две матрицы
     */
    public Matrix4x4 add(Matrix4x4 other) {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.elements[i][j] = this.elements[i][j] + other.elements[i][j];
            }
        }
        return result;
    }
    
    /**
     * Вычитает одну матрицу из другой
     */
    public Matrix4x4 subtract(Matrix4x4 other) {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.elements[i][j] = this.elements[i][j] - other.elements[i][j];
            }
        }
        return result;
    }
    
    /**
     * Умножает матрицу на скаляр
     */
    public Matrix4x4 multiply(double scalar) {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.elements[i][j] = this.elements[i][j] * scalar;
            }
        }
        return result;
    }
    
    /**
     * Умножает две матрицы
     */
    public Matrix4x4 multiply(Matrix4x4 other) {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += this.elements[i][k] * other.elements[k][j];
                }
                result.elements[i][j] = sum;
            }
        }
        return result;
    }
    
    /**
     * Транспонирует матрицу
     */
    public Matrix4x4 transpose() {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.elements[i][j] = this.elements[j][i];
            }
        }
        return result;
    }
    
    /**
     * Создает матрицу масштабирования
     */
    public static Matrix4x4 createScale(double sx, double sy, double sz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.elements[0][0] = sx;
        matrix.elements[1][1] = sy;
        matrix.elements[2][2] = sz;
        return matrix;
    }
    
    /**
     * Создает матрицу переноса
     */
    public static Matrix4x4 createTranslation(double tx, double ty, double tz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.elements[0][3] = tx;
        matrix.elements[1][3] = ty;
        matrix.elements[2][3] = tz;
        return matrix;
    }
    
    /**
     * Создает матрицу поворота вокруг оси X
     */
    public static Matrix4x4 createRotationX(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.elements[1][1] = cos;
        matrix.elements[1][2] = -sin;
        matrix.elements[2][1] = sin;
        matrix.elements[2][2] = cos;
        return matrix;
    }
    
    /**
     * Создает матрицу поворота вокруг оси Y
     */
    public static Matrix4x4 createRotationY(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.elements[0][0] = cos;
        matrix.elements[0][2] = sin;
        matrix.elements[2][0] = -sin;
        matrix.elements[2][2] = cos;
        return matrix;
    }
    
    /**
     * Создает матрицу поворота вокруг оси Z
     */
    public static Matrix4x4 createRotationZ(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.elements[0][0] = cos;
        matrix.elements[0][1] = -sin;
        matrix.elements[1][0] = sin;
        matrix.elements[1][1] = cos;
        return matrix;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix4x4:\n");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%10.3f ", elements[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}