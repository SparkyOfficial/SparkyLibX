# SparkyLibX

## English

A simple and powerful library for working with regions in Minecraft plugins.

### What the library can do
- Create and manage territories (cuboids, spheres, polygons, cylinders)
- Save regions to database
- Check if points belong to regions
- Region access control system
- Work with vectors and matrices
- Generate points on lines, circles and spheres
- Check figure intersections
- Calculate areas and volumes
- Display regions using particles
- Animated boundary effects
- Customize colors and display styles
- 3D vectors with basic operations
- 4x4 matrices for transformations
- Quaternions for rotations
- Useful mathematical functions

### Installation

Add dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>SparkyLibX</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Quick Start

```java
// Create region
World world = Bukkit.getWorld("world");
CuboidRegion region = new CuboidRegion("myregion", world, 0, 0, 0, 10, 10, 10);

// Check point
Location location = new Location(world, 5, 5, 5);
if (region.contains(location)) {
    System.out.println("Point is inside region!");
}

// Display region to player
RegionVisualizer visualizer = new RegionVisualizer();
visualizer.showRegion(player, region);
```

---

## Русский

Простая и мощная библиотека для работы с регионами в Minecraft плагинах.

### Что умеет библиотека
- Создание и управление территориями (кубоиды, сферы, полигоны, цилиндры)
- Сохранение регионов в базу данных
- Проверка принадлежности точек к регионам
- Система прав доступа к регионам
- Работа с векторами и матрицами
- Генерация точек на линиях, окружностях и сферах
- Проверка пересечений фигур
- Расчет площадей и объемов
- Отображение регионов с помощью частиц
- Анимированные эффекты границ
- Настройка цветов и стилей отображения
- 3D векторы с основными операциями
- 4x4 матрицы для трансформаций
- Кватернионы для вращений
- Полезные математические функции

### Установка

Добавьте зависимость в ваш pom.xml:

```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>SparkyLibX</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Быстрый старт

```java
// Создание региона
World world = Bukkit.getWorld("world");
CuboidRegion region = new CuboidRegion("myregion", world, 0, 0, 0, 10, 10, 10);

// Проверка точки
Location location = new Location(world, 5, 5, 5);
if (region.contains(location)) {
    System.out.println("Точка внутри региона!");
}

// Отображение региона игроку
RegionVisualizer visualizer = new RegionVisualizer();
visualizer.showRegion(player, region);
```

---

## Українська

Проста і потужна бібліотека для роботи з регіонами в плагінах Minecraft.

### Що вміє бібліотека
- Створення та управління територіями (кубоїди, сфери, полігони, циліндри)
- Збереження регіонів у базу даних
- Перевірка належності точок до регіонів
- Система прав доступу до регіонів
- Робота з векторами та матрицями
- Генерація точок на лініях, колах та сферах
- Перевірка перетинів фігур
- Розрахунок площ та об'ємів
- Відображення регіонів за допомогою частинок
- Анімовані ефекти меж
- Налаштування кольорів та стилів відображення
- 3D вектори з основними операціями
- 4x4 матриці для трансформацій
- Кватерніони для обертань
- Корисні математичні функції

### Встановлення

Додайте залежність у ваш pom.xml:

```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>SparkyLibX</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Швидкий старт

```java
// Створення регіону
World world = Bukkit.getWorld("world");
CuboidRegion region = new CuboidRegion("myregion", world, 0, 0, 0, 10, 10, 10);

// Перевірка точки
Location location = new Location(world, 5, 5, 5);
if (region.contains(location)) {
    System.out.println("Точка всередині регіону!");
}

// Відображення регіону гравцеві
RegionVisualizer visualizer = new RegionVisualizer();
visualizer.showRegion(player, region);
```

---

## License

MIT

---
Author: Андрій Будильников