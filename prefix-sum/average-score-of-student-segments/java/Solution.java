/*
 * Title: Average Score of Student Segments
 *
 * Problem Description:
 * A teacher has recorded the scores of n students in a linear array scores.
 * She wants to evaluate the performance of students over multiple contiguous
 * segments of the class roster. For each query, she provides a range [left, right]
 * (0-indexed, inclusive) and wants to know the average score (as a floating point
 * number) of students in that segment.
 *
 * Given an integer array scores and a 2D integer array queries where
 * queries[i] = [left, right], return an array of floating point numbers where
 * the i-th element is the average score of students from index left to index
 * right (inclusive).
 *
 * Constraints:
 * - 1 <= scores.length <= 10^5
 * - 0 <= scores[i] <= 100
 * - 1 <= queries.length <= 10^4
 * - 0 <= queries[i][0] <= queries[i][1] < scores.length
 *
 * Example 1:
 * Input: scores = [80, 90, 70, 60, 85], queries = [[0, 2], [1, 4]]
 * Output: [80.0, 76.25]
 * Explanation:
 *   For query [0,2]: (80+90+70)/3 = 80.0
 *   For query [1,4]: (90+70+60+85)/4 = 76.25
 *
 * Example 2:
 * Input: scores = [50, 100, 40, 90, 20], queries = [[0, 4], [2, 3]]
 * Output: [60.0, 65.0]
 * Explanation:
 *   For query [0,4]: (50+100+40+90+20)/5 = 60.0
 *   For query [2,3]: (40+90)/2 = 65.0
 */

import java.util.Arrays;

/**
 * Solution class for the "Average Score of Student Segments" problem.
 *
 * <p>Core Idea (Prefix Sum):
 * Instead of summing up elements for every query from scratch (which would be slow),
 * we precompute a "prefix sum" array. The prefix sum array stores the cumulative
 * sum of scores up to each index. Then, any range sum [left, right] can be
 * computed in O(1) time using:
 *   rangeSum = prefixSum[right + 1] - prefixSum[left]
 *
 * <p>This is a classic technique to answer multiple range-sum queries efficiently.
 */
public class Solution {

    /**
     * Computes the average scores for each query range using the Prefix Sum technique.
     *
     * <p>Algorithm Steps:
     * 1. Build a prefix sum array of size (n + 1), where prefixSum[i] holds the
     *    sum of scores[0] through scores[i-1].
     * 2. For each query [left, right]:
     *    a. Compute the range sum: prefixSum[right + 1] - prefixSum[left]
     *    b. Compute the number of elements: right - left + 1
     *    c. Divide to get the average (use double division)
     * 3. Store each average in the result array and return it.
     *
     * @param scores  the array of student scores (0-indexed)
     * @param queries a 2D array where each row is [left, right] representing a range query
     * @return a double array where the i-th element is the average score for the i-th query
     *
     * Time Complexity:  O(n + q), where n = scores.length and q = queries.length.
     *                   Building the prefix sum takes O(n); each query is answered in O(1).
     * Space Complexity: O(n) for the prefix sum array (result array O(q) not counted as extra).
     */
    public double[] averageScores(int[] scores, int[][] queries) {

        // -----------------------------------------------------------------------
        // Step 1: Determine the length of the scores array.
        // -----------------------------------------------------------------------
        int n = scores.length;

        // -----------------------------------------------------------------------
        // Step 2: Build the prefix sum array.
        //
        // We create an array of size (n + 1) so that:
        //   prefixSum[0] = 0            (no elements summed yet — a sentinel value)
        //   prefixSum[1] = scores[0]
        //   prefixSum[2] = scores[0] + scores[1]
        //   ...
        //   prefixSum[i] = scores[0] + scores[1] + ... + scores[i-1]
        //
        // Using a long array prevents integer overflow for large inputs
        // (up to 10^5 elements each up to 100 → max sum = 10^7, fits in int,
        //  but long is safer and a good habit).
        // -----------------------------------------------------------------------
        long[] prefixSum = new long[n + 1];

        // prefixSum[0] is already 0 by default in Java (array initialization).
        for (int i = 1; i <= n; i++) {
            // Each entry is the previous cumulative sum plus the current score.
            prefixSum[i] = prefixSum[i - 1] + scores[i - 1];
        }

        // -----------------------------------------------------------------------
        // Step 3: Prepare the result array — one answer per query.
        // -----------------------------------------------------------------------
        double[] result = new double[queries.length];

        // -----------------------------------------------------------------------
        // Step 4: Answer each query in O(1) using the prefix sum array.
        // -----------------------------------------------------------------------
        for (int i = 0; i < queries.length; i++) {

            // Extract the left and right boundaries of the current query.
            int left  = queries[i][0];
            int right = queries[i][1];

            // ------------------------------------------------------------------
            // Compute the range sum using the prefix sum formula:
            //   sum(left, right) = prefixSum[right + 1] - prefixSum[left]
            //
            // Why does this work?
            //   prefixSum[right + 1] = sum of scores[0..right]
            //   prefixSum[left]      = sum of scores[0..left-1]
            //   Subtracting removes the part before 'left', leaving scores[left..right].
            // ------------------------------------------------------------------
            long rangeSum = prefixSum[right + 1] - prefixSum[left];

            // ------------------------------------------------------------------
            // Compute the number of elements in the range [left, right].
            // For example, [1, 4] has 4 - 1 + 1 = 4 elements.
            // ------------------------------------------------------------------
            int count = right - left + 1;

            // ------------------------------------------------------------------
            // Compute the average as a double.
            // We cast rangeSum to double before dividing to ensure floating-point
            // division (not integer division).
            // ------------------------------------------------------------------
            result[i] = (double) rangeSum / count;
        }

        // -----------------------------------------------------------------------
        // Step 5: Return the array of averages.
        // -----------------------------------------------------------------------
        return result;
    }

