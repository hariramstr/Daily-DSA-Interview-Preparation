import java.util.*;

/*
 * Title: Minimum Fatigue to Merge Spell Scrolls
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A wizard archive stores a row of spell scrolls, where the i-th scroll has an energy value energy[i].
 * You must merge all scrolls into exactly one final scroll. In one operation, you may choose any
 * contiguous block of exactly k scrolls and merge them into a single new scroll. The fatigue cost
 * of that operation is the sum of the energy values of those k scrolls. The new scroll's energy
 * becomes that same sum, and it remains in the row in place of the merged block.
 *
 * Your task is to return the minimum total fatigue needed to end with one scroll. If it is impossible
 * to reduce the row to one scroll using only merges of exactly k consecutive scrolls, return -1.
 *
 * Because every merge changes future costs, a greedy strategy is not always optimal. You need to
 * determine the globally minimum fatigue over all valid merge orders.
 *
 * Constraints:
 * - 1 <= energy.length <= 30
 * - 1 <= energy[i] <= 10^4
 * - 2 <= k <= 30
 *
 * Notes:
 * - Only contiguous blocks may be merged.
 * - Every merge must combine exactly k current scrolls, not fewer and not more.
 * - The answer fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: energy = [3, 2, 4, 1], k = 2
 * Output: 20
 *
 * Example 2:
 * Input: energy = [3, 2, 4, 1], k = 3
 * Output: -1
 *
 * Example 3:
 * Input: energy = [3, 5, 1, 2, 6], k = 3
 * Output: 25
 */

public class Solution {

    /**
     * Computes the minimum total fatigue needed to merge all scrolls into exactly one scroll.
     *
     * Core idea:
     * We use interval dynamic programming.
     *
     * Let dp[i][j] represent the minimum cost to merge the subarray energy[i..j] into the
     * minimum possible number of piles achievable under the rules.
     *
     * Important observation:
     * Every merge of exactly k piles reduces the pile count by (k - 1).
     * Therefore, starting from n piles, it is possible to end with exactly 1 pile if and only if:
     * (n - 1) % (k - 1) == 0
     *
     * For an interval [i..j], after optimally merging its internal parts as much as possible,
     * we can merge it into one pile only when:
     * (length - 1) % (k - 1) == 0
     * In that case, after combining its subproblems, we add the sum of the interval because
     * one final merge of k piles into one pile is performed.
     *
     * Transition:
     * dp[i][j] = min(dp[i][mid] + dp[mid + 1][j]) for mid stepping by (k - 1)
     *
     * Why step by (k - 1)?
     * Because only those split points preserve valid pile counts for future merges.
     * This is a standard optimization for this problem and keeps the DP both correct and efficient.
     *
     * @param energy the energy values of the scrolls in their initial order
     * @param k the exact number of consecutive scrolls that must be merged in one operation
     * @return the minimum total fatigue to end with one scroll, or -1 if impossible
     * Time complexity: O(n^3 / (k - 1)) in practice, bounded by O(n^3), where n = energy.length
     * Space complexity: O(n^2)
     */
    public int minFatigue(int[] energy, int k) {
        int n = energy.length;

        // If there is only one scroll already, no merge is needed.
        if (n == 1) {
            return 0;
        }

        // Feasibility check:
        // Each merge reduces the number of piles by exactly (k - 1).
        // To go from n piles to 1 pile, (n - 1) must be divisible by (k - 1).
        if ((n - 1) % (k - 1) != 0) {
            return -1;
        }

        // Prefix sums allow O(1) interval sum queries.
        // prefix[i] = sum of energy[0..i-1]
        int[] prefix = buildPrefixSums(energy);

        // dp[i][j] = minimum cost to merge interval [i..j] into the minimum possible number of piles.
        int[][] dp = new int[n][n];

        // We fill intervals by increasing length.
        // Length 1 intervals need no cost because a single pile is already "merged".
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len <= n; i++) {
                int j = i + len - 1;

                // Initialize with a large number.
                dp[i][j] = Integer.MAX_VALUE;

                // Try splitting the interval into two parts.
                //
                // Very important:
                // We only need to split at positions that differ by (k - 1).
                // This ensures the left part can be reduced to a valid number of piles
                // that can eventually participate in future merges correctly.
                for (int mid = i; mid < j; mid += (k - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i][mid] + dp[mid + 1][j]);
                }

                // After combining subintervals optimally, if this interval's length allows
                // reduction to exactly one pile, then we must pay one additional merge cost:
                // the sum of all energies in [i..j].
                //
                // Condition explanation:
                // An interval of length len can become 1 pile iff (len - 1) % (k - 1) == 0.
                if ((len - 1) % (k - 1) == 0) {
                    dp[i][j] += rangeSum(prefix, i, j);
                }
            }
        }

        return dp[0][n - 1];
    }

    /**
     * Builds prefix sums for fast interval sum queries.
     *
     * @param energy the input energy array
     * @return prefix sum array where prefix[i] stores the sum of the first i elements
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildPrefixSums(int[] energy) {
        int n = energy.length;
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + energy[i];
        }
        return prefix;
    }

    /**
     * Returns the sum of energy[left..right] inclusive using prefix sums.
     *
     * @param prefix the prefix sum array
     * @param left the left index of the interval, inclusive
     * @param right the right index of the interval, inclusive
     * @return the sum of the interval [left..right]
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int rangeSum(int[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    /**
     * Helper method to print an example run in a beginner-friendly way.
     *
     * @param energy the input energy array
     * @param k the merge size
     * @param solver the Solution instance used to compute the answer
     * @return nothing; this method prints directly to standard output
     * Time complexity: O(n^3) dominated by the solver call
     * Space complexity: O(n^2) dominated by the solver call
     */
    public static void runExample(int[] energy, int k, Solution solver) {
        System.out.println("energy = " + Arrays.toString(energy) + ", k = " + k);
        System.out.println("Minimum fatigue = " + solver.minFatigue(energy, k));
        System.out.println();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 20
     * Example 2 -> -1
     * Example 3 -> 25
     *
     * @param args command-line arguments (not used)
     * @return nothing; this method prints directly to standard output
     * Time complexity: O(total of example solver runs)
     * Space complexity: O(n^2) per solver run
     */
    public static void main(String[] args) {
        Solution solver = new Solution();

        // Example 1
        runExample(new int[]{3, 2, 4, 1}, 2, solver);

        // Example 2
        runExample(new int[]{3, 2, 4, 1}, 3, solver);

        // Example 3
        runExample(new int[]{3, 5, 1, 2, 6}, 3, solver);
    }
}