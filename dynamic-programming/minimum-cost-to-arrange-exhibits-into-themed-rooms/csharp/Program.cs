/*
Minimum Cost to Arrange Exhibits into Themed Rooms

Problem Description:
A museum is preparing a long hallway exhibition with n exhibits placed in a fixed left-to-right order.
Each exhibit has an integer theme label given by the array themes, where themes[i] is the theme of the i-th exhibit.
The museum wants to divide the hallway into exactly k contiguous rooms, and every exhibit must belong to exactly one room.

The cost of a single room is defined as the number of unordered pairs of exhibits inside that room
that share the same theme label.

Example:
If a room contains themes [2, 3, 2, 2], then its cost is 3 because the equal-theme pairs are:
- first 2 with second 2
- first 2 with third 2
- second 2 with third 2

The total arrangement cost is the sum of the costs of all rooms.

Task:
Return the minimum possible total cost after partitioning the exhibits into exactly k contiguous rooms.

Constraints:
- 1 <= n <= 1000
- 1 <= k <= min(n, 50)
- 1 <= themes[i] <= 10^5

Examples:
1)
themes = [1, 2, 1, 2, 1], k = 2
Output: 1

2) Corrected example
themes = [4, 4, 4, 5, 5], k = 2
Output: 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing cost for every subarray: O(n^2)
    - Dynamic programming transitions: O(k * n^2)
    - Total: O(n^2 + k * n^2) = O(k * n^2)

    Space Complexity:
    - Cost table: O(n^2)
    - DP table: O(k * n)
    - Frequency dictionary used during cost precomputation: O(n) in the worst case
    - Total: O(n^2 + k * n)
    */
    public long MinCostToArrangeExhibits(int[] themes, int k)
    {
        int n = themes.Length;

        // cost[i, j] will store the room cost of the contiguous segment themes[i..j].
        // In other words, if we place exhibits i through j into one room,
        // cost[i, j] tells us how many equal-theme unordered pairs are inside that room.
        //
        // Why precompute this?
        // Because the DP will repeatedly ask:
        // "What is the cost if the last room starts at position p and ends at position i-1?"
        // If we can answer that in O(1), the DP becomes much simpler.
        long[,] cost = new long[n, n];

        // Precompute all subarray costs.
        //
        // We fix a left boundary i, then extend the right boundary j one step at a time.
        // While extending, we maintain how many times each theme has appeared in the current segment.
        //
        // Key observation:
        // When we add a new exhibit with theme x to the current room,
        // the number of NEW equal-theme pairs created is exactly the number of previous x's already in the room.
        //
        // Example:
        // Current room has [2, 3, 2], and we add another 2.
        // There are already 2 copies of theme 2 in the room,
        // so the new exhibit forms 2 new equal pairs.
        for (int i = 0; i < n; i++)
        {
            var freq = new Dictionary<int, int>();
            long currentCost = 0;

            for (int j = i; j < n; j++)
            {
                int theme = themes[j];

                // If this theme has already appeared t times in the current segment,
                // then adding one more copy creates exactly t new equal pairs.
                if (freq.TryGetValue(theme, out int count))
                {
                    currentCost += count;
                    freq[theme] = count + 1;
                }
                else
                {
                    freq[theme] = 1;
                }

                cost[i, j] = currentCost;
            }
        }

        // dp[rooms, i] = minimum cost to partition the first i exhibits
        //                 (that is, themes[0..i-1]) into exactly 'rooms' contiguous rooms.
        //
        // Important indexing note:
        // - i ranges from 0 to n
        // - i means "how many exhibits have been used"
        //
        // So:
        // - dp[0, 0] = 0  -> zero exhibits into zero rooms costs zero
        // - dp[0, i] = impossible for i > 0
        //
        // Final answer will be dp[k, n].
        long INF = long.MaxValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        // Initialize all states as impossible first.
        for (int rooms = 0; rooms <= k; rooms++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[rooms, i] = INF;
            }
        }

        dp[0, 0] = 0;

        // Build the DP room by room.
        for (int rooms = 1; rooms <= k; rooms++)
        {
            // To split first i exhibits into 'rooms' non-empty rooms,
            // we must have at least 'rooms' exhibits.
            for (int i = rooms; i <= n; i++)
            {
                // We try every possible starting point p of the last room.
                //
                // Then:
                // - first p exhibits are split into rooms - 1 rooms
                // - exhibits p..i-1 form the last room
                //
                // Since rooms are non-empty, p must be at least rooms - 1
                // and at most i - 1.
                for (int p = rooms - 1; p <= i - 1; p++)
                {
                    if (dp[rooms - 1, p] == INF)
                    {
                        // This previous state is impossible, so skip it.
                        continue;
                    }

                    // Cost of the last room is the cost of subarray themes[p..i-1].
                    long candidate = dp[rooms - 1, p] + cost[p, i - 1];

                    if (candidate < dp[rooms, i])
                    {
                        dp[rooms, i] = candidate;
                    }
                }
            }
        }

        return dp[k, n];
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] themes1 = { 1, 2, 1, 2, 1 };
int k1 = 2;
long result1 = solution.MinCostToArrangeExhibits(themes1, k1);
Console.WriteLine(result1); // Expected: 1

// Corrected Example 2
int[] themes2 = { 4, 4, 4, 5, 5 };
int k2 = 2;
long result2 = solution.MinCostToArrangeExhibits(themes2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional small demo
int[] themes3 = { 2, 3, 2, 2 };
int k3 = 1;
long result3 = solution.MinCostToArrangeExhibits(themes3, k3);
Console.WriteLine(result3); // Expected: 3