package com.sparky.libx.pathfinding;

import com.sparky.libx.math.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Класс для поиска пути с использованием алгоритма A*
 */
public class Pathfinder {
    
    private static final double STRAIGHT_COST = 1.0;
    private static final double DIAGONAL_COST = 1.414;
    
    private final World world;
    private final NodeGrid grid;
    private final double stepSize;
    
    /**
     * Создает экземпляр Pathfinder
     * @param world мир, в котором ищется путь
     * @param stepSize размер шага при построении сетки (в блоках)
     */
    public Pathfinder(World world, double stepSize) {
        this.world = world;
        this.stepSize = stepSize;
        this.grid = new NodeGrid(stepSize);
    }
    
    /**
     * Находит путь от начальной до конечной точки
     * @param start начальная точка
     * @param end конечная точка
     * @param maxDistance максимальная дистанция поиска
     * @param avoidLava избегать ли лаву
     * @param maxDrop максимальная высота падения
     * @return список точек пути или пустой список, если путь не найден
     */
    public List<Location> findPath(Location start, Location end, double maxDistance, boolean avoidLava, double maxDrop) {
        if (!start.getWorld().equals(world) || !end.getWorld().equals(world)) {
            return Collections.emptyList();
        }
        
        Node startNode = grid.getNode(start);
        Node endNode = grid.getNode(end);
        
        // Проверка на слишком большое расстояние
        if (start.distance(end) > maxDistance) {
            return Collections.emptyList();
        }
        
        // Проверка на прямую видимость
        if (hasLineOfSight(start, end)) {
            return Collections.singletonList(end);
        }
        
        // Инициализация открытого и закрытого множеств
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Double> gScore = new HashMap<>();
        Map<Node, Double> fScore = new HashMap<>();
        
        // Начальные значения
        gScore.put(startNode, 0.0);
        fScore.put(startNode, heuristicCostEstimate(startNode, endNode));
        openSet.add(startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            // Путь найден
            if (current.equals(endNode)) {
                return reconstructPath(cameFrom, current, start);
            }
            
            closedSet.add(current);
            
            // Перебираем соседей
            for (Node neighbor : getNeighbors(current, avoidLava, maxDrop)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                // Расстояние от start до neighbor через current
                double tentativeGScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + 
                                      distanceBetween(current, neighbor);
                
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    continue; // Это не лучший путь
                }
                
                // Этот путь пока что лучший
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + heuristicCostEstimate(neighbor, endNode));
            }
        }
        
        // Путь не найден
        return Collections.emptyList();
    }
    
    /**
     * Проверяет, есть ли прямая видимость между точками
     */
    private boolean hasLineOfSight(Location from, Location to) {
        // Упрощенная проверка прямой видимости
        // В реальной реализации следует использовать RayCasting
        return from.getWorld().rayTraceBlocks(from, to.toVector().subtract(from.toVector())) == null;
    }
    
    /**
     * Восстанавливает путь от конечной точки до начальной
     */
    private List<Location> reconstructPath(Map<Node, Node> cameFrom, Node current, Location start) {
        List<Location> path = new ArrayList<>();
        path.add(current.getLocation(world));
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current.getLocation(world));
        }
        
        // Удаляем начальную точку, если она совпадает с исходной
        if (!path.isEmpty() && path.get(0).distanceSquared(start) < 0.1) {
            path.remove(0);
        }
        
        return path;
    }
    
    /**
     * Получает список соседних узлов
     */
    private List<Node> getNeighbors(Node node, boolean avoidLava, double maxDrop) {
        List<Node> neighbors = new ArrayList<>();
        
        // Проверяем все 8 направлений
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                // Пропускаем текущую точку
                if (dx == 0 && dz == 0) continue;
                
                // Вычисляем координаты соседа
                double x = node.getX() + dx * stepSize;
                double z = node.getZ() + dz * stepSize;
                
                // Находим поверхность в этой точке
                double y = findSurface(x, z, node.getY(), maxDrop);
                
                // Если поверхность не найдена, пропускаем
                if (y == Double.NEGATIVE_INFINITY) continue;
                
                // Проверяем, можно ли стоять на этом блоке
                if (!isWalkable(x, y, z, avoidLava)) continue;
                
                // Проверяем, можно ли дойти до этого блока
                if (!canMoveTo(x, y, z, node.getY(), maxDrop)) continue;
                
                neighbors.add(new Node(x, y, z));
            }
        }
        
        return neighbors;
    }
    
    /**
     * Находит поверхность в указанных координатах
     */
    private double findSurface(double x, double z, double startY, double maxDrop) {
        // Начинаем поиск с текущей высоты
        int blockY = (int) Math.round(startY);
        
        // Ищем поверхность сверху вниз
        for (int y = blockY + 1; y >= blockY - maxDrop; y--) {
            Block block = world.getBlockAt((int) x, y, (int) z);
            Block below = block.getRelative(BlockFace.DOWN);
            
            // Если блок под нами твердый, а текущий блок проходимый
            if (below.getType().isSolid() && !block.getType().isSolid()) {
                return y;
            }
        }
        
        return Double.NEGATIVE_INFINITY; // Поверхность не найдена
    }
    
    /**
     * Проверяет, можно ли стоять на блоке
     */
    private boolean isWalkable(double x, double y, double z, boolean avoidLava) {
        Block feet = world.getBlockAt((int) x, (int) y, (int) z);
        Block head = feet.getRelative(BlockFace.UP);
        Block below = feet.getRelative(BlockFace.DOWN);
        
        // Проверяем, что ноги не в твердом блоке
        if (feet.getType().isSolid()) return false;
        
        // Проверяем, что голова не в твердом блоке
        if (head.getType().isSolid()) return false;
        
        // Проверяем, что под ногами что-то твердое
        if (!below.getType().isSolid()) return false;
        
        // Проверяем лаву, если нужно
        if (avoidLava && (feet.getType() == Material.LAVA || 
                          feet.getType() == Material.LAVA ||
                          below.getType() == Material.LAVA ||
                          below.getType() == Material.LAVA)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверяет, можно ли добраться до точки
     */
    private boolean canMoveTo(double x, double y, double z, double fromY, double maxDrop) {
        // Проверяем, не слишком ли высокий подъем
        if (y - fromY > 1.0) return false; // Максимальная высота подъема - 1 блок
        
        // Проверяем, не слишком ли глубокое падение
        if (fromY - y > maxDrop) return false;
        
        return true;
    }
    
    /**
     * Вычисляет эвристическую оценку стоимости пути между узлами
     */
    private double heuristicCostEstimate(Node from, Node to) {
        // Используем евклидово расстояние в качестве эвристики
        return from.distanceTo(to);
    }
    
    /**
     * Вычисляет расстояние между узлами
     */
    private double distanceBetween(Node a, Node b) {
        // Если движение по диагонали
        if (a.getX() != b.getX() && a.getZ() != b.getZ()) {
            return DIAGONAL_COST * stepSize;
        }
        return STRAIGHT_COST * stepSize;
    }
    
    /**
     * Внутренний класс для представления узла сетки
     */
    private static class Node implements Comparable<Node> {
        private final double x, y, z;
        
        public Node(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        
        public Location getLocation(World world) {
            return new Location(world, x, y, z);
        }
        
        public double distanceTo(Node other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Double.compare(node.x, x) == 0 &&
                   Double.compare(node.y, y) == 0 &&
                   Double.compare(node.z, z) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
        
        @Override
        public int compareTo(Node other) {
            // Сравниваем по f-стоимости (g + h)
            double thisF = 0; // Здесь должна быть f-стоимость
            double otherF = 0; // Здесь должна быть f-стоимость
            
            return Double.compare(thisF, otherF);
        }
    }
    
    /**
     * Внутренний класс для хранения сетки узлов
     */
    private static class NodeGrid {
        private final double stepSize;
        private final Map<Long, Node> nodes;
        
        public NodeGrid(double stepSize) {
            this.stepSize = stepSize;
            this.nodes = new HashMap<>();
        }
        
        public Node getNode(Location loc) {
            return getNode(loc.getX(), loc.getY(), loc.getZ());
        }
        
        public Node getNode(double x, double y, double z) {
            // Округляем координаты до ближайшего узла сетки
            long gridX = Math.round(x / stepSize);
            long gridY = Math.round(y / stepSize);
            long gridZ = Math.round(z / stepSize);
            
            // Используем хеш для быстрого поиска
            long key = (gridX & 0x7FFFFFF) | ((gridZ & 0x7FFFFFF) << 27) | ((gridY & 0xFF) << 54);
            
            return nodes.computeIfAbsent(key, k -> 
                new Node(gridX * stepSize, gridY * stepSize, gridZ * stepSize)
            );
        }
    }
}
