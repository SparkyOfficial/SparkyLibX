package com.sparky.libx.testing;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * простий фреймворк для модульного тестування
 * @author Андрій Будильников
 */
public class TestFramework {
    
    private final List<TestResult> testResults = new CopyOnWriteArrayList<>();
    private final AtomicInteger passedTests = new AtomicInteger(0);
    private final AtomicInteger failedTests = new AtomicInteger(0);
    private final AtomicInteger skippedTests = new AtomicInteger(0);
    
    /**
     * анотація для позначення тестового методу
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Test {
        String description() default "";
    }
    
    /**
     * анотація для позначення методу налаштування перед тестами
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeEach {
    }
    
    /**
     * анотація для позначення методу очищення після тестів
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterEach {
    }
    
    /**
     * анотація для позначення методу налаштування перед усіма тестами
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeAll {
    }
    
    /**
     * анотація для позначення методу очищення після усіх тестів
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterAll {
    }
    
    /**
     * анотація для пропуску тесту
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Skip {
        String reason() default "";
    }
    
    /**
     * анотація для групування тестів
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface TestGroup {
        String[] value();
    }
    
    /**
     * результат тесту
     */
    public static class TestResult {
        private final String testName;
        private final String className;
        private final boolean passed;
        private final String errorMessage;
        private final long executionTime;
        private final String description;
        
        public TestResult(String testName, String className, boolean passed, String errorMessage, 
                         long executionTime, String description) {
            this.testName = testName;
            this.className = className;
            this.passed = passed;
            this.errorMessage = errorMessage;
            this.executionTime = executionTime;
            this.description = description;
        }
        
        // геттери
        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public boolean isPassed() { return passed; }
        public String getErrorMessage() { return errorMessage; }
        public long getExecutionTime() { return executionTime; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(className).append(".").append(testName);
            if (description != null && !description.isEmpty()) {
                sb.append(" (").append(description).append(")");
            }
            sb.append(": ").append(passed ? "PASSED" : "FAILED");
            if (!passed && errorMessage != null) {
                sb.append(" - ").append(errorMessage);
            }
            sb.append(" (").append(executionTime).append("ms)");
            return sb.toString();
        }
    }
    
    /**
     * статистика тестування
     */
    public static class TestStatistics {
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        private final int skippedTests;
        private final long totalTime;
        private final List<TestResult> failedResults;
        
        public TestStatistics(int totalTests, int passedTests, int failedTests, int skippedTests, 
                             long totalTime, List<TestResult> failedResults) {
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
            this.skippedTests = skippedTests;
            this.totalTime = totalTime;
            this.failedResults = failedResults;
        }
        
        // геттери
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        public int getSkippedTests() { return skippedTests; }
        public long getTotalTime() { return totalTime; }
        public List<TestResult> getFailedResults() { return failedResults; }
        
