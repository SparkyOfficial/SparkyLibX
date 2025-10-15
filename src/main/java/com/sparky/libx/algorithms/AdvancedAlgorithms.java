package com.sparky.libx.algorithms;

import java.util.*;

/**
 * просунуті алгоритми для складних обчислень
 * включає алгоритми сортування, пошуку, динамічного програмування і теорії чисел
 * @author Андрій Будильников
 */
public class AdvancedAlgorithms {
    
    /**
     * алгоритм швидкого сортування (quicksort)
     */
    public static void quickSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        quickSortRecursive(array, 0, array.length - 1);
    }
    
    private static void quickSortRecursive(int[] array, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(array, low, high);
            quickSortRecursive(array, low, pivotIndex - 1);
            quickSortRecursive(array, pivotIndex + 1, high);
        }
    }
    
    private static int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (array[j] <= pivot) {
                i++;
                swap(array, i, j);
            }
        }
        
        swap(array, i + 1, high);
        return i + 1;
    }
    
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    /**
     * алгоритм сортування злиттям (merge sort)
     */
    public static void mergeSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int[] temp = new int[array.length];
        mergeSortRecursive(array, temp, 0, array.length - 1);
    }
    
    private static void mergeSortRecursive(int[] array, int[] temp, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRecursive(array, temp, left, mid);
            mergeSortRecursive(array, temp, mid + 1, right);
            merge(array, temp, left, mid, right);
        }
    }
    
    private static void merge(int[] array, int[] temp, int left, int mid, int right) {
        // копіювати дані в тимчасовий масив
        for (int i = left; i <= right; i++) {
            temp[i] = array[i];
        }
        
        int i = left;      // індекс лівої підмасиву
        int j = mid + 1;   // індекс правої підмасиву
        int k = left;      // індекс злитого масиву
        
        // злиття підмасивів назад в array[left..right]
        while (i <= mid && j <= right) {
            if (temp[i] <= temp[j]) {
                array[k] = temp[i];
                i++;
            } else {
                array[k] = temp[j];
                j++;
            }
            k++;
        }
        
        // копіювати залишки лівої підмасиву, якщо є
        while (i <= mid) {
            array[k] = temp[i];
            i++;
            k++;
        }
        
        // копіювати залишки правої підмасиву, якщо є
        while (j <= right) {
            array[k] = temp[j];
            j++;
            k++;
        }
    }
    
    /**
     * алгоритм сортування купою (heap sort)
     */
    public static void heapSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        int n = array.length;
        
        // побудувати максимальну купу
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(array, n, i);
        }
        
        // витягувати елементи з купи один за одним
        for (int i = n - 1; i > 0; i--) {
            // перемістити поточний корінь в кінець
            swap(array, 0, i);
            
            // викликати heapify на зменшеній купі
            heapify(array, i, 0);
        }
    }
    
    private static void heapify(int[] array, int n, int i) {
        int largest = i;        // ініціалізувати largest як корінь
        int left = 2 * i + 1;   // лівий = 2*i + 1
        int right = 2 * i + 2;  // правий = 2*i + 2
        
        // якщо лівий дочірній елемент більший за корінь
        if (left < n && array[left] > array[largest]) {
            largest = left;
        }
        
        // якщо правий дочірній елемент більший за largest
        if (right < n && array[right] > array[largest]) {
            largest = right;
        }
        
        // якщо largest не корінь
        if (largest != i) {
            swap(array, i, largest);
            
            // рекурсивно heapify впливну піддереву
            heapify(array, n, largest);
        }
    }
    
    /**
     * алгоритм пошуку Кнута-Морріса-Пратта (KMP)
     */
    public static int kmpSearch(String text, String pattern) {
        if (text == null || pattern == null || pattern.length() == 0) {
            return -1;
        }
        
        int[] lps = computeLPSArray(pattern);
        int i = 0; // індекс для text
        int j = 0; // індекс для pattern
        
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }
            
            if (j == pattern.length()) {
                return i - j; // знайдено збіг
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return -1; // не знайдено
    }
    
    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        
        return lps;
    }
    
    /**
     * алгоритм пошуку Бойєра-Мура
     */
    public static int boyerMooreSearch(String text, String pattern) {
        if (text == null || pattern == null || pattern.length() == 0) {
            return -1;
        }
        
        int[] badChar = new int[256]; // таблиця поганих символів
        badCharacterHeuristic(pattern, badChar);
        
        int s = 0; // зсув pattern відносно text
        while (s <= (text.length() - pattern.length())) {
            int j = pattern.length() - 1;
            
            // зменшувати індекс j поки символи збігаються
            while (j >= 0 && pattern.charAt(j) == text.charAt(s + j)) {
                j--;
            }
            
            if (j < 0) {
                return s; // знайдено збіг
            } else {
                // зсунути pattern так, щоб поганий символ в text[i+j]
                // вирівнявся з останнім входженням цього символу в pattern
                s += Math.max(1, j - badChar[text.charAt(s + j)]);
            }
        }
        
        return -1; // не знайдено
    }
    
    private static void badCharacterHeuristic(String pattern, int[] badChar) {
        int n = pattern.length();
        
        // ініціалізувати всі входження як -1
        Arrays.fill(badChar, -1);
        
        // заповнити фактичне значення останнього входження
        for (int i = 0; i < n; i++) {
            badChar[pattern.charAt(i)] = i;
        }
    }
    
    /**
     * алгоритм динамічного програмування для задачі про рюкзак
     */
    public static int knapsack(int[] weights, int[] values, int capacity) {
        if (weights == null || values == null || weights.length != values.length || capacity < 0) {
            throw new IllegalArgumentException("Неправильні вхідні дані");
        }
        
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];
        
        // побудувати таблицю dp[][] знизу вгору
        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (i == 0 || w == 0) {
                    dp[i][w] = 0;
                } else if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(
                        values[i - 1] + dp[i - 1][w - weights[i - 1]],
                        dp[i - 1][w]
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        
        return dp[n][capacity];
    }
    
    /**
     * алгоритм динамічного програмування для найдовшої спільної підпослідовності
     */
    public static int longestCommonSubsequence(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }
        
        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        // побудувати таблицю dp[][] знизу вгору
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    dp[i][j] = 0;
                } else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
    
    /**
     * алгоритм динамічного програмування для обчислення чисел Фібоначчі
     */
    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n повинен бути невід'ємним");
        }
        
        if (n <= 1) {
            return n;
        }
        
        long[] dp = new long[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        
        return dp[n];
    }
    
    /**
     * алгоритм Евкліда для знаходження найбільшого спільного дільника
     */
    public static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        
        return a;
    }
    
    /**
     * обчислення найменшого спільного кратного
     */
    public static long lcm(long a, long b) {
        return Math.abs(a * b) / gcd(a, b);
    }
    
    /**
     * перевірка чи число є простим
     */
    public static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * решето Ератосфена для знаходження всіх простих чисел до n
     */
    public static List<Long> sieveOfEratosthenes(long n) {
        List<Long> primes = new ArrayList<>();
        if (n < 2) return primes;
        
        boolean[] isPrime = new boolean[(int)n + 1];
        Arrays.fill(isPrime, true);
        isPrime[0] = isPrime[1] = false;
        
        for (int i = 2; i * i <= n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) {
                primes.add((long)i);
            }
        }
        
        return primes;
    }
    
    /**
     * алгоритм бінарного пошуку
     */
    public static int binarySearch(int[] array, int target) {
        if (array == null) {
            return -1;
        }
        
        int left = 0;
        int right = array.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (array[mid] == target) {
                return mid;
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1;
    }
    
    /**
     * алгоритм інтерполяційного пошуку
     */
    public static int interpolationSearch(int[] array, int target) {
        if (array == null || array.length == 0) {
            return -1;
        }
        
        int left = 0;
        int right = array.length - 1;
        
        while (left <= right && target >= array[left] && target <= array[right]) {
            if (left == right) {
                if (array[left] == target) return left;
                return -1;
            }
            
            // знаходимо позицію за формулою інтерполяції
            int pos = left + ((target - array[left]) * (right - left)) / (array[right] - array[left]);
            
            if (array[pos] == target) {
                return pos;
            }
            
            if (array[pos] < target) {
                left = pos + 1;
            } else {
                right = pos - 1;
            }
        }
        
        return -1;
    }
    
    /**
     * алгоритм пошуку з експоненційним зростанням
     */
    public static int exponentialSearch(int[] array, int target) {
        if (array == null || array.length == 0) {
            return -1;
        }
        
        if (array[0] == target) {
            return 0;
        }
        
        // знаходимо діапазон для бінарного пошуку
        int i = 1;
        while (i < array.length && array[i] <= target) {
            i *= 2;
        }
        
        // виконуємо бінарний пошук в знайденому діапазоні
        return binarySearchRange(array, target, i / 2, Math.min(i, array.length - 1));
    }
    
    private static int binarySearchRange(int[] array, int target, int left, int right) {
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (array[mid] == target) {
                return mid;
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1;
    }
    
    /**
     * алгоритм сортування підрахунком
     */
    public static void countingSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        // знаходимо максимальне і мінімальне значення
        int max = Arrays.stream(array).max().orElse(0);
        int min = Arrays.stream(array).min().orElse(0);
        int range = max - min + 1;
        
        // створюємо масив підрахунків
        int[] count = new int[range];
        int[] output = new int[array.length];
        
        // підраховуємо входження кожного елемента
        for (int i = 0; i < array.length; i++) {
            count[array[i] - min]++;
        }
        
        // змінюємо count[i] так, щоб він містив актуальну позицію
        // цього елемента в output[]
        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }
        
        // будуємо вихідний масив
        for (int i = array.length - 1; i >= 0; i--) {
            output[count[array[i] - min] - 1] = array[i];
            count[array[i] - min]--;
        }
        
        // копіюємо відсортований масив назад в оригінал
        System.arraycopy(output, 0, array, 0, array.length);
    }
    
    /**
     * алгоритм сортування корзинами (bucket sort)
     */
    public static void bucketSort(float[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        int n = array.length;
        List<List<Float>> buckets = new ArrayList<>(n);
        
        // створюємо порожні корзини
        for (int i = 0; i < n; i++) {
            buckets.add(new ArrayList<>());
        }
        
        // розподіляємо елементи по корзинам
        for (int i = 0; i < n; i++) {
            int bucketIndex = (int) (n * array[i]);
            if (bucketIndex >= n) bucketIndex = n - 1;
            buckets.get(bucketIndex).add(array[i]);
        }
        
        // сортуємо кожну корзину
        for (List<Float> bucket : buckets) {
            Collections.sort(bucket);
        }
        
        // збираємо всі елементи з корзин
        int index = 0;
        for (List<Float> bucket : buckets) {
            for (Float value : bucket) {
                array[index++] = value;
            }
        }
    }
    
    /**
     * алгоритм сортування поразрядами (radix sort)
     */
    public static void radixSort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        // знаходимо максимальне число для визначення кількості цифр
        int max = Arrays.stream(array).max().orElse(0);
        
        // виконуємо сортування підрахунком для кожної цифри
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortForRadix(array, exp);
        }
    }
    
    private static void countingSortForRadix(int[] array, int exp) {
        int n = array.length;
        int[] output = new int[n];
        int[] count = new int[10];
        
        // підраховуємо входження цифр
        for (int i = 0; i < n; i++) {
            count[(array[i] / exp) % 10]++;
        }
        
        // змінюємо count[i] так, щоб він містив актуальну позицію
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        
        // будуємо вихідний масив
        for (int i = n - 1; i >= 0; i--) {
            output[count[(array[i] / exp) % 10] - 1] = array[i];
            count[(array[i] / exp) % 10]--;
        }
        
        // копіюємо відсортований масив назад в оригінал
        System.arraycopy(output, 0, array, 0, n);
    }
}