package com.sparky.libx.example;

import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.testing.TestFramework;

/**
 * приклад використання тестового фреймворку
 * демонструє різні можливості тестування
 * @author Андрій Будильников
 */
public class TestFrameworkExample extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("TestFrameworkExample plugin включено!");
        
        // демонстрація тестового фреймворку
        demonstrateTestFramework();
    }
    
    /**
     * демонстрація тестового фреймворку
     */
    private void demonstrateTestFramework() {
        getLogger().info("=== Демонстрація тестового фреймворку ===");
        
        TestFramework testFramework = new TestFramework();
        
        // запустити тести
        testFramework.runTests(CalculatorTest.class);
        testFramework.runTests(StringUtilsTest.class);
        testFramework.runTests(CollectionUtilsTest.class);
        
        // вивести статистику
        TestFramework.TestStatistics stats = testFramework.getStatistics();
        getLogger().info(stats.toString());
        
        // вивести провалені тести
        if (!stats.getFailedResults().isEmpty()) {
            getLogger().info("Провалені тести:");
            for (TestFramework.TestResult result : stats.getFailedResults()) {
                getLogger().info("  " + result);
            }
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("TestFrameworkExample plugin вимкнено!");
    }
    
    /**
     * приклад класу для тестування - калькулятор
     */
    public static class Calculator {
        
        public int add(int a, int b) {
            return a + b;
        }
        
        public int subtract(int a, int b) {
            return a - b;
        }
        
        public int multiply(int a, int b) {
            return a * b;
        }
        
        public int divide(int a, int b) {
            if (b == 0) {
                throw new IllegalArgumentException("Ділення на нуль");
            }
            return a / b;
        }
        
        public boolean isEven(int number) {
            return number % 2 == 0;
        }
        
        public int factorial(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("Факторіал визначений тільки для невід'ємних чисел");
            }
            if (n == 0 || n == 1) {
                return 1;
            }
            return n * factorial(n - 1);
        }
    }
    
    /**
     * тестування калькулятора
     */
    public static class CalculatorTest {
        
        private Calculator calculator;
        
        @TestFramework.BeforeEach
        public void setUp() {
            calculator = new Calculator();
        }
        
        @TestFramework.AfterEach
        public void tearDown() {
            calculator = null;
        }
        
        @TestFramework.Test(description = "Тест додавання")
        public void testAdd() {
            TestFramework.Assert.assertEquals(5, calculator.add(2, 3));
            TestFramework.Assert.assertEquals(0, calculator.add(-1, 1));
            TestFramework.Assert.assertEquals(-5, calculator.add(-2, -3));
        }
        
        @TestFramework.Test(description = "Тест віднімання")
        public void testSubtract() {
            TestFramework.Assert.assertEquals(1, calculator.subtract(3, 2));
            TestFramework.Assert.assertEquals(-2, calculator.subtract(-1, 1));
            TestFramework.Assert.assertEquals(1, calculator.subtract(-2, -3));
        }
        
        @TestFramework.Test(description = "Тест множення")
        public void testMultiply() {
            TestFramework.Assert.assertEquals(6, calculator.multiply(2, 3));
            TestFramework.Assert.assertEquals(-3, calculator.multiply(-1, 3));
            TestFramework.Assert.assertEquals(0, calculator.multiply(0, 100));
        }
        
        @TestFramework.Test(description = "Тест ділення")
        public void testDivide() {
            TestFramework.Assert.assertEquals(2, calculator.divide(6, 3));
            TestFramework.Assert.assertEquals(-2, calculator.divide(-6, 3));
            TestFramework.Assert.assertEquals(0, calculator.divide(0, 5));
        }
        
        @TestFramework.Test(description = "Тест ділення на нуль")
        public void testDivideByZero() {
            TestFramework.Assert.assertThrows(IllegalArgumentException.class, 
                () -> calculator.divide(5, 0),
                "Очікувалося виняток при діленні на нуль");
        }
        
        @TestFramework.Test(description = "Тест перевірки парного числа")
        public void testIsEven() {
            TestFramework.Assert.assertTrue(calculator.isEven(2));
            TestFramework.Assert.assertTrue(calculator.isEven(0));
            TestFramework.Assert.assertTrue(calculator.isEven(-4));
            TestFramework.Assert.assertFalse(calculator.isEven(3));
            TestFramework.Assert.assertFalse(calculator.isEven(-1));
        }
        
        @TestFramework.Test(description = "Тест факторіала")
        public void testFactorial() {
            TestFramework.Assert.assertEquals(1, calculator.factorial(0));
            TestFramework.Assert.assertEquals(1, calculator.factorial(1));
            TestFramework.Assert.assertEquals(2, calculator.factorial(2));
            TestFramework.Assert.assertEquals(6, calculator.factorial(3));
            TestFramework.Assert.assertEquals(24, calculator.factorial(4));
        }
        
        @TestFramework.Test(description = "Тест факторіала від'ємного числа")
        public void testFactorialNegative() {
            TestFramework.Assert.assertThrows(IllegalArgumentException.class,
                () -> calculator.factorial(-1),
                "Очікувалося виняток при обчисленні факторіала від'ємного числа");
        }
        
        @TestFramework.Test(description = "Пропущений тест")
        @TestFramework.Skip(reason = "Ще не реалізовано")
        public void testNotImplemented() {
            // цей тест буде пропущено
        }
    }
    
    /**
     * утиліти для роботи з рядками
     */
    public static class StringUtils {
        
        public static boolean isEmpty(String str) {
            return str == null || str.length() == 0;
        }
        
        public static boolean isBlank(String str) {
            return str == null || str.trim().length() == 0;
        }
        
        public static String reverse(String str) {
            if (str == null) {
                return null;
            }
            return new StringBuilder(str).reverse().toString();
        }
        
        public static String capitalize(String str) {
            if (str == null || str.length() == 0) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
        
        public static String[] split(String str, String delimiter) {
            if (str == null) {
                return new String[0];
            }
            if (delimiter == null || delimiter.isEmpty()) {
                return new String[]{str};
            }
            return str.split(delimiter);
        }
        
        public static boolean contains(String str, String searchStr) {
            if (str == null || searchStr == null) {
                return false;
            }
            return str.contains(searchStr);
        }
    }
    
    /**
     * тестування утиліт для роботи з рядками
     */
    public static class StringUtilsTest {
        
        @TestFramework.Test(description = "Тест перевірки порожнього рядка")
        public void testIsEmpty() {
            TestFramework.Assert.assertTrue(StringUtils.isEmpty(null));
            TestFramework.Assert.assertTrue(StringUtils.isEmpty(""));
            TestFramework.Assert.assertFalse(StringUtils.isEmpty(" "));
            TestFramework.Assert.assertFalse(StringUtils.isEmpty("abc"));
        }
        
        @TestFramework.Test(description = "Тест перевірки пробілового рядка")
        public void testIsBlank() {
            TestFramework.Assert.assertTrue(StringUtils.isBlank(null));
            TestFramework.Assert.assertTrue(StringUtils.isBlank(""));
            TestFramework.Assert.assertTrue(StringUtils.isBlank(" "));
            TestFramework.Assert.assertTrue(StringUtils.isBlank("   "));
            TestFramework.Assert.assertFalse(StringUtils.isBlank("abc"));
            TestFramework.Assert.assertFalse(StringUtils.isBlank(" abc "));
        }
        
        @TestFramework.Test(description = "Тест реверсу рядка")
        public void testReverse() {
            TestFramework.Assert.assertEquals("cba", StringUtils.reverse("abc"));
            TestFramework.Assert.assertEquals("", StringUtils.reverse(""));
            TestFramework.Assert.assertNull(StringUtils.reverse(null));
            TestFramework.Assert.assertEquals("321", StringUtils.reverse("123"));
        }
        
        @TestFramework.Test(description = "Тест капіталізації рядка")
        public void testCapitalize() {
            TestFramework.Assert.assertEquals("Abc", StringUtils.capitalize("abc"));
            TestFramework.Assert.assertEquals("Abc", StringUtils.capitalize("ABC"));
            TestFramework.Assert.assertEquals("Abc", StringUtils.capitalize("aBc"));
            TestFramework.Assert.assertEquals("", StringUtils.capitalize(""));
            TestFramework.Assert.assertNull(StringUtils.capitalize(null));
        }
        
        @TestFramework.Test(description = "Тест розділення рядка")
        public void testSplit() {
            TestFramework.Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
            TestFramework.Assert.assertArrayEquals(new String[]{"a"}, StringUtils.split("a", ","));
            TestFramework.Assert.assertArrayEquals(new String[]{""}, StringUtils.split("", ","));
            TestFramework.Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", "\\."));
        }
        
        @TestFramework.Test(description = "Тест пошуку підрядка")
        public void testContains() {
            TestFramework.Assert.assertTrue(StringUtils.contains("abcdef", "bcd"));
            TestFramework.Assert.assertTrue(StringUtils.contains("hello world", "world"));
            TestFramework.Assert.assertFalse(StringUtils.contains("abc", "def"));
            TestFramework.Assert.assertFalse(StringUtils.contains(null, "abc"));
            TestFramework.Assert.assertFalse(StringUtils.contains("abc", null));
        }
    }
    
    /**
     * утиліти для роботи з колекціями
     */
    public static class CollectionUtils {
        
        public static <T> boolean isEmpty(Collection<T> collection) {
            return collection == null || collection.isEmpty();
        }
        
        public static <T> int size(Collection<T> collection) {
            return collection == null ? 0 : collection.size();
        }
        
        public static <T> List<T> reverse(List<T> list) {
            if (list == null) {
                return null;
            }
            List<T> reversed = new ArrayList<>(list);
            Collections.reverse(reversed);
            return reversed;
        }
        
        public static <T> boolean containsAll(Collection<T> collection, T... elements) {
            if (collection == null) {
                return false;
            }
            for (T element : elements) {
                if (!collection.contains(element)) {
                    return false;
                }
            }
            return true;
        }
        
        public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
            if (a == null || b == null) {
                return new ArrayList<>();
            }
            List<T> result = new ArrayList<>();
            for (T element : a) {
                if (b.contains(element) && !result.contains(element)) {
                    result.add(element);
                }
            }
            return result;
        }
        
        public static <T> List<T> union(Collection<T> a, Collection<T> b) {
            if (a == null && b == null) {
                return new ArrayList<>();
            }
            if (a == null) {
                return new ArrayList<>(b);
            }
            if (b == null) {
                return new ArrayList<>(a);
            }
            
            List<T> result = new ArrayList<>(a);
            for (T element : b) {
                if (!result.contains(element)) {
                    result.add(element);
                }
            }
            return result;
        }
    }
    
    /**
     * тестування утиліт для роботи з колекціями
     */
    public static class CollectionUtilsTest {
        
        @TestFramework.Test(description = "Тест перевірки порожньої колекції")
        public void testIsEmpty() {
            TestFramework.Assert.assertTrue(CollectionUtils.isEmpty(null));
            TestFramework.Assert.assertTrue(CollectionUtils.isEmpty(new ArrayList<>()));
            TestFramework.Assert.assertFalse(CollectionUtils.isEmpty(Arrays.asList("a")));
        }
        
        @TestFramework.Test(description = "Тест отримання розміру колекції")
        public void testSize() {
            TestFramework.Assert.assertEquals(0, CollectionUtils.size(null));
            TestFramework.Assert.assertEquals(0, CollectionUtils.size(new ArrayList<>()));
            TestFramework.Assert.assertEquals(3, CollectionUtils.size(Arrays.asList("a", "b", "c")));
        }
        
        @TestFramework.Test(description = "Тест реверсу списку")
        public void testReverse() {
            List<String> original = Arrays.asList("a", "b", "c");
            List<String> reversed = CollectionUtils.reverse(original);
            TestFramework.Assert.assertArrayEquals(new String[]{"c", "b", "a"}, reversed.toArray());
            TestFramework.Assert.assertNull(CollectionUtils.reverse(null));
        }
        
        @TestFramework.Test(description = "Тест перевірки наявності елементів")
        public void testContainsAll() {
            List<String> list = Arrays.asList("a", "b", "c", "d");
            TestFramework.Assert.assertTrue(CollectionUtils.containsAll(list, "a", "b"));
            TestFramework.Assert.assertTrue(CollectionUtils.containsAll(list, "a", "b", "c", "d"));
            TestFramework.Assert.assertFalse(CollectionUtils.containsAll(list, "a", "e"));
            TestFramework.Assert.assertFalse(CollectionUtils.containsAll(null, "a"));
        }
        
        @TestFramework.Test(description = "Тест перетину колекцій")
        public void testIntersection() {
            List<String> list1 = Arrays.asList("a", "b", "c");
            List<String> list2 = Arrays.asList("b", "c", "d");
            List<String> intersection = CollectionUtils.intersection(list1, list2);
            TestFramework.Assert.assertArrayEquals(new String[]{"b", "c"}, intersection.toArray());
            
            TestFramework.Assert.assertArrayEquals(new String[0], 
                CollectionUtils.intersection(null, list2).toArray());
        }
        
        @TestFramework.Test(description = "Тест об'єднання колекцій")
        public void testUnion() {
            List<String> list1 = Arrays.asList("a", "b", "c");
            List<String> list2 = Arrays.asList("c", "d", "e");
            List<String> union = CollectionUtils.union(list1, list2);
            Collections.sort(union); // сортуємо для перевірки
            TestFramework.Assert.assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, union.toArray());
        }
    }
}