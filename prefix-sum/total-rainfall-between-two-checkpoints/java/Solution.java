/*
 * Title: Total Rainfall Between Two Checkpoints
 * Difficulty: Easy
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A weather station has recorded daily rainfall measurements along a straight highway.
 * There are n checkpoints numbered from 0 to n-1, and rainfall[i] represents the amount
 * of rainfall (in millimeters) recorded at checkpoint i on a given day.
 *
 * You are given an integer array rainfall of length n and a 2D array queries where
 * queries[j] = [left, right]. For each query, you need to find the total rainfall recorded
 * between checkpoint left and checkpoint right, inclusive.
 *
 * Return an integer array answer where answer[j] is the total rainfall for the j-th query.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 0 <= rainfall[i] <= 1000
 * - 1 <= queries.length <= 10^5
 * - 0 <= left <= right < n
 *
 * Example 1:
 * Input: rainfall = [3, 1, 4, 1, 5, 9, 2, 6], queries = [[1, 4], [0, 6], [3, 3]]
 * Output: [11, 25, 1]
 *
 * Example 2:
 * Input: rainfall = [0, 2, 7, 3, 5], queries = [[0, 4], [2, 3]]
 * Output: [17, 10]
 */

import java.util.Arrays;

/**
 * Solution class for the "Total Rainfall Between Two Checkpoints" problem.
 *
 * <p>The key insight is to use a PREFIX SUM array to answer range sum queries efficiently.
 * Instead of summing elements from scratch for every query (O(n) per query),
 * we precompute cumulative sums so each query can be answered in O(1) time.
 *
 * <p>Prefix Sum Concept:
 * prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
 * (prefix[0] = 0, meaning "sum of zero elements before index 0")
 *
 * <p>Range sum formula:
 * sum(left, right) = prefix[right + 1] - prefix[left]
 */
public class Solution {

    /**
     * Computes the total rainfall for each query using the prefix sum technique.
     *
     * <p>Algorithm Overview:
     * 1. Build a prefix sum array of size (n + 1) where prefix[i] = sum of rainfall[0..i-1].
     * 2. For each query [left, right], answer = prefix[right + 1] - prefix[left].
     *
     * @param rainfall an integer array where rainfall[i] is the rainfall at checkpoint i
     * @param queries  a 2D array where queries[j] = [left, right] defines a range query
     * @return an integer array where answer[j] is the total rainfall from left to right inclusive
     *
     * Time Complexity:  O(n + q) — O(n) to build the prefix array, O(1) per query, O(q) total for q queries
     * Space Complexity: O(n) — for the prefix sum array of size n+1
     */
    public int[] totalRainfall(int[] rainfall, int[][] queries) {

        // -----------------------------------------------------------------------
        // STEP 1: Determine the length of the rainfall array.
        // -----------------------------------------------------------------------
        int n = rainfall.length;

        // -----------------------------------------------------------------------
        // STEP 2: Build the prefix sum array.
        //
        // We create an array of size (n + 1) to avoid boundary checks.
        // prefix[0] = 0  (no elements before index 0)
        // prefix[1] = rainfall[0]
        // prefix[2] = rainfall[0] + rainfall[1]
        // ...
        // prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
        //
        // This "1-indexed" prefix array makes the range sum formula clean:
        //   sum(left, right) = prefix[right + 1] - prefix[left]
        // -----------------------------------------------------------------------
        int[] prefix = new int[n + 1];

        // prefix[0] is already 0 by default (Java initializes int arrays to 0).
        for (int i = 1; i <= n; i++) {
            // Each prefix entry is the previous prefix plus the current rainfall value.
            // Example: rainfall = [3, 1, 4, 1, 5, 9, 2, 6]
            //   prefix[1] = prefix[0] + rainfall[0] = 0 + 3 = 3
            //   prefix[2] = prefix[1] + rainfall[1] = 3 + 1 = 4
            //   prefix[3] = prefix[2] + rainfall[2] = 4 + 4 = 8
            //   prefix[4] = prefix[3] + rainfall[3] = 8 + 1 = 9
            //   prefix[5] = prefix[4] + rainfall[4] = 9 + 5 = 14
            //   prefix[6] = prefix[5] + rainfall[5] = 14 + 9 = 23
            //   prefix[7] = prefix[6] + rainfall[6] = 23 + 2 = 25
            //   prefix[8] = prefix[7] + rainfall[7] = 25 + 6 = 31
            prefix[i] = prefix[i - 1] + rainfall[i - 1];
        }

        // -----------------------------------------------------------------------
        // STEP 3: Prepare the answer array with the same length as queries.
        // -----------------------------------------------------------------------
        int[] answer = new int[queries.length];

        // -----------------------------------------------------------------------
        // STEP 4: Answer each query in O(1) using the prefix sum array.
        //
        // For a query [left, right]:
        //   Total = prefix[right + 1] - prefix[left]
        //
        // Why does this work?
        //   prefix[right + 1] = sum of rainfall[0..right]
        //   prefix[left]      = sum of rainfall[0..left-1]
        //   Subtracting removes the part before 'left', leaving rainfall[left..right].
        //
        // Example verification for rainfall = [3, 1, 4, 1, 5, 9, 2, 6]:
        //   Query [1, 4]: prefix[5] - prefix[1] = 14 - 3 = 11  ✓ (1+4+1+5=11)
        //   Query [0, 6]: prefix[7] - prefix[0] = 25 - 0 = 25  ✓ (3+1+4+1+5+9+2=25)
        //   Query [3, 3]: prefix[4] - prefix[3] = 9  - 8 = 1   ✓ (1=1)
        // -----------------------------------------------------------------------
        for (int j = 0; j < queries.length; j++) {
            int left  = queries[j][0];  // starting checkpoint (inclusive)
            int right = queries[j][1];  // ending checkpoint (inclusive)

            // Apply the prefix sum range formula.
            // prefix[right + 1] gives the cumulative sum up to and including 'right'.
            // prefix[left] gives the cumulative sum up to but NOT including 'left'.
            // Their difference is exactly the sum from 'left' to 'right'.
            answer[j] = prefix[right + 1] - prefix[left];
        }

        // -----------------------------------------------------------------------
        // STEP 5: Return the completed answer array.
        // -----------------------------------------------------------------------
        return answer;
    }

