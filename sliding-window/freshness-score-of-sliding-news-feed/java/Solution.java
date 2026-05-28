/*
 * Title: Freshness Score of a Sliding News Feed
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are building a news aggregator that monitors a stream of articles.
 * Each article has a freshness score represented as a non-negative integer.
 * Your task is to find the maximum average freshness score across any contiguous
 * window of exactly k articles in the feed.
 *
 * Given an integer array scores where scores[i] represents the freshness score
 * of the i-th article, and an integer k, return the maximum average value of any
 * contiguous subarray of length k. Your answer will be accepted if it is within
 * 10^-5 of the actual answer.
 *
 * Constraints:
 * - 1 <= k <= scores.length <= 10^5
 * - 0 <= scores[i] <= 10^4
 *
 * Example 1:
 * Input: scores = [3, 7, 2, 9, 4, 6, 1], k = 3
 * Output: 6.66667
 * Explanation: The subarray [9, 4, 6] has a sum of 19, giving an average of
 * 19/3 ≈ 6.66667, which is the highest among all windows of size 3.
 *
 * Example 2:
 * Input: scores = [5, 5, 5, 5], k = 2
 * Output: 5.00000
 * Explanation: Every window of size 2 has a sum of 10 and an average of 5.0.
 */

public class Solution {

