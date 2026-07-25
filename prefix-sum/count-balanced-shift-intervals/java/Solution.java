import java.util.*;

/*
 * Title: Count Balanced Shift Intervals
 * Difficulty: Medium
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A company records employee shift activity for a single day as an array hours,
 * where hours[i] is the number of hours worked during the i-th time block.
 * Management defines a time block as heavy if hours[i] >= threshold, otherwise it is light.
 *
 * An interval is called balanced if it contains the same number of heavy blocks
 * and light blocks. The task is to return the total number of balanced intervals
 * in the array.
 *
 * Formally, count the number of pairs (l, r) such that 0 <= l <= r < n and in the
 * subarray hours[l...r], the number of indices with hours[i] >= threshold is equal
 * to the number of indices with hours[i] < threshold.
 *
 * Efficient Idea:
 * Convert each element into:
 *   +1 if hours[i] >= threshold   (heavy)
 *   -1 if hours[i] < threshold    (light)
 *
 * Then a subarray is balanced exactly when its transformed sum is 0.
 *
 * Using prefix sums:
 * Let prefix[i] be the sum of transformed values from index 0 to i.
 * A subarray (l...r) has sum 0 if:
 *   prefix[r] == prefix[l - 1]
 *
 * Therefore, for every current prefix sum, the number of earlier equal prefix sums
 * tells us how many balanced subarrays end at the current position.
 *
 * We count frequencies of prefix sums with a HashMap.
 *
 * Constraints:
 * - 1 <= n == hours.length <= 2 * 10^5
 * - 0 <= hours[i] <= 10^9
 * - 0 <= threshold <= 10^9
 *
 * Example 1:
 * Input: hours = [6, 3, 8, 2, 7], threshold = 5
 * Transformed: [+1, -1, +1, -1, +1]
 * Balanced intervals count = 4
 *
 * Example 2:
 * Input: hours = [4, 4, 9, 1], threshold = 4
 * Transformed: [+1, +1, +1, -1]
 * The mathematically correct balanced interval count is 1:
 *   [2, 3]
 *
 * Note:
 * The example statement claims the answer is 2 and includes [0, 3], but:
 *   [0, 3] contains 3 heavy blocks and 1 light block,
 * which is not balanced.
 * Therefore, the correct answer for Example 2 is 1.
 */

public class Solution {

    /**
     * Counts the number of balanced intervals in the given hours array.
     *
     * A block is treated as:
     * - heavy  -> +1 if hours[i] >= threshold
     * - light  -> -1 if hours[i] < threshold
     *
     * A subarray is balanced when the number of heavy and light blocks is equal,
     * which means the transformed subarray sum is 0.
     *
     * We use prefix sums and a frequency map:
     * - If the same prefix sum appears multiple times, then the subarray between
     *   any two equal prefix sum positions has total sum 0.
     *
     * @param hours the array of worked hours for each time block
     * @param threshold the threshold that separates heavy and light blocks
     * @return the total number of balanced intervals
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long countBalancedIntervals(int[] hours, int threshold) {
        // This map stores:
        // prefix sum value -> how many times we have seen this prefix sum so far
        Map<Integer, Integer> prefixFrequency = new HashMap<>();

        // Important initialization:
        // Before processing any element, the prefix sum is 0 once.
        // This allows us to count subarrays starting from index 0.
        prefixFrequency.put(0, 1);

        // Running prefix sum of transformed values (+1 / -1).
        int prefixSum = 0;

        // We use long because the number of subarrays can be large:
        // in the worst case it can be about n * (n + 1) / 2.
        long balancedCount = 0L;

        // Process each time block from left to right.
        for (int value : hours) {
            // Step 1:
            // Convert the current block into +1 or -1.
            // +1 means heavy, -1 means light.
            if (value >= threshold) {
                prefixSum += 1;
            } else {
                prefixSum -= 1;
            }

            // Step 2:
            // If this prefix sum has appeared before, then each earlier occurrence
            // forms one balanced subarray ending at the current index.
            //
            // Why?
            // Suppose current prefix sum is S.
            // If an earlier prefix sum was also S, then the sum of the elements
            // between those two positions is 0.
            int seen = prefixFrequency.getOrDefault(prefixSum, 0);
            balancedCount += seen;

            // Step 3:
            // Record that we have now seen this prefix sum one more time.
            prefixFrequency.put(prefixSum, seen + 1);
        }

        return balancedCount;
    }

    /**
     * Builds and returns the transformed representation of the hours array.
     *
     * Each element becomes:
     * - +1 if hours[i] >= threshold
     * - -1 otherwise
     *
     * This helper is useful for demonstration and learning.
     *
     * @param hours the original hours array
     * @param threshold the threshold for heavy vs light
     * @return a new array containing only +1 and -1 values
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] transformHours(int[] hours, int threshold) {
        int[] transformed = new int[hours.length];

        for (int i = 0; i < hours.length; i++) {
            transformed[i] = (hours[i] >= threshold) ? 1 : -1;
        }

        return transformed;
    }

    /**
     * Converts an int array into a readable string.
     *
     * This helper avoids relying on external utilities beyond the standard library
     * and keeps the demonstration beginner-friendly.
     *
     * @param array the array to convert
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * The demonstration includes:
     * - the original hours array
     * - the threshold
     * - the transformed +1/-1 array
     * - the computed balanced interval count
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) per demonstration case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] hours1 = {6, 3, 8, 2, 7};
        int threshold1 = 5;
        int[] transformed1 = solution.transformHours(hours1, threshold1);
        long result1 = solution.countBalancedIntervals(hours1, threshold1);

        System.out.println("Example 1");
        System.out.println("hours = " + solution.arrayToString(hours1));
        System.out.println("threshold = " + threshold1);
        System.out.println("transformed = " + solution.arrayToString(transformed1));
        System.out.println("balanced intervals = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] hours2 = {4, 4, 9, 1};
        int threshold2 = 4;
        int[] transformed2 = solution.transformHours(hours2, threshold2);
        long result2 = solution.countBalancedIntervals(hours2, threshold2);

        System.out.println("Example 2");
        System.out.println("hours = " + solution.arrayToString(hours2));
        System.out.println("threshold = " + threshold2);
        System.out.println("transformed = " + solution.arrayToString(transformed2));
        System.out.println("balanced intervals = " + result2);
        System.out.println("Correct mathematical result = 1");
        System.out.println("Note: The provided statement's claimed answer 2 is inconsistent with the definition.");
        System.out.println();

        // Additional quick sanity checks
        int[] hours3 = {1};
        int threshold3 = 1;
        System.out.println("Sanity Check 1");
        System.out.println("hours = " + solution.arrayToString(hours3));
        System.out.println("threshold = " + threshold3);
        System.out.println("balanced intervals = " + solution.countBalancedIntervals(hours3, threshold3));
        System.out.println("Expected = 0");
        System.out.println();

        int[] hours4 = {2, 1, 2, 1};
        int threshold4 = 2;
        System.out.println("Sanity Check 2");
        System.out.println("hours = " + solution.arrayToString(hours4));
        System.out.println("threshold = " + threshold4);
        System.out.println("transformed = " + solution.arrayToString(solution.transformHours(hours4, threshold4)));
        System.out.println("balanced intervals = " + solution.countBalancedIntervals(hours4, threshold4));
        System.out.println("Expected = 4");
    }
}