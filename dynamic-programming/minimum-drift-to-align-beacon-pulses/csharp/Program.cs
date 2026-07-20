/*
Title: Minimum Drift to Align Beacon Pulses
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A monitoring system receives pulse timestamps from two independent space beacons. Due to clock drift and packet loss, the two timestamp sequences are not perfectly aligned. You are given two integer arrays a and b, where a[i] and b[j] are pulse times in milliseconds, sorted in non-decreasing order. You want to align the sequences by partitioning each array into the same number of non-empty contiguous groups. The k-th group from a must be matched with the k-th group from b.

If a group from a spans indices l1..r1 and the matched group from b spans indices l2..r2, the drift cost of that matched pair is:

abs((sum of a[l1..r1]) - (sum of b[l2..r2]))

Your task is to return the minimum possible total drift cost over all valid ways to partition both arrays into the same number of contiguous non-empty groups.

In other words, you may decide where to cut each sequence, but both sequences must end up with exactly the same number of segments, and segments must be matched in order.

Constraints:
- 1 <= a.length, b.length <= 200
- 1 <= a[i], b[j] <= 10^6
- Both arrays are sorted in non-decreasing order
- The answer fits in a 64-bit signed integer

Example 1:
Input: a = [2, 5, 9], b = [4, 6, 7]
Output: 1

Example 2:
Input: a = [1, 3, 8, 10], b = [2, 4, 6, 12]
Output: 2
*/

using System;

public class Solution
{
    /*
        Time Complexity:
        O(n^2 * m^2), where n = a.Length and m = b.Length.

        Why:
        - dp[i, j] means the minimum total drift cost to align the first i elements of a
          with the first j elements of b.
        - To compute dp[i, j], we try every possible previous cut position:
              previous cut in a at x, where 0 <= x < i
              previous cut in b at y, where 0 <= y < j
          That creates the last matched group:
              a[x..i-1] with b[y..j-1]
        - So each state (i, j) checks O(i * j) transitions.
        - Summed over all states, this is O(n^2 * m^2).

        Space Complexity:
        O(n * m) for the DP table, plus O(n + m) for prefix sums.
    */
    public long MinimumDrift(int[] a, int[] b)
    {
        int n = a.Length;
        int m = b.Length;

        // Prefix sums let us compute any contiguous subarray sum in O(1).
        //
        // prefixA[i] = sum of the first i elements of a
        // So:
        //   sum of a[l..r] = prefixA[r + 1] - prefixA[l]
        //
        // We use long because sums can be large:
        // up to 200 * 1_000_000 = 200,000,000 per array,
        // and total answer is also required to fit in 64-bit signed integer.
        long[] prefixA = new long[n + 1];
        long[] prefixB = new long[m + 1];

        for (int i = 0; i < n; i++)
        {
            prefixA[i + 1] = prefixA[i] + a[i];
        }

        for (int j = 0; j < m; j++)
        {
            prefixB[j + 1] = prefixB[j] + b[j];
        }

        // We use a large value to represent "infinity" for impossible or not-yet-computed states.
        long INF = long.MaxValue / 4;

        // dp[i, j] = minimum total drift cost to align:
        // - first i elements of a  => a[0..i-1]
        // - first j elements of b  => b[0..j-1]
        //
        // Important interpretation:
        // We partition these prefixes into the same number of non-empty contiguous groups,
        // matched in order, and dp[i, j] stores the minimum possible total cost.
        //
        // Dimensions are (n+1) x (m+1) because prefix length can be 0.
        long[,] dp = new long[n + 1, m + 1];

        // Initialize all states to INF first.
        for (int i = 0; i <= n; i++)
        {
            for (int j = 0; j <= m; j++)
            {
                dp[i, j] = INF;
            }
        }

        // Base case:
        // Aligning 0 elements of a with 0 elements of b costs 0.
        // No groups, no cost.
        dp[0, 0] = 0;

        // We now fill the DP table.
        //
        // For each target prefix (i, j), we decide where the LAST matched group starts.
        // Suppose the last group starts:
        //   in a at index x, so the last group is a[x..i-1]
        //   in b at index y, so the last group is b[y..j-1]
        //
        // Then before that last group, we must have already optimally aligned:
        //   a[0..x-1] with b[0..y-1]
        // whose cost is dp[x, y]
        //
        // So transition is:
        //   dp[i, j] = min over all x < i and y < j of
        //              dp[x, y] + abs(sum(a[x..i-1]) - sum(b[y..j-1]))
        //
        // This is the core recurrence.
        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= m; j++)
            {
                long best = INF;

                // Try every possible starting point x of the last group in a.
                for (int x = 0; x < i; x++)
                {
                    // Sum of the candidate last group in a: a[x..i-1]
                    long sumA = prefixA[i] - prefixA[x];

                    // Try every possible starting point y of the last group in b.
                    for (int y = 0; y < j; y++)
                    {
                        // If the previous prefixes cannot be aligned, skip.
                        if (dp[x, y] == INF)
                        {
                            continue;
                        }

                        // Sum of the candidate last group in b: b[y..j-1]
                        long sumB = prefixB[j] - prefixB[y];

                        // Cost of matching these two last groups.
                        long groupCost = Math.Abs(sumA - sumB);

                        // Total cost = best cost for previous prefixes + cost of last group.
                        long candidate = dp[x, y] + groupCost;

                        if (candidate < best)
                        {
                            best = candidate;
                        }
                    }
                }

                dp[i, j] = best;
            }
        }

        // Final answer:
        // minimum cost to align all elements of a with all elements of b.
        return dp[n, m];
    }
}

// Demo code
var solution = new Solution();

int[] a1 = { 2, 5, 9 };
int[] b1 = { 4, 6, 7 };
long result1 = solution.MinimumDrift(a1, b1);
Console.WriteLine(result1); // Expected: 1

int[] a2 = { 1, 3, 8, 10 };
int[] b2 = { 2, 4, 6, 12 };
long result2 = solution.MinimumDrift(a2, b2);
Console.WriteLine(result2); // Expected: 2