    /**
     * Finds the maximum average of any contiguous subarray of length k
     * using the sliding window technique.
     *
     * <p>The sliding window approach works by:
     * 1. Computing the sum of the first window of size k.
     * 2. Sliding the window one position to the right at a time:
     *    - Add the new element entering the window (right side).
     *    - Remove the element leaving the window (left side).
     * 3. Track the maximum sum seen across all windows.
     * 4. Divide the maximum sum by k to get the maximum average.
     *
     * <p>This avoids recomputing the sum from scratch for each window,
     * achieving O(n) time complexity instead of O(n*k).
     *
     * @param scores the array of freshness scores (non-negative integers)
     * @param k      the fixed window size (number of articles per window)
     * @return the maximum average freshness score over any window of size k
     *
     * Time Complexity:  O(n) — we traverse the array exactly once after the
     *                   initial window setup, where n = scores.length
     * Space Complexity: O(1) — only a constant number of extra variables are used
     */
    public double findMaxAverage(int[] scores, int k) {

        // ---------------------------------------------------------------
        // STEP 1: Compute the sum of the very first window (indices 0..k-1)
        // ---------------------------------------------------------------
        // We need a starting point before we begin sliding.
        // Initialize windowSum to 0 and accumulate the first k elements.
        double windowSum = 0;
        for (int i = 0; i < k; i++) {
            // Add each score in the first window to windowSum
            windowSum += scores[i];
            // Example 1 trace (k=3): i=0 → windowSum=3, i=1 → 10, i=2 → 12
            // Example 2 trace (k=2): i=0 → windowSum=5, i=1 → 10
        }

        // ---------------------------------------------------------------
        // STEP 2: Record the first window's sum as the current maximum sum
        // ---------------------------------------------------------------
        // maxSum holds the largest window sum we have seen so far.
        // We start with the first window's sum as our baseline.
        double maxSum = windowSum;
        // Example 1: maxSum = 12  (window [3,7,2])
        // Example 2: maxSum = 10  (window [5,5])

        // ---------------------------------------------------------------
        // STEP 3: Slide the window from index k to scores.length - 1
        // ---------------------------------------------------------------
        // At each step, the window covers indices [i-k+1 .. i].
        // We add scores[i] (new right element) and subtract scores[i-k]
        // (old left element that just fell out of the window).
        for (int i = k; i < scores.length; i++) {

            // --- Add the new element entering the window on the right ---
            // scores[i] is the article just entering the window.
            windowSum += scores[i];

            // --- Remove the element leaving the window on the left ---
            // scores[i - k] is the article that is no longer in the window.
            // For example, when i=3 and k=3, we remove scores[0].
            windowSum -= scores[i - k];

            // Example 1 detailed trace (k=3):
            //   i=3: windowSum = 12 + 9 - 3 = 18  → window [7,2,9]
            //   i=4: windowSum = 18 + 4 - 7 = 15  → window [2,9,4]
            //   i=5: windowSum = 15 + 6 - 2 = 19  → window [9,4,6]  ← maximum!
            //   i=6: windowSum = 19 + 1 - 9 = 11  → window [4,6,1]

            // Example 2 detailed trace (k=2):
            //   i=2: windowSum = 10 + 5 - 5 = 10  → window [5,5]
            //   i=3: windowSum = 10 + 5 - 5 = 10  → window [5,5]

            // --- Update the maximum sum if the current window is better ---
            if (windowSum > maxSum) {
                maxSum = windowSum;
            }
        }
        // After Example 1: maxSum = 19
        // After Example 2: maxSum = 10

        // ---------------------------------------------------------------
        // STEP 4: Compute and return the maximum average
        // ---------------------------------------------------------------
        // Divide the maximum sum by k to convert sum → average.
        // We cast to double to ensure floating-point division.
        // Example 1: 19.0 / 3 = 6.66667
        // Example 2: 10.0 / 2 = 5.00000
        return maxSum / k;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through both examples from the problem description and
     * prints the results to verify correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the instance method
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1
        // Input:  scores = [3, 7, 2, 9, 4, 6, 1], k = 3
        // Expected Output: 6.66667
        // Explanation: Window [9, 4, 6] → sum=19 → avg=19/3≈6.66667
        // ---------------------------------------------------------------
        int[] scores1 = {3, 7, 2, 9, 4, 6, 1};
        int k1 = 3;
        double result1 = solution.findMaxAverage(scores1, k1);
        System.out.println("=== Example 1 ===");
        System.out.println("Input scores: [3, 7, 2, 9, 4, 6, 1]");
        System.out.println("Window size k: " + k1);
        // Format to 5 decimal places to match expected output style
        System.out.printf("Maximum Average: %.5f%n", result1);
        System.out.println("Expected:       6.66667");
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2
        // Input:  scores = [5, 5, 5, 5], k = 2
        // Expected Output: 5.00000
        // Explanation: Every window of size 2 → sum=10 → avg=5.0
        // ---------------------------------------------------------------
        int[] scores2 = {5, 5, 5, 5};
        int k2 = 2;
        double result2 = solution.findMaxAverage(scores2, k2);
        System.out.println("=== Example 2 ===");
        System.out.println("Input scores: [5, 5, 5, 5]");
        System.out.println("Window size k: " + k2);
        System.out.printf("Maximum Average: %.5f%n", result2);
        System.out.println("Expected:       5.00000");
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Edge Case: Single element array, k = 1
        // Input:  scores = [7], k = 1
        // Expected Output: 7.00000
        // ---------------------------------------------------------------
        int[] scores3 = {7};
        int k3 = 1;
        double result3 = solution.findMaxAverage(scores3, k3);
        System.out.println("=== Edge Case: Single Element ===");
        System.out.println("Input scores: [7]");
        System.out.println("Window size k: " + k3);
        System.out.printf("Maximum Average: %.5f%n", result3);
        System.out.println("Expected:       7.00000");
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Edge Case: k equals array length
        // Input:  scores = [1, 2, 3, 4, 5], k = 5
        // Expected Output: 3.00000  (only one window: sum=15, avg=3.0)
        // ---------------------------------------------------------------
        int[] scores4 = {1, 2, 3, 4, 5};
        int k4 = 5;
        double result4 = solution.findMaxAverage(scores4, k4);
        System.out.println("=== Edge Case: k Equals Array Length ===");
        System.out.println("Input scores: [1, 2, 3, 4, 5]");
        System.out.println("Window size k: " + k4);
        System.out.printf("Maximum Average: %.5f%n", result4);
        System.out.println("Expected:       3.00000");
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Edge Case: All zeros
        // Input:  scores = [0, 0, 0, 0], k = 2
        // Expected Output: 0.00000
        // ---------------------------------------------------------------
        int[] scores5 = {0, 0, 0, 0};
        int k5 = 2;
        double result5 = solution.findMaxAverage(scores5, k5);
        System.out.println("=== Edge Case: All Zeros ===");
        System.out.println("Input scores: [0, 0, 0, 0]");
        System.out.println("Window size k: " + k5);
        System.out.printf("Maximum Average: %.5f%n", result5);
        System.out.println("Expected:       0.00000");
    }
}