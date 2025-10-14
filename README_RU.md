# SparkyLibX - Расширенная библиотека для Minecraft плагинов

SparkyLibX - это мощная и комплексная библиотека для разработки плагинов Minecraft, 
предоставляющая широкий набор инструментов для работы с регионами, геометрией, 
математикой, блоками и визуализацией.

## Основные возможности

### 1. Система регионов
- CuboidRegion - кубоидные регионы
- SphereRegion - сферические регионы
- CylinderRegion - цилиндрические регионы
- PolygonRegion - полигональные регионы
- Поддержка иерархии регионов и прав доступа

### 2. Геометрические утилиты
- BoundingBox - ограничительные коробки
- Ray - лучи для трассировки
- ShapeUtils - утилиты для работы с формами
- AdvancedGeometry - расширенные геометрические операции

### 3. Математические инструменты
- Vector3D - трехмерные векторы
- Matrix4x4 - матрицы 4x4
- Quaternion - кватернионы для 3D вращений
- MathUtils - математические утилиты
- Trigonometry - тригонометрические функции
- NoiseGenerator - генераторы шума (Перлин, Вороного, волновой и др.)

### 4. Работа с блоками
- BlockUtils - утилиты для работы с блоками
- Создание сложных структур и паттернов
- Генерация ландшафтов с помощью шума
- Манипуляции с блоками в больших объемах

### 5. Генерация паттернов
Библиотека теперь включает мощные инструменты для создания сложных паттернов:

#### Шум Перлина
```java
// Создание сферы с органической текстурой из шума Перлина
List<Block> blocks = BlockUtils.createNoisySphere(
    center, radius, Material.GLASS, 0.1, 0.0
);
```

#### Фрактальный шум (FBM)
```java
// Создание реалистичного ландшафта
List<Block> terrain = BlockUtils.createPerlinTerrain(
    corner1, corner2, Material.STONE, 6, 0.5, 0.02, 20, 70
);
```

#### Шум Вороного
```java
// Создание ячеистой структуры
Material[] materials = {Material.STONE, Material.DIRT, Material.GRASS_BLOCK};
List<Block> pattern = BlockUtils.createVoronoiPattern(
    corner1, corner2, materials, 2.0
);
```

#### Спиральные паттерны
```java
// Создание спиральной структуры
List<Block> spiral = BlockUtils.createSpiralPattern(
    center, 8.0, 20.0, 5.0, Material.GOLD_BLOCK
);
```

#### Трехмерная шахматная доска
```java
// Создание 3D шахматной доски
List<Block> checkerboard = BlockUtils.create3DCheckerboard(
    corner1, corner2, Material.BLACK_CONCRETE, Material.WHITE_CONCRETE, 2
);
```

### 6. Процедурная генерация структур
- StructureGenerator - генератор сложных структур
- Деревья, кристальные пещеры, спиральные башни, лабиринты

#### Генерация деревьев
```java
// Создание процедурного дерева
List<Block> tree = StructureGenerator.generateTree(
    location, 7, Material.OAK_LOG, Material.OAK_LEAVES
);
```

#### Кристальные пещеры
```java
// Создание кристальной пещеры
List<Block> cave = StructureGenerator.generateCrystalCave(
    center, 8.0, Material.GLASS, Material.STONE
);
```

#### Спиральные башни
```java
// Создание спиральной башни
List<Block> tower = StructureGenerator.generateSpiralTower(
    center, 15, 2.5, Material.STONE, 2.0
);
```

#### Лабиринты
```java
// Создание лабиринта
List<Block> maze = StructureGenerator.generateMaze(
    corner1, corner2, Material.COBBLESTONE, 2
);
```

### 7. Поиск пути
- Pathfinder - реализация алгоритма A* для поиска пути
- Поддержка 3D навигации
- Учет препятствий и лавы

### 8. Визуализация
- ParticleRenderer - рендерер частиц
- AnimatedRegionRenderer - анимированный рендерер регионов
- RegionVisualizationManager - менеджер визуализации

### 9. Хранение данных
- RegionDatabase - система хранения регионов
- HikariCP - пулинг соединений с БД
- Поддержка SQLite и H2

## Установка

1. Добавьте зависимость в ваш pom.xml:
```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>SparkyLibX</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. Или скопируйте JAR файл в папку plugins вашего сервера.

## Использование

### Пример создания ландшафта:
```java
// Создание реалистичного ландшафта с помощью фрактального шума Перлина
World world = Bukkit.getWorld("world");
Location corner1 = new Location(world, 0, 64, 0);
Location corner2 = new Location(world, 100, 100, 100);

List<Block> terrain = BlockUtils.createPerlinTerrain(
    corner1, corner2, 
    Material.STONE,  // Основной материал
    6,               // Количество октав
    0.5,             // Persistence
    0.02,            // Масштаб
    20,              // Множитель высоты
    70               // Уровень моря
);
```

### Пример использования генератора шума:
```java
// Генерация различных типов шума
double perlin = NoiseGenerator.perlinNoise(0.5, 0.5, 0.5);
double fractal = NoiseGenerator.fractalNoise(0.5, 0.5, 0.5, 4, 0.5, 0.01);
double voronoi = NoiseGenerator.voronoiNoise(0.5, 0.5, 0.5, 1.0);
```

### Пример генерации структур:
```java
// Генерация процедурного дерева
List<Block> tree = StructureGenerator.generateTree(
    location, 7, Material.OAK_LOG, Material.OAK_LEAVES
);

// Генерация кристальной пещеры
List<Block> cave = StructureGenerator.generateCrystalCave(
    center, 8.0, Material.GLASS, Material.STONE
);
```

## Лицензия

MIT License - см. файл LICENSE для подробностей.

## Автор

Разработано для сообщества разработчиков Minecraft плагинов.