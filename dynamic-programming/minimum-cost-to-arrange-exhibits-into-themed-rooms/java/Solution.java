import java.util.*;

/*
Problem Title: Minimum Cost to Arrange Exhibits into Themed Rooms

Problem Description:
A museum is preparing a long hallway exhibition with n exhibits placed in a fixed left-to-right order.
Each exhibit has an integer theme label given by the array themes, where themes[i] is the theme of the
i-th exhibit. The museum wants to divide the hallway into exactly k contiguous rooms, and every exhibit
must belong to exactly one room.

The cost of a single room is defined as the number of unordered pairs of exhibits inside that room that
share the same theme label. For example, if a room contains themes [2, 3, 2, 2], then its cost is 3
because the three equal-theme pairs are formed by the three exhibits labeled 2. The total arrangement
cost is the sum of the costs of all rooms.

Your task is to return the minimum possible total cost after partitioning the exhibits into exactly k
contiguous rooms.

This is a partition dynamic programming problem. The order of exhibits cannot be changed, and empty rooms
are not allowed.

Constraints:
- 1 <= n <= 1000
- 1 <= k <= min(n, 50)
- 1 <= themes[i] <= 10^5

Example 1:
Input: themes = [1, 2, 1, 2, 1], k = 2
Output: 1
Explanation: Split as [1, 2, 1] and [2, 1]. The first room has one equal-theme pair (the two 1s),
and the second room has cost 0. Total cost = 1, which is minimum.

Example 2:
Input: themes = [4, 4, 4, 5, 5], k = 2
Output: 4
Explanation: The best split is [4, 4, 4] and [5, 5]. Their costs are 3 and 1, so the total is 4.
Any other split produces a cost of at least 4.
*/

public class Solution {