    /**
     * Main method to demonstrate the solution with the provided examples.
     *
     * <p>Traces through both examples from the problem description and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the non-static method.
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // rainfall = [3, 1, 4, 1, 5, 9, 2, 6]
        // queries  = [[1, 4], [0, 6], [3, 3]]
        // Expected output: [11, 25, 1]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] rainfall1 = {3, 1, 4, 1, 5, 9, 2, 6};
        int[][] queries1 = {{1, 4}, {0, 6}, {3, 3}};

        System.out.println("Rainfall array : " + Arrays.toString(rainfall1));
        System.out.println("Queries        : " + Arrays.deepToString(queries1));

        int[] result1 = solution.totalRainfall(rainfall1, queries1);
        System.out.println("Result         : " + Arrays.toString(result1));
        System.out.println("Expected       : [11, 25, 1]");

        // Manual verification:
        // Query [1,4]: rainfall[1]+rainfall[2]+rainfall[3]+rainfall[4] = 1+4+1+5 = 11 ✓
        // Query [0,6]: rainfall[0]+...+rainfall[6] = 3+1+4+1+5+9+2 = 25 ✓
        // Query [3,3]: rainfall[3] = 1 ✓
        System.out.println("Match: " + Arrays.equals(result1, new int[]{11, 25, 1}));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // rainfall = [0, 2, 7, 3, 5]
        // queries  = [[0, 4], [2, 3]]
        // Expected output: [17, 10]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] rainfall2 = {0, 2, 7, 3, 5};
        int[][] queries2 = {{0, 4}, {2, 3}};

        System.out.println("Rainfall array : " + Arrays.toString(rainfall2));
        System.out.println("Queries        : " + Arrays.deepToString(queries2));

        int[] result2 = solution.totalRainfall(rainfall2, queries2);
        System.out.println("Result         : " + Arrays.toString(result2));
        System.out.println("Expected       : [17, 10]");

        // Manual verification:
        // Query [0,4]: 0+2+7+3+5 = 17 ✓
        // Query [2,3]: 7+3 = 10 ✓
        System.out.println("Match: " + Arrays.equals(result2, new int[]{17, 10}));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional edge case: single element array, single query
        // -----------------------------------------------------------------------
        System.out.println("=== Edge Case: Single Element ===");
        int[] rainfall3 = {42};
        int[][] queries3 = {{0, 0}};

        System.out.println("Rainfall array : " + Arrays.toString(rainfall3));
        System.out.println("Queries        : " + Arrays.deepToString(queries3));

        int[] result3 = solution.totalRainfall(rainfall3, queries3);
        System.out.println("Result         : " + Arrays.toString(result3));
        System.out.println("Expected       : [42]");
        System.out.println("Match: " + Arrays.equals(result3, new int[]{42}));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional edge case: all zeros
        // -----------------------------------------------------------------------
        System.out.println("=== Edge Case: All Zeros ===");
        int[] rainfall4 = {0, 0, 0, 0, 0};
        int[][] queries4 = {{0, 4}, {1, 3}};

        System.out.println("Rainfall array : " + Arrays.toString(rainfall4));
        System.out.println("Queries        : " + Arrays.deepToString(queries4));

        int[] result4 = solution.totalRainfall(rainfall4, queries4);
        System.out.println("Result         : " + Arrays.toString(result4));
        System.out.println("Expected       : [0, 0]");
        System.out.println("Match: " + Arrays.equals(result4, new int[]{0, 0}));
    }
}