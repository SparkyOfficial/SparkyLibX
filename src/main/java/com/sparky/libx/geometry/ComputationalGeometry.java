package com.sparky.libx.geometry;

import java.util.*;

import com.sparky.libx.math.Vector3D;

/**
 * обчислювальна геометрія
 * включає алгоритми для роботи з точками, лініями, багатокутниками, опуклими оболонками і багатогранниками
 * @author Андрій Будильников
 */
public class ComputationalGeometry {
    
    /**
     * точка в 2D просторі
     */
    public static class Point2D {
        public final double x, y;
        
        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double distanceTo(Point2D other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point2D point2D = (Point2D) obj;
            return Double.compare(point2D.x, x) == 0 && Double.compare(point2D.y, y) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
        
        @Override
        public String toString() {
            return String.format("Point2D(%.2f, %.2f)", x, y);
        }
    }
    
    /**
     * лінія в 2D просторі
     */
    public static class Line2D {
        public final Point2D start;
        public final Point2D end;
        
        public Line2D(Point2D start, Point2D end) {
            this.start = start;
            this.end = end;
        }
        
        /**
         * отримати довжину лінії
         */
        public double length() {
            return start.distanceTo(end);
        }
        
        /**
         * перевірити чи дві лінії перетинаються
         */
        public boolean intersects(Line2D other) {
            return intersect(this, other) != null;
        }
        
        @Override
        public String toString() {
            return String.format("Line2D[%s -> %s]", start, end);
        }
    }
    
    /**
     * багатокутник
     */
    public static class Polygon {
        private final List<Point2D> vertices;
        
        public Polygon(List<Point2D> vertices) {
            if (vertices == null || vertices.size() < 3) {
                throw new IllegalArgumentException("Багатокутник повинен мати щонайменше 3 вершини");
            }
            this.vertices = new ArrayList<>(vertices);
        }
        
        /**
         * отримати вершини багатокутника
         */
        public List<Point2D> getVertices() {
            return new ArrayList<>(vertices);
        }
        
        /**
         * отримати кількість вершин
         */
        public int getVertexCount() {
            return vertices.size();
        }
        
        /**
         * обчислити площу багатокутника формулою шнайдера
         */
        public double area() {
            double area = 0;
            int n = vertices.size();
            
            for (int i = 0; i < n; i++) {
                Point2D current = vertices.get(i);
                Point2D next = vertices.get((i + 1) % n);
                area += current.x * next.y - next.x * current.y;
            }
            
            return Math.abs(area) / 2.0;
        }
        
        /**
         * обчислити периметр багатокутника
         */
        public double perimeter() {
            double perimeter = 0;
            int n = vertices.size();
            
            for (int i = 0; i < n; i++) {
                Point2D current = vertices.get(i);
                Point2D next = vertices.get((i + 1) % n);
                perimeter += current.distanceTo(next);
            }
            
            return perimeter;
        }
        
        /**
         * перевірити чи точка знаходиться всередині багатокутника
         */
        public boolean contains(Point2D point) {
            int windingNumber = 0;
            int n = vertices.size();
            
            for (int i = 0; i < n; i++) {
                Point2D current = vertices.get(i);
                Point2D next = vertices.get((i + 1) % n);
                
                if (current.y <= point.y) {
                    if (next.y > point.y && isLeft(current, next, point) > 0) {
                        windingNumber++;
                    }
                } else {
                    if (next.y <= point.y && isLeft(current, next, point) < 0) {
                        windingNumber--;
                    }
                }
            }
            
            return windingNumber != 0;
        }
        
        /**
         * перевірити орієнтацію трьох точок
         */
        private double isLeft(Point2D p0, Point2D p1, Point2D p2) {
            return (p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y);
        }
        
