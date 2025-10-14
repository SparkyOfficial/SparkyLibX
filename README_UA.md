# SparkyLibX - Розширена бібліотека для Minecraft плагінів

SparkyLibX - це потужна та комплексна бібліотека для розробки плагінів Minecraft, 
що надає широкий набір інструментів для роботи з регіонами, геометрією, 
математикою, блоками та візуалізацією.

## Основні можливості

### 1. Система регіонів
- CuboidRegion - кубоїдні регіони
- SphereRegion - сферичні регіони
- CylinderRegion - циліндричні регіони
- PolygonRegion - полігональні регіони
- Підтримка ієрархії регіонів та прав доступу

### 2. Геометричні утиліти
- BoundingBox - обмежувальні коробки
- Ray - промені для трасування
- ShapeUtils - утиліти для роботи з формами
- AdvancedGeometry - розширені геометричні операції

### 3. Математичні інструменти
- Vector3D - тривимірні вектори
- Matrix4x4 - матриці 4x4
- Quaternion - кватерніони для 3D обертань
- MathUtils - математичні утиліти
- Trigonometry - тригонометричні функції
- NoiseGenerator - генератори шуму (Перлін, Вороного, хвильовий тощо)

### 4. Робота з блоками
- BlockUtils - утиліти для роботи з блоками
- Створення складних структур та патернів
- Генерація ландшафтів за допомогою шуму
- Маніпуляції з блоками у великих об'ємах

### 5. Генерація патернів
Бібліотека тепер включає потужні інструменти для створення складних патернів:

#### Шум Перліна
```java
// Створення сфери з органічною текстурою з шуму Перліна
List<Block> blocks = BlockUtils.createNoisySphere(
    center, radius, Material.GLASS, 0.1, 0.0
);
```

#### Фрактальний шум (FBM)
```java
// Створення реалістичного ландшафту
List<Block> terrain = BlockUtils.createPerlinTerrain(
    corner1, corner2, Material.STONE, 6, 0.5, 0.02, 20, 70
);
```

#### Шум Вороного
```java
// Створення ячеїстої структури
Material[] materials = {Material.STONE, Material.DIRT, Material.GRASS_BLOCK};
List<Block> pattern = BlockUtils.createVoronoiPattern(
    corner1, corner2, materials, 2.0
);
```

#### Спіральні патерни
```java
// Створення спіральної структури
List<Block> spiral = BlockUtils.createSpiralPattern(
    center, 8.0, 20.0, 5.0, Material.GOLD_BLOCK
);
```

#### Тривимірна шахова дошка
```java
// Створення 3D шахової дошки
List<Block> checkerboard = BlockUtils.create3DCheckerboard(
    corner1, corner2, Material.BLACK_CONCRETE, Material.WHITE_CONCRETE, 2
);
```

### 6. Процедурна генерація структур
- StructureGenerator - генератор складних структур
- Дерева, кристальні печери, спіральні вежі, лабіринти

#### Генерація дерев
```java
// Створення процедурного дерева
List<Block> tree = StructureGenerator.generateTree(
    location, 7, Material.OAK_LOG, Material.OAK_LEAVES
);
```

#### Кристальні печери
```java
// Створення кристальної печери
List<Block> cave = StructureGenerator.generateCrystalCave(
    center, 8.0, Material.GLASS, Material.STONE
);
```

#### Спіральні вежі
```java
// Створення спіральної вежі
List<Block> tower = StructureGenerator.generateSpiralTower(
    center, 15, 2.5, Material.STONE, 2.0
);
```

#### Лабіринти
```java
// Створення лабіринту
List<Block> maze = StructureGenerator.generateMaze(
    corner1, corner2, Material.COBBLESTONE, 2
);
```

### 7. Пошук шляху
- Pathfinder - реалізація алгоритму A* для пошуку шляху
- Підтримка 3D навігації
- Врахування перешкод та лави

### 8. Візуалізація
- ParticleRenderer - рендерер частинок
- AnimatedRegionRenderer - анімований рендерер регіонів
- RegionVisualizationManager - менеджер візуалізації

### 9. Зберігання даних
- RegionDatabase - система зберігання регіонів
- HikariCP - пулінг з'єднань з БД
- Підтримка SQLite та H2

## Встановлення

1. Додайте залежність у ваш pom.xml:
```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>SparkyLibX</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. Або скопіюйте JAR файл у папку plugins вашого сервера.

## Використання

### Приклад створення ландшафту:
```java
// Створення реалістичного ландшафту за допомогою фрактального шуму Перліна
World world = Bukkit.getWorld("world");
Location corner1 = new Location(world, 0, 64, 0);
Location corner2 = new Location(world, 100, 100, 100);

List<Block> terrain = BlockUtils.createPerlinTerrain(
    corner1, corner2, 
    Material.STONE,  // Основний матеріал
    6,               // Кількість октав
    0.5,             // Persistence
    0.02,            // Масштаб
    20,              // Множник висоти
    70               // Рівень моря
);
```

### Приклад використання генератора шуму:
```java
// Генерація різних типів шуму
double perlin = NoiseGenerator.perlinNoise(0.5, 0.5, 0.5);
double fractal = NoiseGenerator.fractalNoise(0.5, 0.5, 0.5, 4, 0.5, 0.01);
double voronoi = NoiseGenerator.voronoiNoise(0.5, 0.5, 0.5, 1.0);
```

### Приклад генерації структур:
```java
// Генерація процедурного дерева
List<Block> tree = StructureGenerator.generateTree(
    location, 7, Material.OAK_LOG, Material.OAK_LEAVES
);

// Генерація кристальної печери
List<Block> cave = StructureGenerator.generateCrystalCave(
    center, 8.0, Material.GLASS, Material.STONE
);
```

## Ліцензія

MIT License - див. файл LICENSE для деталей.

## Автор

Розроблено для спільноти розробників Minecraft плагінів.