    // ===========================================================================
    // Main method — demonstrates the solution with the provided examples.
    // ===========================================================================

    /**
     * Entry point for demonstration purposes.
     * Runs both examples from the problem description and prints the results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1
        // scores  = [80, 90, 70, 60, 85]
        // queries = [[0, 2], [1, 4]]
        // Expected output: [80.0, 76.25]
        //
        // Trace:
        //   prefixSum = [0, 80, 170, 240, 300, 385]
        //
        //   Query [0, 2]:
        //     rangeSum = prefixSum[3] - prefixSum[0] = 240 - 0 = 240
        //     count    = 2 - 0 + 1 = 3
        //     average  = 240 / 3 = 80.0  ✓
        //
        //   Query [1, 4]:
        //     rangeSum = prefixSum[5] - prefixSum[1] = 385 - 80 = 305
        //     count    = 4 - 1 + 1 = 4
        //     average  = 305 / 4 = 76.25  ✓
        // -----------------------------------------------------------------------
        int[] scores1  = {80, 90, 70, 60, 85};
        int[][] queries1 = {{0, 2}, {1, 4}};
        double[] result1 = solution.averageScores(scores1, queries1);

        System.out.println("=== Example 1 ===");
        System.out.println("scores  : " + Arrays.toString(scores1));
        System.out.println("queries : " + Arrays.deepToString(queries1));
        System.out.println("Output  : " + Arrays.toString(result1));
        System.out.println("Expected: [80.0, 76.25]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2
        // scores  = [50, 100, 40, 90, 20]
        // queries = [[0, 4], [2, 3]]
        // Expected output: [60.0, 65.0]
        //
        // Trace:
        //   prefixSum = [0, 50, 150, 190, 280, 300]
        //
        //   Query [0, 4]:
        //     rangeSum = prefixSum[5] - prefixSum[0] = 300 - 0 = 300
        //     count    = 4 - 0 + 1 = 5
        //     average  = 300 / 5 = 60.0  ✓
        //
        //   Query [2, 3]:
        //     rangeSum = prefixSum[4] - prefixSum[2] = 280 - 150 = 130
        //     count    = 3 - 2 + 1 = 2
        //     average  = 130 / 2 = 65.0  ✓
        // -----------------------------------------------------------------------
        int[] scores2  = {50, 100, 40, 90, 20};
        int[][] queries2 = {{0, 4}, {2, 3}};
        double[] result2 = solution.averageScores(scores2, queries2);

        System.out.println("=== Example 2 ===");
        System.out.println("scores  : " + Arrays.toString(scores2));
        System.out.println("queries : " + Arrays.deepToString(queries2));
        System.out.println("Output  : " + Arrays.toString(result2));
        System.out.println("Expected: [60.0, 65.0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional edge-case: single element query
        // scores  = [42]
        // queries = [[0, 0]]
        // Expected output: [42.0]
        // -----------------------------------------------------------------------
        int[] scores3  = {42};
        int[][] queries3 = {{0, 0}};
        double[] result3 = solution.averageScores(scores3, queries3);

        System.out.println("=== Edge Case: Single Element ===");
        System.out.println("scores  : " + Arrays.toString(scores3));
        System.out.println("queries : " + Arrays.deepToString(queries3));
        System.out.println("Output  : " + Arrays.toString(result3));
        System.out.println("Expected: [42.0]");
    }
}