/*
Title: Minimum Cost to Bundle Songs into Albums

Problem Description:
A music platform wants to package a sequence of songs into albums for physical release.
The songs must remain in their original order, and every song must belong to exactly one album.

For each song i, you are given its duration durations[i].
You are also given an integer maxSongs, meaning an album can contain at most maxSongs consecutive songs.

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
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * maxSongs)
    - There are n positions in the DP.
    - For each ending position, we look backward at most maxSongs songs to form the last album.

    Space Complexity:
    O(n)
    - We store one DP array where dp[i] means the minimum cost to package the first i songs.
    */
    public long MinCostToBundleSongs(int[] durations, int maxSongs)
    {
        int n = durations.Length;

        // dp[i] will store the minimum total cost needed to package the first i songs.
        //
        // Important meaning:
        // - dp[0] = 0 means: packaging zero songs costs zero.
        // - dp[1] means: minimum cost for durations[0..0]
        // - dp[2] means: minimum cost for durations[0..1]
        // - ...
        // - dp[n] means: minimum cost for the entire array
        //
        // We use long instead of int because:
        // - durations[i] can be as large as 1,000,000
        // - album size can be up to 2000
        // - total cost across many albums can exceed int range
        long[] dp = new long[n + 1];

        // Initialize all states except dp[0] to a very large value.
        // This represents "not computed yet" / "currently unreachable".
        //
        // We do not use long.MaxValue directly because later we add costs to it,
        // and adding to long.MaxValue could overflow.
        long INF = long.MaxValue / 4;
        for (int i = 1; i <= n; i++)
        {
            dp[i] = INF;
        }

        // Base case:
        // Packaging zero songs costs zero.
        dp[0] = 0;

        // We now build the answer from left to right.
        //
        // For each i from 1 to n:
        // We want to compute dp[i], the minimum cost to package the first i songs.
        //
        // The key DP idea:
        // Consider the LAST album in an optimal solution for the first i songs.
        // That last album must contain some number of songs len, where 1 <= len <= maxSongs,
        // and of course len cannot exceed i.
        //
        // If the last album has length len, then:
        // - It covers songs from index (i - len) to (i - 1)
        // - The songs before it are the first (i - len) songs
        // - Their optimal cost is dp[i - len]
        // - The last album cost is (maximum duration in that segment) * len
        //
        // So:
        // dp[i] = min over all valid len:
        //         dp[i - len] + max(durations[i-len .. i-1]) * len
        for (int i = 1; i <= n; i++)
        {
            // As we try different possible lengths for the last album,
            // we need the maximum duration inside that album.
            //
            // Instead of recomputing the max from scratch each time,
            // we expand the segment backward one song at a time and update the max incrementally.
            //
            // This is much more efficient and keeps the total complexity at O(n * maxSongs).
            int currentMaxDuration = 0;

            // Try every possible album length ending at song i - 1.
            //
            // len = 1 means the last album contains only durations[i - 1]
            // len = 2 means the last album contains durations[i - 2], durations[i - 1]
            // ...
            // len can go up to maxSongs, but also cannot exceed i
            for (int len = 1; len <= maxSongs && len <= i; len++)
            {
                // The new song added to the front of the current candidate last album
                // is at index i - len.
                int songIndex = i - len;

                // Update the maximum duration of the candidate last album.
                //
                // Why this works:
                // - When len = 1, the album is just one song, so max is that song.
                // - When len increases by 1, we add one more song to the front.
                //   The new max is simply max(previousMax, newly added song).
                currentMaxDuration = Math.Max(currentMaxDuration, durations[songIndex]);

                // Compute the cost of making songs [songIndex .. i-1] the last album.
                long lastAlbumCost = (long)currentMaxDuration * len;

                // Total cost if we choose this split:
                // - dp[i - len] = best cost for all songs before the last album
                // - lastAlbumCost = cost of the last album itself
                long candidateTotalCost = dp[i - len] + lastAlbumCost;

                // Keep the best (minimum) among all possible last album lengths.
                if (candidateTotalCost < dp[i])
                {
                    dp[i] = candidateTotalCost;
                }
            }
        }

        // The answer for the full array is the minimum cost to package the first n songs.
        return dp[n];
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] durations1 = { 3, 1, 4, 2 };
int maxSongs1 = 2;
long result1 = solution.MinCostToBundleSongs(durations1, maxSongs1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] durations2 = { 5, 2, 2, 6, 3 };
int maxSongs2 = 3;
long result2 = solution.MinCostToBundleSongs(durations2, maxSongs2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity checks
int[] durations3 = { 7 };
int maxSongs3 = 1;
long result3 = solution.MinCostToBundleSongs(durations3, maxSongs3);
Console.WriteLine($"Single song Result: {result3}");

int[] durations4 = { 2, 2, 2, 2 };
int maxSongs4 = 4;
long result4 = solution.MinCostToBundleSongs(durations4, maxSongs4);
Console.WriteLine($"All equal durations Result: {result4}");