    /**
     * Computes the minimum total cost to partition the exhibits into exactly k contiguous rooms.
     *
     * Core idea:
     * 1. Precompute cost[i][j] = cost of putting subarray themes[i..j] into one room.
     * 2. Use dynamic programming:
     *    dp[p][i] = minimum cost to partition the first i exhibits into exactly p rooms.
     * 3. Transition:
     *    dp[p][i] = min over t from p-1 to i-1 of dp[p-1][t] + cost[t][i-1]
     *    where:
     *    - first t exhibits form p-1 rooms
     *    - exhibits t..i-1 form the last room
     *
     * @param themes the theme labels of exhibits in fixed order
     * @param k the exact number of contiguous rooms
     * @return the minimum possible total cost
     * Time complexity: O(n^2 + k * n^2) = O(k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public int minimumCost(int[] themes, int k) {
        int n = themes.length;

        // Step 1:
        // Precompute the cost of every possible contiguous segment.
        // cost[i][j] means:
        // "If exhibits from index i to index j are placed into one room,
        //  how many equal-theme unordered pairs are inside that room?"
        //
        // Example:
        // segment [1, 2, 1, 2, 1]
        // frequencies:
        // 1 -> 3 occurrences => C(3,2) = 3 pairs
        // 2 -> 2 occurrences => C(2,2) = 1 pair
        // total = 4
        //
        // We compute this efficiently by fixing the left boundary i,
        // then extending the right boundary j one step at a time.
        // When we add themes[j]:
        // - if this value has already appeared freq times in the current segment,
        //   then adding one more creates exactly freq new equal pairs.
        long[][] cost = buildCostTable(themes);

        // We use a large number as "infinity" for impossible states.
        long INF = Long.MAX_VALUE / 4;

        // dp[p][i] = minimum cost to partition first i exhibits (0..i-1) into exactly p rooms.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states as impossible.
        for (int p = 0; p <= k; p++) {
            Arrays.fill(dp[p], INF);
        }

        // Base case:
        // 0 exhibits into 0 rooms costs 0.
        dp[0][0] = 0;

        // Fill DP room count by room count.
        for (int rooms = 1; rooms <= k; rooms++) {
            // To split first i exhibits into exactly 'rooms' non-empty rooms,
            // we must have at least 'rooms' exhibits.
            for (int i = rooms; i <= n; i++) {

                // Try every possible starting point of the last room.
                // Let t be the number of exhibits used by the first rooms-1 rooms.
                // Then:
                // - first part: exhibits [0 .. t-1] => rooms-1 rooms
                // - last room: exhibits [t .. i-1]
                //
                // Since rooms are non-empty:
                // t must be at least rooms-1
                // t must be at most i-1
                for (int t = rooms - 1; t <= i - 1; t++) {
                    if (dp[rooms - 1][t] == INF) {
                        continue;
                    }

                    long candidate = dp[rooms - 1][t] + cost[t][i - 1];
                    if (candidate < dp[rooms][i]) {
                        dp[rooms][i] = candidate;
                    }
                }
            }
        }

        return (int) dp[k][n];
    }

    /**
     * Builds a table where cost[i][j] is the number of unordered equal-theme pairs
     * inside the contiguous subarray themes[i..j].
     *
     * Detailed construction:
     * - Fix left boundary i.
     * - Maintain a frequency map while expanding right boundary j.
     * - Suppose themes[j] has already appeared freq times in themes[i..j-1].
     * - After adding themes[j], it forms exactly freq new equal pairs with those previous occurrences.
     * - Therefore:
     *   runningCost += freq
     *
     * This works because each new equal pair is counted exactly once: when the later element is added.
     *
     * @param themes the theme labels of exhibits
     * @return a 2D table of segment costs
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long[][] buildCostTable(int[] themes) {
        int n = themes.length;
        long[][] cost = new long[n][n];

        for (int left = 0; left < n; left++) {
            // Frequency map for the current segment starting at 'left'.
            Map<Integer, Integer> freq = new HashMap<>();

            long currentCost = 0;

            for (int right = left; right < n; right++) {
                int value = themes[right];

                // How many times has this theme already appeared in the current segment?
                int previousCount = freq.getOrDefault(value, 0);

                // Adding this exhibit creates exactly 'previousCount' new equal pairs.
                currentCost += previousCount;

                // Update frequency after using the old count.
                freq.put(value, previousCount + 1);

                // Store the cost for segment [left..right].
                cost[left][right] = currentCost;
            }
        }

        return cost;
    }

    /**
     * A small helper method to print one demonstration case.
     *
     * @param themes the theme labels
     * @param k the number of rooms
     * @return the computed minimum cost
     * Time complexity: same as minimumCost, O(k * n^2)
     * Space complexity: same as minimumCost, O(n^2 + k * n)
     */
    public int demonstrate(int[] themes, int k) {
        int answer = minimumCost(themes, k);
        System.out.println("themes = " + Arrays.toString(themes) + ", k = " + k);
        System.out.println("Minimum cost = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * Verified examples:
     * 1) [1, 2, 1, 2, 1], k = 2 => 1
     * 2) [4, 4, 4, 5, 5], k = 2 => 4
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: depends on the sample sizes used
     * Space complexity: depends on the sample sizes used
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] themes1 = {1, 2, 1, 2, 1};
        int k1 = 2;
        int result1 = solution.demonstrate(themes1, k1);
        System.out.println("Expected: 1, Actual: " + result1);
        System.out.println();

        int[] themes2 = {4, 4, 4, 5, 5};
        int k2 = 2;
        int result2 = solution.demonstrate(themes2, k2);
        System.out.println("Expected: 4, Actual: " + result2);
        System.out.println();

        // Additional quick sanity checks.
        int[] themes3 = {2, 3, 2, 2};
        int k3 = 1;
        int result3 = solution.demonstrate(themes3, k3);
        System.out.println("Expected: 3, Actual: " + result3);
        System.out.println();

        int[] themes4 = {1, 1, 1, 1};
        int k4 = 4;
        int result4 = solution.demonstrate(themes4, k4);
        System.out.println("Expected: 0, Actual: " + result4);
    }
}