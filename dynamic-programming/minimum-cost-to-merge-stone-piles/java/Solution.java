```java
/*
 * Title: Minimum Cost to Merge Stone Piles
 *
 * Problem Description:
 * You have a row of n piles of stones, where the i-th pile has stones[i] stones.
 * In one move, you can merge any two adjacent piles into a single pile.
 * The cost of this merge is equal to the total number of stones in the two piles being merged.
 *
 * Your goal is to merge all the piles into a single pile with the minimum total cost.
 *
 * Return the minimum cost to merge all piles into one.
 *
 * Constraints:
 * - 2 <= n <= 100
 * - 1 <= stones[i] <= 1000
 *
 * Example 1:
 * Input: stones = [3, 2, 4, 1]
 * Output: 20
 *
 * Example 2:
 * Input: stones = [1, 8, 3, 2]
 * Output: 27
 *
 * Approach: Interval Dynamic Programming
 * dp[i][j] = minimum cost to merge all piles from index i to index j into one pile.
 *
 * Key insight: To merge piles[i..j] into one pile, we must at some point make the
 * "last merge" that combines two sub-piles. We try every possible split point k,
 * where we merge piles[i..k] into one pile and piles[k+1..j] into one pile,
 * then combine those two results.
 *
 * The cost of the final merge is always sum(piles[i..j]) because we're combining
 * everything from i to j into one pile at the end.
 *
 * Recurrence:
 * dp[i][j] = min over all k in [i, j-1] of (dp[i][k] + dp[k+1][j] + sum(i, j))
 * Base case: dp[i][i] = 0 (a single pile costs nothing to "merge")
 */

import java.util.Arrays;

/**
 * Solution class for the Minimum Cost to Merge Stone Piles problem.
 * Uses interval dynamic programming to find the optimal merging order.
 */
public class Solution {

    /**
     * Computes the minimum cost to merge all stone piles into one pile.
     *
     * <p>Algorithm Overview (Interval DP):
     * <ul>
     *   <li>Define dp[i][j] as the minimum cost to merge piles[i..j] into a single pile.</li>
     *   <li>For each interval [i, j], try every split point k to find the cheapest way
     *       to first reduce [i..k] to one pile and [k+1..j] to one pile, then merge them.</li>
     *   <li>The cost of merging two sub-results is always the total sum of stones in [i..j],
     *       because that's how many stones are in the resulting merged pile.</li>
     * </ul>
     *
     * @param stones array of stone pile sizes
     * @return the minimum total cost to merge all piles into one
     * @throws IllegalArgumentException if stones is null or has fewer than 2 elements
     *
     * Time Complexity:  O(n^3) — three nested loops over the interval
     * Space Complexity: O(n^2) — for the dp table and prefix sums
     */
    public int minCostMergeStones(int[] stones) {
        // -----------------------------------------------------------------------
        // Step 0: Input validation
        // -----------------------------------------------------------------------
        if (stones == null || stones.length < 2) {
            throw new IllegalArgumentException("stones must have at least 2 elements");
        }

        int n = stones.length;

        // -----------------------------------------------------------------------
        // Step 1: Build a prefix sum array so we can compute range sums in O(1).
        //
        // prefix[i] = stones[0] + stones[1] + ... + stones[i-1]
        // prefix[0] = 0  (empty prefix)
        // prefix[1] = stones[0]
        // prefix[i] = prefix[i-1] + stones[i-1]
        //
        // Sum of stones[i..j] (0-indexed, inclusive) = prefix[j+1] - prefix[i]
        // -----------------------------------------------------------------------
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + stones[i];
        }

        // -----------------------------------------------------------------------
        // Step 2: Initialize the DP table.
        //
        // dp[i][j] = minimum cost to merge piles from index i to index j (inclusive).
        //
        // We fill the table with a large value (infinity) initially, then set
        // base cases: dp[i][i] = 0 because a single pile needs no merging.
        // -----------------------------------------------------------------------
        int[][] dp = new int[n][n];
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE / 2); // use half of MAX to avoid overflow
        }

        // Base case: single piles cost 0 to "merge" (they're already one pile)
        for (int i = 0; i < n; i++) {
            dp[i][i] = 0;
        }

        // -----------------------------------------------------------------------
        // Step 3: Fill the DP table by increasing interval length.
        //
        // We iterate over all possible interval lengths (len) from 2 to n.
        // For each length, we consider every starting index i, compute the
        // ending index j = i + len - 1, and try every split point k.
        //
        // Why iterate by length?
        // Because dp[i][j] depends on dp[i][k] and dp[k+1][j], which are
        // smaller intervals. By processing shorter intervals first, we ensure
        // all sub-problems are solved before we need them.
        // -----------------------------------------------------------------------
        for (int len = 2; len <= n; len++) {           // interval length from 2 to n
            for (int i = 0; i <= n - len; i++) {       // starting index
                int j = i + len - 1;                   // ending index

                // Sum of all stones in the range [i, j]
                // This is the cost of the FINAL merge that combines the two halves.
                int rangeSum = prefix[j + 1] - prefix[i];

                // Try every possible split point k within [i, j-1]
                // k is the last index of the LEFT sub-pile
                // k+1 is the first index of the RIGHT sub-pile
                for (int k = i; k < j; k++) {
                    // Cost to merge [i..k] into one pile: dp[i][k]
                    // Cost to merge [k+1..j] into one pile: dp[k+1][j]
                    // Cost to merge those two resulting piles: rangeSum
                    //   (because the two piles together contain all stones from i to j)
                    int cost = dp[i][k] + dp[k + 1][j] + rangeSum;

                    // Keep the minimum cost found so far for interval [i, j]
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                    }
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: The answer is dp[0][n-1], the minimum cost to merge ALL piles
        // (from index 0 to index n-1) into a single pile.
        // -----------------------------------------------------------------------
        return dp[0][n - 1];
    }

    /**
     * Helper method to print the DP table for educational/debugging purposes.
     *
     * @param dp    the 2D DP table
     * @param n     the number of piles
     *
     * Time Complexity:  O(n^2)
     * Space Complexity: O(1) extra
     */
    public void printDpTable(int[][] dp, int n) {
        System.out.println("DP Table (dp[i][j] = min cost to merge piles i..j):");
        System.out.print("     ");
        for (int j = 0; j < n; j++) {
            System.out.printf("%6d", j);
        }
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("i=%d: ", i);
            for (int j = 0; j < n; j++) {
                if (j < i) {
                    System.out.printf("%6s", "-");
                } else {
                    System.out.printf("%6d", dp[i][j]);
                }
            }
            System.out.println();
        }
    }

    /**
     * Verbose version of minCostMergeStones that also prints the DP table.
     * Useful for understanding the algorithm step by step.
     *
     * @param stones array of stone pile sizes
     * @return the minimum total cost to merge all piles into one
     *
     * Time Complexity:  O(n^3)
     * Space Complexity: O(n^2)
     */
    public int minCostMergeStoneVerbose(int[] stones) {
        int n = stones.length;

        // Build prefix sums
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + stones[i];
        }

        System.out.println("Stones: " + Arrays.toString(stones));
        System.out.println("Prefix sums: " + Arrays.toString(prefix));
        System.out.println("Total sum: " + prefix[n]);
        System.out.println();

        // Initialize DP table
        int[][] dp = new int[n][n];
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE / 2);
        }
        for (int i = 0; i < n; i++) {
            dp[i][i] = 0;
        }

        // Fill DP table
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                int rangeSum = prefix[j + 1] - prefix[i];

                System.out.printf("Interval [%d, %d], rangeSum=%d:%n", i, j, rangeSum);

                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + rangeSum;
                    System.out.printf("  Split at k=%d: dp[%d][%d]=%d + dp[%d][%d]=%d + %d = %d%n",
                            k, i, k, dp[i][k], k + 1, j, dp[k + 1][j], rangeSum, cost);
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                        System.out.printf("  -> New best for dp[%d][%d] = %d%n", i, j, cost);
                    }
                }
                System.out.printf("  Final dp[%d][%d] = %d%n%n", i, j, dp[i][j]);
            }
        }

        printDpTable(dp, n);
        System.out.println();

        return dp[0][n - 1];
    }

    /**
     * Main method demonstrating the solution with sample inputs.
     * Traces through examples from the problem description to verify correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: stones = [3, 2, 4, 1], Expected Output: 20
        //
        // Trace:
        // Merge index 2 and 3: cost = 4+1 = 5, array becomes [3, 2, 5]
        // Merge index 0 and 1: cost = 3+2 = 5, array becomes [5, 5]
        // Merge index 0 and 1: cost = 5+5 = 10, array becomes [10]
        // Total = 5 + 5 + 10 = 20
        // -----------------------------------------------------------------------
        System.out.println("=".repeat(60));
        System.out.println("EXAMPLE 1");
        System.out.println("=".repeat(60));
        int[] stones1 = {3, 2, 4, 1};
        System.out.println("Input: stones = " + Arrays.toString(stones1));
        int result1 = solution.minCostMergeStoneVerbose(stones1);
        System.out.println("Result: " + result1);
        System.out.println("Expected: 20");
        System.out.println("PASS: " + (result1 == 20));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: stones = [1, 8, 3, 2], Expected Output: 27
        //
        // Trace (optimal):
        // Merge index 1 and 2: cost = 8+3 = 11, array becomes [1, 11, 2]
        // Merge index 1 and 2: cost = 11+2 = 13, array becomes [1, 13]
        // Merge index 0 and 1: cost = 1+13 = 14, array becomes [14]
        // Total = 11 + 13 + 14 = 38? Let me re-check...
        //
        // Actually from the problem:
        // Merge index 2 and 3 (cost=5): [1, 8, 5]
        // Merge index 0 and 1 (cost=9): [9, 5]
        // Merge (cost=14): [14]
        // Total = 5 + 9 + 14 = 28
        //
        // Better: merge index 1 and 2 (cost=11): [1, 11, 2]
        //         merge index 2 and 3 (cost=13): [1, 13]
        //         merge (cost=14): [14]
        //         Total = 11 + 13 + 14 = 38? No wait...
        //
        // The problem says answer is 27. Let me trace more carefully:
        // stones = [1, 8, 3, 2]
        // Option: merge (3,2)->5: [1, 8, 5], cost=5
        //         merge (1,8)->9: [9, 5], cost=9
        //         merge (9,5)->14: [14], cost=14
        //         Total = 5+9+14 = 28
        //
        // Option: merge (8,3)->11: [1, 11, 2], cost=11
        //         merge (11,2)->13: [1, 13], cost=13
        //         merge (1,13)->14: [14], cost=14
        //         Total = 11+13+14 = 38? That's wrong.
        //
        // Wait, the problem says 11 + 3 + 13 = 27. Let me re-read...
        // "merge index 1 and 2 (cost = 11), giving [1, 11, 2].
        //  Merge index 2 and 3 (cost = 13), giving [1, 13].
        //  Merge (cost = 14). Total = 11 + 3 + 13 = 27."
        // Hmm, 11 + 3 + 13 = 27, but where does 3 come from?
        // Actually I think there's a typo in the problem. Let me just verify
        // what the DP gives us.
        // -----------------------------------------------------------------------
        System.out.println("=".repeat(60));
        System.out.println("EXAMPLE 