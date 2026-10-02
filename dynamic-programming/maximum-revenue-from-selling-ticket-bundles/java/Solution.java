import java.util.*;

/*
 * Title: Maximum Revenue from Selling Ticket Bundles
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A concert venue is selling tickets for N consecutive seat sections, numbered from 0 to N - 1.
 * The venue may create promotional bundles, where each bundle must cover a contiguous range of sections.
 * If a bundle covers sections i through j, inclusive, its revenue is bundleRevenue[i][j].
 * You are allowed to choose any number of bundles, but no two chosen bundles may overlap in covered sections.
 * It is also allowed to leave some sections unbundled.
 * Your task is to compute the maximum total revenue that can be earned.
 *
 * This is not a scheduling problem with explicit times; instead, each possible contiguous section range
 * has a precomputed revenue value. Some bundles may be unattractive, and skipping them can lead to a
 * better overall answer. You need to decide which non-overlapping ranges to select so that the sum of
 * their revenues is maximized.
 *
 * Return the maximum total revenue.
 *
 * Constraints:
 * - 1 <= N <= 300
 * - bundleRevenue is an N x N matrix
 * - 0 <= bundleRevenue[i][j] <= 10^6 for all 0 <= i <= j < N
 * - bundleRevenue[i][j] = 0 for i > j, or such entries may be ignored
 * - Your solution should run in O(N^2) or O(N^2 log N) time
 *
 * Example 1:
 * Input:
 * N = 4
 * bundleRevenue = [
 *   [5, 9, 10, 10],
 *   [0, 4, 7, 8],
 *   [0, 0, 6, 9],
 *   [0, 0, 0, 3]
 * ]
 * Output:
 * 15
 * Explanation:
 * Choose bundles covering sections [0, 1] with revenue 9 and [2, 2] with revenue 6. Total = 15.
 * Taking [0, 3] gives only 10, which is worse.
 *
 * Example 2:
 * Input:
 * N = 5
 * bundleRevenue = [
 *   [2, 8, 8, 9, 9],
 *   [0, 1, 5, 7, 7],
 *   [0, 0, 4, 6, 12],
 *   [0, 0, 0, 3, 5],
 *   [0, 0, 0, 0, 4]
 * ]
 * Output:
 * 20
 * Explanation:
 * One optimal choice is [0, 1] with revenue 8 and [2, 4] with revenue 12.
 * These do not overlap because section 1 is immediately before section 2.
 * Total = 20.
 */

public class Solution {

    /**
     * Computes the maximum total revenue obtainable by selecting any number of
     * non-overlapping contiguous bundles.
     *
     * Core dynamic programming idea:
     * Let dp[i] represent the maximum revenue we can earn using only sections
     * from index i to index N - 1.
     *
     * For each starting position i, we have two categories of choices:
     * 1. Skip section i entirely, so revenue becomes dp[i + 1].
     * 2. Start a bundle at i and end it at some j >= i.
     *    If we choose bundle [i, j], then the next usable section is j + 1,
     *    so total revenue becomes bundleRevenue[i][j] + dp[j + 1].
     *
     * Therefore:
     * dp[i] = max(
     *     dp[i + 1],
     *     max over j from i to N - 1 of (bundleRevenue[i][j] + dp[j + 1])
     * )
     *
     * The answer is dp[0].
     *
     * @param bundleRevenue an N x N matrix where bundleRevenue[i][j] is the revenue
     *                      for choosing the contiguous bundle covering sections i..j
     *                      inclusive; entries with i > j are ignored
     * @return the maximum total revenue obtainable from non-overlapping bundles
     * @implNote Time complexity: O(N^2), because for each start index i we try all end indices j >= i
     * @implNote Space complexity: O(N), for the one-dimensional DP array
     */
    public long maxRevenue(int[][] bundleRevenue) {
        validateInput(bundleRevenue);

        int n = bundleRevenue.length;

        // dp[i] will store the best answer for the suffix of sections starting at i.
        // In other words:
        // - dp[0] = best answer for the whole problem
        // - dp[1] = best answer if section 0 is no longer available
        // - ...
        // - dp[n] = 0 because there are no sections left to use
        long[] dp = new long[n + 1];

        // Base case:
        // If we are already past the last section, there is nothing left to bundle.
        dp[n] = 0L;

        // We fill the DP array from right to left.
        // Why right to left?
        // Because dp[i] depends on dp[i + 1], dp[i + 2], ..., dp[n],
        // so those values must already be known when computing dp[i].
        for (int i = n - 1; i >= 0; i--) {

            // Option 1: skip section i completely.
            // This means we do not start any bundle at i.
            // Then the best we can do is whatever is optimal starting from i + 1.
            long best = dp[i + 1];

            // Option 2: start a bundle at i and try every possible ending position j.
            // For each candidate bundle [i, j]:
            // - we earn bundleRevenue[i][j] immediately
            // - then we continue optimally from section j + 1
            for (int j = i; j < n; j++) {
                long candidate = (long) bundleRevenue[i][j] + dp[j + 1];

                // Keep the best choice seen so far.
                if (candidate > best) {
                    best = candidate;
                }
            }

            // Store the optimal answer for suffix starting at i.
            dp[i] = best;
        }

        // The full problem starts at section 0.
        return dp[0];
    }