        /**
         * перевірити чи багатокутник опуклий
         */
        public boolean isConvex() {
            int n = vertices.size();
            if (n < 3) return false;
            
            boolean sign = false;
            for (int i = 0; i < n; i++) {
                Point2D p1 = vertices.get(i);
                Point2D p2 = vertices.get((i + 1) % n);
                Point2D p3 = vertices.get((i + 2) % n);
                
                double crossProduct = crossProduct(p1, p2, p3);
                if (i == 0) {
                    sign = crossProduct > 0;
                } else {
                    if ((crossProduct > 0) != sign) {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        /**
         * обчислити векторний добуток трьох точок
         */
        private double crossProduct(Point2D a, Point2D b, Point2D c) {
            return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
        }
        
        @Override
        public String toString() {
            return String.format("Polygon%s", vertices);
        }
    }
    
    /**
     * знайти точку перетину двох ліній
     * @param line1 перша лінія
     * @param line2 друга лінія
     * @return точка перетину або null якщо лінії не перетинаються
     */
    public static Point2D intersect(Line2D line1, Line2D line2) {
        double x1 = line1.start.x, y1 = line1.start.y;
        double x2 = line1.end.x, y2 = line1.end.y;
        double x3 = line2.start.x, y3 = line2.start.y;
        double x4 = line2.end.x, y4 = line2.end.y;
        
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) {
            return null; // лінії паралельні
        }
        
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
        double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;
        
        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            double x = x1 + t * (x2 - x1);
            double y = y1 + t * (y2 - y1);
            return new Point2D(x, y);
        }
        
        return null; // лінії не перетинаються в межах відрізків
    }
    
    /**
     * знайти опуклу оболонку множини точок алгоритмом Грехема
     * @param points множина точок
     * @return точки опуклої оболонки
     */
    public static List<Point2D> convexHull(List<Point2D> points) {
        if (points == null || points.size() < 3) {
            return new ArrayList<>(points);
        }
        
        // знайти точку з найменшою y-координатою (і найменшою x при однакових y)
        Point2D pivot = points.get(0);
        for (Point2D point : points) {
            if (point.y < pivot.y || (point.y == pivot.y && point.x < pivot.x)) {
                pivot = point;
            }
        }
        
        final Point2D finalPivot = pivot; // зробити ефективно фінальною для лямбда-виразів
        
        // сортувати точки за полярним кутом відносно опорної точки
        List<Point2D> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort((a, b) -> {
            double angleA = Math.atan2(a.y - finalPivot.y, a.x - finalPivot.x);
            double angleB = Math.atan2(b.y - finalPivot.y, b.x - finalPivot.x);
            if (angleA != angleB) {
                return Double.compare(angleA, angleB);
            }
            // якщо кути однакові, сортувати за відстанню
            return Double.compare(finalPivot.distanceTo(a), finalPivot.distanceTo(b));
        });
        
        // побудувати опуклу оболонку
        Stack<Point2D> hull = new Stack<>();
        for (Point2D point : sortedPoints) {
            while (hull.size() > 1 && crossProduct(hull.get(hull.size() - 2), hull.peek(), point) <= 0) {
                hull.pop();
            }
            hull.push(point);
        }
        
        return new ArrayList<>(hull);
    }
    
    /**
     * обчислити векторний добуток трьох точок
     */
    private static double crossProduct(Point2D a, Point2D b, Point2D c) {
        return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
    }
    
    /**
     * знайти мінімальний обмежувальний прямокутник для множини точок
     * @param points множина точок
     * @return масив з чотирьох точок прямокутника [minXminY, maxXminY, maxXmaxY, minXmaxY]
     */
    public static Point2D[] boundingRectangle(List<Point2D> points) {
        if (points == null || points.isEmpty()) {
            return new Point2D[0];
        }
        
        double minX = points.get(0).x;
        double maxX = minX;
        double minY = points.get(0).y;
        double maxY = minY;
        
        for (Point2D point : points) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        
        return new Point2D[] {
            new Point2D(minX, minY),
            new Point2D(maxX, minY),
            new Point2D(maxX, maxY),
            new Point2D(minX, maxY)
        };
    }
    
    /**
     * знайти центр мас множини точок
     * @param points множина точок
     * @return центр мас
     */
    public static Point2D centroid(List<Point2D> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Список точок не може бути порожнім");
        }
        
        double sumX = 0;
        double sumY = 0;
        
        for (Point2D point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        
        int n = points.size();
        return new Point2D(sumX / n, sumY / n);
    }
    
    /**
     * обчислити відстань від точки до лінії
     * @param point точка
     * @param line лінія
     * @return відстань
     */
    public static double distancePointToLine(Point2D point, Line2D line) {
        double x = point.x;
        double y = point.y;
        double x1 = line.start.x;
        double y1 = line.start.y;
        double x2 = line.end.x;
        double y2 = line.end.y;
        
        // формула відстані від точки до лінії
        double numerator = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1);
        double denominator = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        
        return numerator / denominator;
    }
    
    /**
     * знайти найближчу пару точок методом розділяй і володарюй
     * @param points множина точок
     * @return пара найближчих точок
     */
    public static Point2D[] closestPair(List<Point2D> points) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Потрібно щонайменше дві точки");
        }
        
        if (points.size() == 2) {
            return new Point2D[] {points.get(0), points.get(1)};
        }
        
        // сортувати точки за x-координатою
        List<Point2D> sortedByX = new ArrayList<>(points);
        sortedByX.sort(Comparator.comparingDouble(p -> p.x));
        
        // сортувати точки за y-координатою
        List<Point2D> sortedByY = new ArrayList<>(points);
        sortedByY.sort(Comparator.comparingDouble(p -> p.y));
        
        return closestPairRecursive(sortedByX, sortedByY);
    }
    
    private static Point2D[] closestPairRecursive(List<Point2D> pointsX, List<Point2D> pointsY) {
        int n = pointsX.size();
        
        if (n <= 3) {
            // для малої кількості точок використовуємо грубою силу
            double minDistance = Double.MAX_VALUE;
            Point2D[] closestPair = new Point2D[2];
            
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double distance = pointsX.get(i).distanceTo(pointsX.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestPair[0] = pointsX.get(i);
                        closestPair[1] = pointsX.get(j);
                    }
                }
            }
            
