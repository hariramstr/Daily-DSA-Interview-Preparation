```java
/*
 * Title: Minimum Unique Colors in Every Window
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `colors` of length `n`, where `colors[i]` represents the color
 * of the i-th paint bucket (as a positive integer). You are also given an integer `k`
 * representing the size of a sliding window.
 *
 * For each contiguous subarray of size `k`, compute the number of **distinct** colors present.
 * Return the **minimum** number of distinct colors found across all windows of size `k`.
 *
 * Additionally, return the **starting index** of the first window that achieves this minimum.
 * If multiple windows tie for the minimum, return the smallest starting index.
 *
 * Your solution must run in **O(n)** time.
 *
 * Constraints:
 * - 1 <= k <= n <= 100000
 * - 1 <= colors[i] <= 10^6
 *
 * Example 1:
 * Input: colors = [1, 2, 1, 3, 2, 1, 1], k = 3
 * Output: [2, 0]
 * Explanation:
 * - [1,2,1] → 2 distinct  (index 0)
 * - [2,1,3] → 3 distinct  (index 1)
 * - [1,3,2] → 3 distinct  (index 2)
 * - [3,2,1] → 3 distinct  (index 3)
 * - [2,1,1] → 2 distinct  (index 4)
 * Minimum is 2, first achieved at index 0, so output is [2, 0].
 *
 * Example 2:
 * Input: colors = [4, 4, 4, 1, 2, 3], k = 2
 * Output: [1, 0]
 * Explanation:
 * - [4,4] → 1 distinct  (index 0)
 * - [4,4] → 1 distinct  (index 1)
 * - [4,1] → 2 distinct  (index 2)
 * - [1,2] → 2 distinct  (index 3)
 * - [2,3] → 2 distinct  (index 4)
 * Minimum is 1, first achieved at index 0, so output is [1, 0].
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Solution class for the "Minimum Unique Colors in Every Window" problem.
 *
 * <p>Core Idea (Sliding Window):
 * We maintain a HashMap that tracks the frequency of each color currently inside
 * the window of size k. As the window slides one step to the right:
 *   1. We add the new right element (increment its count).
 *   2. We remove the old left element (decrement its count; if it reaches 0, remove the key).
 * The number of distinct colors at any moment is simply the size of the HashMap.
 * This gives us O(n) time overall.
 */
public class Solution {

    /**
     * Finds the minimum number of distinct colors across all windows of size k,
     * and the starting index of the first window that achieves this minimum.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>Initialize a frequency map and populate it for the very first window [0, k-1].</li>
     *   <li>Record the distinct count of the first window as the current minimum.</li>
     *   <li>Slide the window from index 1 to (n - k), updating the map in O(1) per step.</li>
     *   <li>After each slide, check if the new distinct count is strictly less than the
     *       current minimum; if so, update the minimum and record the new starting index.</li>
     *   <li>Return [minDistinct, bestStartIndex].</li>
     * </ol>
     *
     * @param colors the array of paint-bucket colors (positive integers)
     * @param k      the window size
     * @return an int array of length 2: [minDistinctColors, firstWindowStartIndex]
     *
     * Time Complexity:  O(n) — each element is added and removed from the map at most once.
     * Space Complexity: O(k) — the map holds at most k distinct entries at any time.
     */
    public int[] minUniqueColors(int[] colors, int k) {
        int n = colors.length;

        // -----------------------------------------------------------------------
        // Edge case: if k == 0 or the array is empty, return a safe default.
        // (The constraints guarantee 1 <= k <= n, so this is just defensive code.)
        // -----------------------------------------------------------------------
        if (n == 0 || k == 0) {
            return new int[]{0, 0};
        }

        // -----------------------------------------------------------------------
        // Step 1: Build a frequency map for the FIRST window [0, k-1].
        //
        // frequencyMap maps each color to how many times it appears in the
        // current window. The number of distinct colors equals frequencyMap.size().
        // -----------------------------------------------------------------------
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (int i = 0; i < k; i++) {
            // For each color in the first window, increment its count.
            // getOrDefault returns 0 if the color is not yet in the map.
            frequencyMap.put(colors[i], frequencyMap.getOrDefault(colors[i], 0) + 1);
        }

        // -----------------------------------------------------------------------
        // Step 2: Initialize the minimum tracking variables using the first window.
        //
        // minDistinct  — the fewest distinct colors seen so far across all windows.
        // bestStart    — the starting index of the first window achieving minDistinct.
        // -----------------------------------------------------------------------
        int minDistinct = frequencyMap.size(); // distinct count of window starting at index 0
        int bestStart   = 0;                   // first window starts at index 0

        // -----------------------------------------------------------------------
        // Step 3: Slide the window from starting index 1 to (n - k).
        //
        // When the window moves from start (i-1) to start (i):
        //   • The NEW element entering the window is colors[i + k - 1]  (right edge).
        //   • The OLD element leaving the window is colors[i - 1]        (left edge).
        // -----------------------------------------------------------------------
        for (int i = 1; i <= n - k; i++) {

            // -- 3a. Add the new right element to the frequency map. --
            int newColor = colors[i + k - 1];
            frequencyMap.put(newColor, frequencyMap.getOrDefault(newColor, 0) + 1);
            // If newColor was not in the map before, frequencyMap.size() just increased by 1.

            // -- 3b. Remove the old left element from the frequency map. --
            int oldColor = colors[i - 1];
            int oldCount = frequencyMap.get(oldColor); // guaranteed to exist (count >= 1)

            if (oldCount == 1) {
                // This was the last occurrence of oldColor in the window.
                // Removing the key reduces the distinct count by 1.
                frequencyMap.remove(oldColor);
            } else {
                // oldColor still appears in the window; just decrement its count.
                frequencyMap.put(oldColor, oldCount - 1);
            }

            // -- 3c. Check if the current window sets a new minimum. --
            int currentDistinct = frequencyMap.size();

            if (currentDistinct < minDistinct) {
                // Strictly smaller → update both the minimum and the best starting index.
                minDistinct = currentDistinct;
                bestStart   = i;
                // Note: we only update on STRICT improvement, so the first (smallest)
                // index that achieves the minimum is naturally preserved.
            }
            // If currentDistinct == minDistinct, we do NOT update bestStart,
            // because we want the SMALLEST starting index (which we already recorded).
        }

        // -----------------------------------------------------------------------
        // Step 4: Return the result as a two-element array.
        //         result[0] = minimum distinct color count
        //         result[1] = starting index of the first window achieving that minimum
        // -----------------------------------------------------------------------
        return new int[]{minDistinct, bestStart};
    }

    // ==========================================================================
    // Helper: pretty-print an int array for demonstration purposes.
    // ==========================================================================

    /**
     * Converts an int array to a readable string like "[2, 0]".
     *
     * @param arr the array to format
     * @return a bracketed, comma-separated string representation
     *
     * Time Complexity:  O(m) where m is the length of arr.
     * Space Complexity: O(m) for the StringBuilder.
     */
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // ==========================================================================
    // Main method — demonstrates the solution with the provided examples.
    // ==========================================================================

    /**
     * Entry point. Runs the solution against the sample inputs from the problem
     * description and prints the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1
        // colors = [1, 2, 1, 3, 2, 1, 1], k = 3
        //
        // Windows:
        //   [1,2,1]   → distinct: {1,2}       → 2   (start index 0)
        //   [2,1,3]   → distinct: {2,1,3}     → 3   (start index 1)
        //   [1,3,2]   → distinct: {1,3,2}     → 3   (start index 2)
        //   [3,2,1]   → distinct: {3,2,1}     → 3   (start index 3)
        //   [2,1,1]   → distinct: {2,1}       → 2   (start index 4)
        //
        // Minimum distinct = 2, first achieved at start index 0.
        // Expected output: [2, 0]
        // -----------------------------------------------------------------------
        int[] colors1 = {1, 2, 1, 3, 2, 1, 1};
        int k1 = 3;
        int[] result1 = solution.minUniqueColors(colors1, k1);
        System.out.println("Example 1:");
        System.out.println("  Input : colors = " + arrayToString(colors1) + ", k = " + k1);
        System.out.println("  Output: " + arrayToString(result1));
        System.out.println("  Expected: [2, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2
        // colors = [4, 4, 4, 1, 2, 3], k = 2
        //
        // Windows:
        //   [4,4]  → distinct: {4}    → 1   (start index 0)
        //   [4,4]  → distinct: {4}    → 1   (start index 1)
        //   [4,1]  → distinct: {4,1}  → 2   (start index 2)
        //   [1,2]  → distinct: {1,2}  → 2   (start index 3)
        //   [2,3]  → distinct: {2,3}  → 2   (start index 4)
        //
        // Minimum distinct = 1, first achieved at start index 0.
        // Expected output: [1, 0]
        // -----------------------------------------------------------------------
        int[] colors2 = {4, 4, 4, 1, 2, 3};
        int k2 = 2;
        int[] result2 = solution.minUniqueColors(colors2, k2);
        System.out.println("Example 2:");
        System.out.println("  Input : colors = " + arrayToString(colors2) + ", k = " + k2);
        System.out.println("  Output: " + arrayToString(result2));
        System.out.println("  Expected: [1, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3 — window size equals array length (single window)
        // colors = [5, 3, 5, 3], k = 4
        //
        // Only one window: [5,3,5,3] → distinct: {5,3} → 2
        // Expected output: [2, 0]
        // -----------------------------------------------------------------------
        int[] colors3 = {5, 3, 5, 3};
        int k3 = 4;
        int[] result3 = solution.minUniqueColors(colors3, k3);
        System.out.println("Example 3 (single window):");
        System.out.println("  Input : colors = " + arrayToString(colors3) + ", k = " + k3);
        System.out.println("  Output: " + arrayToString(result3));
        System.out.println("  Expected: [2, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4 — all same color
        // colors = [7, 7, 7, 7], k = 2
        //
        // Windows:
        //   [7,7] → 1 distinct  (index 0)
        //   [7,7] → 1 distinct  (index 1)
        //   [7,7] → 1 distinct  (index 2)
        //
        // Minimum = 1, first at index 0.
        // Expected output: [1, 0]
        // -----------------------------------------------------------------------
        int[] colors4 = {7, 7, 7, 7};
        int k4 = 2;
        int[] result4 = solution.minUniqueColors(colors4, k4);
        System.out.println("Example 4 (all same color):");
        System.out.println("  Input : colors = " + arrayToString(colors4) + ", k = " + k4);
        System.out.println("  Output: " + arrayToString(result4));
        System.out.println("  Expected: [1, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5 — all distinct colors
        // colors = [1, 2, 3, 4, 5], k = 3
        //
        // Windows:
        //   [1,2,3] → 3 distinct  (index 0)
        //   [2,3,4] → 3 distinct  (index 1)
        //   [3,4,5] → 3 distinct  (index 2)
        //
        // Minimum = 3, first at index 0.
        // Expected output: [3, 0]
        // -----------------------------------------------------------------------
        int[] colors5 = {1, 2, 3, 4, 5};
        int k5 = 3;
        int[] result5 = solution.minUniqueColors(colors5, k5);
        System.out.println("Example 5 (all distinct colors):");
        System.out.println("  Input : colors = " + arrayToString(colors5) + ", k = " + k5);
        System.out.println("  Output: " + arrayToString(result5));
        System.out.println("  Expected: [3, 0]");
    }
}
```