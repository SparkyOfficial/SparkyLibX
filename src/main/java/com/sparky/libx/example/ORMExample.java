package com.sparky.libx.example;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.database.DatabaseManager;
import com.sparky.libx.database.orm.ORMFramework;

/**
 * приклад використання ORM фреймворку
 * демонструє роботу з сутностями, збереження, пошук і видалення даних
 * @author Андрій Будильников
 */
public class ORMExample extends JavaPlugin {
    
    private DatabaseManager databaseManager;
    private ORMFramework ormFramework;
    
    @Override
    public void onEnable() {
        getLogger().info("ORMExample plugin включено!");
        
        // ініціалізувати менеджер бази даних
        initializeDatabase();
        
        if (databaseManager != null) {
            // ініціалізувати ORM фреймворк
            ormFramework = new ORMFramework(databaseManager);
            
            // демонстрація ORM можливостей
            demonstrateORM();
        }
    }
    
    /**
     * ініціалізувати менеджер бази даних
     */
    private void initializeDatabase() {
        try {
            // використовуємо H2 базу даних в пам'яті для демонстрації
            databaseManager = new DatabaseManager(
                "jdbc:h2:mem:testdb",  // H2 in-memory database
                "sa",                  // username
                "",                    // password
                "h2"                   // database type
            );
            
            getLogger().info("Підключено до бази даних H2 в пам'яті");
        } catch (Exception e) {
            getLogger().severe("Не вдалося підключитися до бази даних: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * демонстрація ORM можливостей
     */
    private void demonstrateORM() {
        try {
            getLogger().info("=== Демонстрація ORM можливостей ===");
            
            // створити таблиці для сутностей
            ormFramework.createTable(User.class);
            ormFramework.createTable(Product.class);
            getLogger().info("Створено таблиці для сутностей");
            
            // створити нових користувачів
            User user1 = new User();
            user1.setName("Іван Петренко");
            user1.setEmail("ivan@example.com");
            user1.setAge(25);
            
            User user2 = new User();
            user2.setName("Марія Сидоренко");
            user2.setEmail("maria@example.com");
            user2.setAge(30);
            
            // зберегти користувачів
            ormFramework.save(user1);
            ormFramework.save(user2);
            getLogger().info("Збережено 2 користувачів");
            
            // створити продукти
            Product product1 = new Product();
            product1.setName("Ноутбук");
            product1.setPrice(15000.0);
            product1.setCategory("Електроніка");
            
            Product product2 = new Product();
            product2.setName("Смартфон");
            product2.setPrice(8000.0);
            product2.setCategory("Електроніка");
            
            // зберегти продукти
            ormFramework.save(product1);
            ormFramework.save(product2);
            getLogger().info("Збережено 2 продукти");
            
            // знайти всіх користувачів
            List<User> allUsers = ormFramework.findAll(User.class);
            getLogger().info("Знайдено " + allUsers.size() + " користувачів:");
            for (User user : allUsers) {
                getLogger().info("  - " + user.getName() + " (" + user.getEmail() + ")");
            }
            
            // знайти користувача за ID
            User foundUser = ormFramework.findById(User.class, user1.getId());
            if (foundUser != null) {
                getLogger().info("Знайдено користувача за ID: " + foundUser.getName());
            }
            
            // знайти користувачів за умовою
            List<User> youngUsers = ormFramework.findByCondition(User.class, "age < ?", 30);
            getLogger().info("Знайдено " + youngUsers.size() + " користувачів молодше 30 років");
            
            // оновити користувача
            user1.setAge(26);
            ormFramework.save(user1);
            getLogger().info("Оновлено вік користувача: " + user1.getName());
            
            // підрахувати кількість користувачів
            long userCount = ormFramework.count(User.class);
            getLogger().info("Загальна кількість користувачів: " + userCount);
            
            // підрахувати кількість користувачів за умовою
            long youngUserCount = ormFramework.count(User.class, "age < ?", 30);
            getLogger().info("Кількість користувачів молодше 30 років: " + youngUserCount);
            
            getLogger().info("Демонстрація ORM завершена успішно!");
            
        } catch (SQLException e) {
            getLogger().severe("Помилка під час демонстрації ORM: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ORMExample plugin вимкнено!");
        
        // закрити з'єднання з базою даних
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Закрито з'єднання з базою даних");
        }
    }
    
    /**
     * приклад сутності користувача
     */
    @com.sparky.libx.database.orm.ORMFramework.Entity(table = "users")
    public static class User {
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "id", primaryKey = true, autoIncrement = true)
        private Long id;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "name", nullable = false)
        private String name;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "email", nullable = false)
        private String email;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "age")
        private Integer age;
        
        // конструктор за замовчуванням
        public User() {}
        
        // геттери і сеттери
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "', age=" + age + "}";
        }
    }
    
    /**
     * приклад сутності продукта
     */
    @com.sparky.libx.database.orm.ORMFramework.Entity(table = "products")
    public static class Product {
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "id", primaryKey = true, autoIncrement = true)
        private Long id;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "name", nullable = false)
        private String name;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "price")
        private Double price;
        
        @com.sparky.libx.database.orm.ORMFramework.Column(name = "category")
        private String category;
        
        // конструктор за замовчуванням
        public Product() {}
        
        // геттери і сеттери
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        @Override
        public String toString() {
            return "Product{id=" + id + ", name='" + name + "', price=" + price + ", category='" + category + "'}";
        }
    }
}