            return closestPair;
        }
        
        // розділити точки на дві половини
        int mid = n / 2;
        Point2D midpoint = pointsX.get(mid);
        
        List<Point2D> leftX = pointsX.subList(0, mid);
        List<Point2D> rightX = pointsX.subList(mid, n);
        
        List<Point2D> leftY = new ArrayList<>();
        List<Point2D> rightY = new ArrayList<>();
        
        for (Point2D point : pointsY) {
            if (point.x <= midpoint.x) {
                leftY.add(point);
            } else {
                rightY.add(point);
            }
        }
        
        // рекурсивно знайти найближчі пари в кожній половині
        Point2D[] leftPair = closestPairRecursive(leftX, leftY);
        Point2D[] rightPair = closestPairRecursive(rightX, rightY);
        
        // знайти мінімальну відстань
        double leftDistance = leftPair[0].distanceTo(leftPair[1]);
        double rightDistance = rightPair[0].distanceTo(rightPair[1]);
        double minDistance = Math.min(leftDistance, rightDistance);
        Point2D[] minPair = leftDistance < rightDistance ? leftPair : rightPair;
        
        // перевірити точки біля лінії розділення
        List<Point2D> strip = new ArrayList<>();
        for (Point2D point : pointsY) {
            if (Math.abs(point.x - midpoint.x) < minDistance) {
                strip.add(point);
            }
        }
        
        Point2D[] stripPair = closestInStrip(strip, minDistance);
        if (stripPair[0] != null) {
            return stripPair;
        }
        
        return minPair;
    }
    
    private static Point2D[] closestInStrip(List<Point2D> strip, double minDistance) {
        Point2D[] closestPair = new Point2D[2];
        double minDist = minDistance;
        
        for (int i = 0; i < strip.size(); i++) {
            for (int j = i + 1; j < strip.size() && (strip.get(j).y - strip.get(i).y) < minDist; j++) {
                double distance = strip.get(i).distanceTo(strip.get(j));
                if (distance < minDist) {
                    minDist = distance;
                    closestPair[0] = strip.get(i);
                    closestPair[1] = strip.get(j);
                }
            }
        }
        
        return closestPair;
    }
    
    /**
     * перевірити чи дві лінії паралельні
     * @param line1 перша лінія
     * @param line2 друга лінія
     * @return true якщо лінії паралельні
     */
    public static boolean areParallel(Line2D line1, Line2D line2) {
        double dx1 = line1.end.x - line1.start.x;
        double dy1 = line1.end.y - line1.start.y;
        double dx2 = line2.end.x - line2.start.x;
        double dy2 = line2.end.y - line2.start.y;
        
        // лінії паралельні якщо їх напрямні вектори колінеарні
        return Math.abs(dx1 * dy2 - dx2 * dy1) < 1e-10;
    }
    
    /**
     * знайти точку перетину двох прямих (не обмежених відрізками)
     * @param line1 перша пряма
     * @param line2 друга пряма
     * @return точка перетину або null якщо прямі паралельні
     */
    public static Point2D intersectLines(Line2D line1, Line2D line2) {
        double x1 = line1.start.x, y1 = line1.start.y;
        double x2 = line1.end.x, y2 = line1.end.y;
        double x3 = line2.start.x, y3 = line2.start.y;
        double x4 = line2.end.x, y4 = line2.end.y;
        
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) {
            return null; // прямі паралельні
        }
        
        double x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;
        
        return new Point2D(x, y);
    }
    
    /**
     * обчислити кут між двома векторами
     * @param a перший вектор
     * @param b другий вектор
     * @return кут в радіанах
     */
    public static double angleBetweenVectors(Point2D a, Point2D b) {
        double dotProduct = a.x * b.x + a.y * b.y;
        double magnitudeA = Math.sqrt(a.x * a.x + a.y * a.y);
        double magnitudeB = Math.sqrt(b.x * b.x + b.y * b.y);
        
        if (magnitudeA == 0 || magnitudeB == 0) {
            return 0;
        }
        
        double cosTheta = dotProduct / (magnitudeA * magnitudeB);
        // обмежити значення діапазоном [-1, 1] через можливі помилки округлення
        cosTheta = Math.max(-1, Math.min(1, cosTheta));
        
        return Math.acos(cosTheta);
    }
    
    /**
     * знайти площу трикутника за трьома точками
     * @param a перша точка
     * @param b друга точка
     * @param c третя точка
     * @return площа трикутника
     */
    public static double triangleArea(Point2D a, Point2D b, Point2D c) {
        return Math.abs((a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2.0);
    }
    
    /**
     * перевірити чи точки лежать на одній прямій
     * @param points список точок
     * @return true якщо всі точки колінеарні
     */
    public static boolean areCollinear(List<Point2D> points) {
        if (points.size() < 3) {
            return true;
        }
        
        Point2D p1 = points.get(0);
        Point2D p2 = points.get(1);
        
        for (int i = 2; i < points.size(); i++) {
            Point2D p3 = points.get(i);
            // перевірити чи векторний добуток дорівнює нулю
            if (Math.abs(crossProduct(p1, p2, p3)) > 1e-10) {
                return false;
            }
        }
        
        return true;
    }
}