    /**
     * A convenience overload that accepts both N and the revenue matrix.
     * The value of N must match bundleRevenue.length.
     *
     * @param n the number of seat sections
     * @param bundleRevenue an N x N matrix of bundle revenues
     * @return the maximum total revenue obtainable from non-overlapping bundles
     * @implNote Time complexity: O(N^2)
     * @implNote Space complexity: O(N)
     */
    public long maxRevenue(int n, int[][] bundleRevenue) {
        if (bundleRevenue == null || bundleRevenue.length != n) {
            throw new IllegalArgumentException("N must match bundleRevenue.length.");
        }
        return maxRevenue(bundleRevenue);
    }

    /**
     * Validates the input matrix shape.
     *
     * @param bundleRevenue the revenue matrix to validate
     * @return nothing; throws an exception if the input is invalid
     * @implNote Time complexity: O(N)
     * @implNote Space complexity: O(1)
     */
    public void validateInput(int[][] bundleRevenue) {
        if (bundleRevenue == null) {
            throw new IllegalArgumentException("bundleRevenue must not be null.");
        }

        int n = bundleRevenue.length;

        if (n == 0) {
            throw new IllegalArgumentException("bundleRevenue must have at least one row.");
        }

        for (int i = 0; i < n; i++) {
            if (bundleRevenue[i] == null || bundleRevenue[i].length != n) {
                throw new IllegalArgumentException("bundleRevenue must be an N x N matrix.");
            }
        }
    }

    /**
     * Prints a matrix in a beginner-friendly format.
     *
     * @param matrix the matrix to print
     * @return nothing
     * @implNote Time complexity: O(N^2)
     * @implNote Space complexity: O(1), excluding output buffering
     */
    public void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * @implNote Time complexity: O(1) for the fixed demo inputs, excluding the algorithm calls
     * @implNote Space complexity: O(1), excluding the sample matrices
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] bundleRevenue1 = {
            {5, 9, 10, 10},
            {0, 4, 7, 8},
            {0, 0, 6, 9},
            {0, 0, 0, 3}
        };

        int[][] bundleRevenue2 = {
            {2, 8, 8, 9, 9},
            {0, 1, 5, 7, 7},
            {0, 0, 4, 6, 12},
            {0, 0, 0, 3, 5},
            {0, 0, 0, 0, 4}
        };

        System.out.println("Example 1:");
        System.out.println("Input matrix:");
        solution.printMatrix(bundleRevenue1);
        long result1 = solution.maxRevenue(4, bundleRevenue1);
        System.out.println("Computed output: " + result1);
        System.out.println("Expected output: 15");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("Input matrix:");
        solution.printMatrix(bundleRevenue2);
        long result2 = solution.maxRevenue(5, bundleRevenue2);
        System.out.println("Computed output: " + result2);
        System.out.println("Expected output: 20");
        System.out.println();

        // Additional small sanity check:
        // If there is only one section, the answer is simply the best of:
        // - skip it => 0
        // - take [0,0] => bundleRevenue[0][0]
        int[][] bundleRevenue3 = {
            {7}
        };

        System.out.println("Additional sanity check:");
        System.out.println("Input matrix:");
        solution.printMatrix(bundleRevenue3);
        long result3 = solution.maxRevenue(1, bundleRevenue3);
        System.out.println("Computed output: " + result3);
        System.out.println("Expected output: 7");
    }
}