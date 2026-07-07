import java.util.*;

/*
Problem Title: Minimum Cost to Bundle Songs into Albums

Problem Description:
A music platform wants to package a sequence of songs into albums for physical release.
The songs must remain in their original order, and every song must belong to exactly one album.
For each song i, you are given its duration durations[i]. You are also given an integer maxSongs,
meaning an album can contain at most maxSongs consecutive songs.

The production cost of one album is defined as:
(max duration among songs in that album) * (number of songs in that album)

Your task is to split the full song list into one or more albums so that the total production cost is minimized.

Return the minimum possible total cost.

This is an optimization problem over contiguous partitions of the array. A greedy choice does not always work,
because putting a long song into one album may increase that album's maximum duration, but it could still reduce
the total cost if it avoids creating another expensive album later.

Constraints:
- 1 <= durations.length <= 2000
- 1 <= durations[i] <= 10^6
- 1 <= maxSongs <= durations.length
- Each album must contain at least 1 song and at most maxSongs songs

Example 1:
Input: durations = [3, 1, 4, 2], maxSongs = 2
Output: 10

Example 2:
Input: durations = [5, 2, 2, 6, 3], maxSongs = 3
Output: 18
*/

public class Solution {

    /**
     * Computes the minimum total production cost to partition the songs into valid albums.
     *
     * Dynamic Programming idea:
     * Let dp[i] represent the minimum cost to package the first i songs
     * (that is, songs at indices 0 through i - 1).
     *
     * To compute dp[i], we try every possible valid album that ends at song i - 1.
     * If that last album starts at index j, then:
     * - it contains songs j..i-1
     * - its length is (i - j), which must be <= maxSongs
     * - its cost is (maximum duration in durations[j..i-1]) * (i - j)
     *
     * Therefore:
     * dp[i] = min over all valid j of:
     *         dp[j] + max(durations[j..i-1]) * (i - j)
     *
     * We compute this efficiently by iterating backward from i - 1 down to at most i - maxSongs,
     * while maintaining the running maximum duration of the current candidate album.
     *
     * @param durations the durations of songs, in fixed order
     * @param maxSongs the maximum number of consecutive songs allowed in one album
     * @return the minimum possible total production cost
     * Time complexity: O(n * maxSongs), where n = durations.length
     * Space complexity: O(n)
     */
    public long minimumCost(int[] durations, int maxSongs) {
        int n = durations.length;

        // dp[i] = minimum cost to package the first i songs.
        // dp[0] = 0 because packaging zero songs costs nothing.
        long[] dp = new long[n + 1];

        // Initialize all states to a very large value so we can take minimums safely.
        Arrays.fill(dp, Long.MAX_VALUE / 4);
        dp[0] = 0L;

        // We build the answer from smaller prefixes to larger prefixes.
        // For each i, we compute the best way to package the first i songs.
        for (int i = 1; i <= n; i++) {

            // This variable will track the maximum duration in the candidate last album.
            // As we extend the album backward one song at a time, we update this maximum.
            int currentMax = 0;

            // Try every possible starting point j for the last album ending at i - 1.
            // The album length is i - j, and it must be at most maxSongs.
            // So j ranges from i - 1 down to max(0, i - maxSongs).
            for (int j = i - 1; j >= 0 && i - j <= maxSongs; j--) {

                // Include durations[j] into the current candidate album [j..i-1].
                // Since we are moving j backward, we can update the maximum incrementally.
                currentMax = Math.max(currentMax, durations[j]);

                // Number of songs in the current candidate album.
                int albumLength = i - j;

                // Cost of making songs j..i-1 into one album.
                long albumCost = (long) currentMax * albumLength;

                // Total cost if we split before j:
                // - first j songs cost dp[j]
                // - last album costs albumCost
                long candidate = dp[j] + albumCost;

                // Keep the best (minimum) total cost.
                if (candidate < dp[i]) {
                    dp[i] = candidate;
                }
            }
        }

        // dp[n] is the minimum cost to package all n songs.
        return dp[n];
    }

    /**
     * Convenience wrapper that returns the result as an int when it is guaranteed to fit.
     * For the given constraints, the true answer can be as large as 2 * 10^9,
     * which still fits in a signed 32-bit int, but we compute using long internally
     * for safety and clarity.
     *
     * @param durations the durations of songs, in fixed order
     * @param maxSongs the maximum number of consecutive songs allowed in one album
     * @return the minimum possible total production cost as an int
     * Time complexity: O(n * maxSongs), where n = durations.length
     * Space complexity: O(n)
     */
    public int minimumCostInt(int[] durations, int maxSongs) {
        return (int) minimumCost(durations, maxSongs);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs
     * Space complexity: O(1), excluding the called algorithm's internal memory
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] durations1 = {3, 1, 4, 2};
        int maxSongs1 = 2;
        long result1 = solution.minimumCost(durations1, maxSongs1);
        System.out.println("Example 1:");
        System.out.println("durations = " + Arrays.toString(durations1) + ", maxSongs = " + maxSongs1);
        System.out.println("Minimum cost = " + result1);
        System.out.println("Expected = 10");
        System.out.println();

        int[] durations2 = {5, 2, 2, 6, 3};
        int maxSongs2 = 3;
        long result2 = solution.minimumCost(durations2, maxSongs2);
        System.out.println("Example 2:");
        System.out.println("durations = " + Arrays.toString(durations2) + ", maxSongs = " + maxSongs2);
        System.out.println("Minimum cost = " + result2);
        System.out.println("Expected = 18");
        System.out.println();

        int[] durations3 = {7};
        int maxSongs3 = 1;
        long result3 = solution.minimumCost(durations3, maxSongs3);
        System.out.println("Additional Example 3:");
        System.out.println("durations = " + Arrays.toString(durations3) + ", maxSongs = " + maxSongs3);
        System.out.println("Minimum cost = " + result3);
        System.out.println();

        int[] durations4 = {2, 2, 2, 2};
        int maxSongs4 = 4;
        long result4 = solution.minimumCost(durations4, maxSongs4);
        System.out.println("Additional Example 4:");
        System.out.println("durations = " + Arrays.toString(durations4) + ", maxSongs = " + maxSongs4);
        System.out.println("Minimum cost = " + result4);
        System.out.println();

        int[] durations5 = {9, 1, 8, 1, 7};
        int maxSongs5 = 2;
        long result5 = solution.minimumCost(durations5, maxSongs5);
        System.out.println("Additional Example 5:");
        System.out.println("durations = " + Arrays.toString(durations5) + ", maxSongs = " + maxSongs5);
        System.out.println("Minimum cost = " + result5);
    }
}