        public double getSuccessRate() {
            return totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Тестування завершено: %d тестів (%d пройдено, %d провалено, %d пропущено) - %.2f%% успішно за %d мс",
                totalTests, passedTests, failedTests, skippedTests, getSuccessRate(), totalTime
            );
        }
    }
    
    /**
     * запустити тести в класі
     * @param testClass клас з тестами
     */
    public void runTests(Class<?> testClass) {
        try {
            // знайти методи з анотаціями
            Method beforeAllMethod = findMethodWithAnnotation(testClass, BeforeAll.class);
            Method afterAllMethod = findMethodWithAnnotation(testClass, AfterAll.class);
            
            // виконати @BeforeAll
            if (beforeAllMethod != null) {
                executeMethod(null, beforeAllMethod);
            }
            
            // створити екземпляр класу тестів
            Object testInstance = testClass.getDeclaredConstructor().newInstance();
            
            // знайти всі тестові методи
            Method[] methods = testClass.getDeclaredMethods();
            List<Method> testMethods = new ArrayList<>();
            Method beforeEachMethod = null;
            Method afterEachMethod = null;
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                } else if (method.isAnnotationPresent(BeforeEach.class)) {
                    beforeEachMethod = method;
                } else if (method.isAnnotationPresent(AfterEach.class)) {
                    afterEachMethod = method;
                }
            }
            
            // виконати всі тестові методи
            for (Method testMethod : testMethods) {
                runSingleTest(testInstance, testMethod, beforeEachMethod, afterEachMethod);
            }
            
            // виконати @AfterAll
            if (afterAllMethod != null) {
                executeMethod(null, afterAllMethod);
            }
            
        } catch (Exception e) {
            System.err.println("Помилка під час виконання тестів в класі " + testClass.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * запустити один тестовий метод
     */
    private void runSingleTest(Object testInstance, Method testMethod, Method beforeEachMethod, Method afterEachMethod) {
        String testName = testMethod.getName();
        String className = testInstance.getClass().getSimpleName();
        String description = testMethod.isAnnotationPresent(Test.class) 
                           ? testMethod.getAnnotation(Test.class).description() 
                           : "";
        
        // перевірити чи тест потрібно пропустити
        if (testMethod.isAnnotationPresent(Skip.class)) {
            String reason = testMethod.getAnnotation(Skip.class).reason();
            TestResult result = new TestResult(testName, className, true, "Пропущено: " + reason, 0, description);
            testResults.add(result);
            skippedTests.incrementAndGet();
            System.out.println("[SKIPPED] " + result);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String errorMessage = null;
        
        try {
            // виконати @BeforeEach
            if (beforeEachMethod != null) {
                executeMethod(testInstance, beforeEachMethod);
            }
            
            // виконати тестовий метод
            executeMethod(testInstance, testMethod);
            passed = true;
            
        } catch (AssertionError e) {
            passed = false;
            errorMessage = e.getMessage();
        } catch (Exception e) {
            passed = false;
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
        } finally {
            // виконати @AfterEach
            if (afterEachMethod != null) {
                try {
                    executeMethod(testInstance, afterEachMethod);
                } catch (Exception e) {
                    if (errorMessage == null) {
                        errorMessage = "Помилка в @AfterEach: " + e.getMessage();
                    }
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            TestResult result = new TestResult(testName, className, passed, errorMessage, executionTime, description);
            testResults.add(result);
            
            if (passed) {
                passedTests.incrementAndGet();
                System.out.println("[PASSED] " + result);
            } else {
                failedTests.incrementAndGet();
                System.out.println("[FAILED] " + result);
            }
        }
    }
    
    /**
     * виконати метод
     */
    private void executeMethod(Object instance, Method method) throws Exception {
        method.setAccessible(true);
        if (Modifier.isStatic(method.getModifiers())) {
            method.invoke(null);
        } else {
            method.invoke(instance);
        }
    }
    
    /**
     * знайти метод з анотацією
     */
    private Method findMethodWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * отримати результати тестування
     * @return список результатів
     */
    public List<TestResult> getTestResults() {
        return new ArrayList<>(testResults);
    }
    
    /**
     * отримати статистику тестування
     * @return статистика
     */
    public TestStatistics getStatistics() {
        long totalTime = testResults.stream().mapToLong(TestResult::getExecutionTime).sum();
        List<TestResult> failedResults = testResults.stream()
            .filter(result -> !result.isPassed())
            .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
        
        return new TestStatistics(
            testResults.size(),
            passedTests.get(),
            failedTests.get(),
            skippedTests.get(),
            totalTime,
            failedResults
        );
    }
    
    /**
     * очистити результати тестування
     */
    public void clearResults() {
        testResults.clear();
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
    }
    
    /**
     * клас для тверджень (assertions)
     */
    public static class Assert {
        
        /**
         * перевірити чи значення істинне
         */
        public static void assertTrue(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }
        
        /**
         * перевірити чи значення істинне
         */
        public static void assertTrue(boolean condition) {
            assertTrue(condition, "Очікувалося true, але отримано false");
        }
        
        /**
         * перевірити чи значення хибне
         */
        public static void assertFalse(boolean condition, String message) {
            if (condition) {
                throw new AssertionError(message);
            }
        }
        
        /**
         * перевірити чи значення хибне
         */
        public static void assertFalse(boolean condition) {
            assertFalse(condition, "Очікувалося false, але отримано true");
        }
        
        /**
         * перевірити чи два об'єкти рівні
         */
        public static void assertEquals(Object expected, Object actual, String message) {
            if (expected == null && actual == null) {
                return;
            }
            if (expected == null || !expected.equals(actual)) {
                throw new AssertionError(message + " Очікувалося: " + expected + ", але отримано: " + actual);
            }
        }
        
        /**
         * перевірити чи два об'єкти рівні
         */
        public static void assertEquals(Object expected, Object actual) {
            assertEquals(expected, actual, "");
        }
        
        /**
         * перевірити чи два об'єкти не рівні
         */
        public static void assertNotEquals(Object expected, Object actual, String message) {
            if (expected == null && actual == null) {
                throw new AssertionError(message + " Очікувалося, що об'єкти не рівні, але обидва null");
            }
            if (expected != null && expected.equals(actual)) {
                throw new AssertionError(message + " Очікувалося, що об'єкти не рівні: " + expected);
            }
        }
        
        /**
         * перевірити чи два об'єкти не рівні
         */
        public static void assertNotEquals(Object expected, Object actual) {
            assertNotEquals(expected, actual, "");
        }
        
        /**
         * перевірити чи об'єкт null
         */
        public static void assertNull(Object object, String message) {
            if (object != null) {
                throw new AssertionError(message + " Очікувалося null, але отримано: " + object);
            }
        }
        
        /**
         * перевірити чи об'єкт null
         */
        public static void assertNull(Object object) {
            assertNull(object, "");
        }
        
        /**
         * перевірити чи об'єкт не null
         */
        public static void assertNotNull(Object object, String message) {
            if (object == null) {
                throw new AssertionError(message + " Очікувалося не null");
            }
        }
        
        /**
         * перевірити чи об'єкт не null
         */
        public static void assertNotNull(Object object) {
            assertNotNull(object, "");
        }
        
        /**
         * перевірити чи два масиви рівні
         */
        public static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
            if (expected == null && actual == null) {
                return;
            }
            if (expected == null || actual == null) {
                throw new AssertionError(message + " Очікувалося: " + Arrays.toString(expected) + ", але отримано: " + Arrays.toString(actual));
            }
            if (expected.length != actual.length) {
                throw new AssertionError(message + " Довжина масивів не співпадає. Очікувалося: " + expected.length + ", але отримано: " + actual.length);
            }
            for (int i = 0; i < expected.length; i++) {
                if (!Objects.equals(expected[i], actual[i])) {
                    throw new AssertionError(message + " Елементи в індексі " + i + " не співпадають. Очікувалося: " + expected[i] + ", але отримано: " + actual[i]);
                }
            }
        }
        
        /**
         * перевірити чи два масиви рівні
         */
        public static void assertArrayEquals(Object[] expected, Object[] actual) {
            assertArrayEquals(expected, actual, "");
        }
        
        /**
         * перевірити чи виникає виняток
         */
        public static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable, String message) {
            try {
                runnable.run();
                throw new AssertionError(message + " Очікувалося виняток типу " + expectedType.getSimpleName() + ", але він не виник");
            } catch (Throwable actualException) {
                if (!expectedType.isInstance(actualException)) {
                    throw new AssertionError(message + " Очікувалося виняток типу " + expectedType.getSimpleName() + ", але отримано " + actualException.getClass().getSimpleName() + ": " + actualException.getMessage());
                }
            }
        }
        
        /**
         * перевірити чи виникає виняток
         */
        public static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable) {
            assertThrows(expectedType, runnable, "");
        }
        
        /**
         * провалити тест
         */
        public static void fail(String message) {
            throw new AssertionError(message);
        }
        
        /**
         * провалити тест
         */
        public static void fail() {
            fail("Тест провалено");
        }
    }
    
    /**
     * клас для параметризованих тестів
     */
    public static class ParameterizedTest {
        
        /**
         * виконати тест з параметрами
         */
        public static void runWithParameters(RunnableWithParameters test, Object[]... parameters) {
            for (int i = 0; i < parameters.length; i++) {
                try {
                    test.run(parameters[i]);
                } catch (Exception e) {
                    throw new RuntimeException("Параметризований тест провалено на наборі параметрів #" + (i + 1), e);
                }
            }
        }
        
        @FunctionalInterface
        public interface RunnableWithParameters {
            void run(Object[] parameters) throws Exception;
        }
    }
}