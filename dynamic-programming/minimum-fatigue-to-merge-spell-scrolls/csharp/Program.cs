/*
Title: Minimum Fatigue to Merge Spell Scrolls
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A wizard archive stores a row of spell scrolls, where the i-th scroll has an energy value energy[i].
You must merge all scrolls into exactly one final scroll.

In one operation, you may choose any contiguous block of exactly k scrolls and merge them into a
single new scroll. The fatigue cost of that operation is the sum of the energy values of those
k scrolls. The new scroll's energy becomes that same sum, and it remains in the row in place of
the merged block.

Your task is to return the minimum total fatigue needed to end with one scroll. If it is impossible
to reduce the row to one scroll using only merges of exactly k consecutive scrolls, return -1.

Because every merge changes future costs, a greedy strategy is not always optimal. You need to
determine the globally minimum fatigue over all valid merge orders.

Constraints:
- 1 <= energy.length <= 30
- 1 <= energy[i] <= 10^4
- 2 <= k <= 30

Notes:
- Only contiguous blocks may be merged.
- Every merge must combine exactly k current scrolls, not fewer and not more.
- The answer fits in a 32-bit signed integer.

Example 1:
Input: energy = [3, 2, 4, 1], k = 2
Output: 20

Example 2:
Input: energy = [3, 2, 4, 1], k = 3
Output: -1

Example 3:
Input: energy = [3, 5, 1, 2, 6], k = 3
Output: 25
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n^3 / (k - 1)) in practice, commonly described as O(n^3),
    where n is the number of scrolls.

    Why:
    - We consider every interval [i..j].
    - For each interval, we try partition points m.
    - We skip partition points in steps of (k - 1), which reduces work,
      but the standard upper bound is still O(n^3).

    Space Complexity:
    O(n^2)

    Why:
    - We store a 2D DP table dp[i, j], which represents the minimum cost
      to merge the subarray energy[i..j] into the minimum possible number of piles.
    - We also store a prefix sum array of size O(n).
    */
    public int MinFatigue(int[] energy, int k)
    {
        int n = energy.Length;

        // ------------------------------------------------------------
        // Step 1: Check whether it is even possible to end with 1 pile.
        //
        // Every merge takes exactly k piles and turns them into 1 pile.
        // So each merge reduces the total number of piles by (k - 1).
        //
        // Starting from n piles, to reach exactly 1 pile, we need:
        // (n - 1) to be divisible by (k - 1)
        //
        // If not divisible, no sequence of valid merges can ever end at 1.
        // ------------------------------------------------------------
        if ((n - 1) % (k - 1) != 0)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // Step 2: Build prefix sums.
        //
        // Why do we need this?
        // Whenever an interval can finally be merged into 1 pile,
        // we must add the sum of that interval as the merge cost.
        //
        // Prefix sums let us compute any subarray sum in O(1):
        // sum(i..j) = prefix[j + 1] - prefix[i]
        //
        // This is important because the DP will ask for many interval sums.
        // ------------------------------------------------------------
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + energy[i];
        }

        // ------------------------------------------------------------
        // Step 3: Create the DP table.
        //
        // dp[i, j] = minimum cost to merge energy[i..j]
        //            into the minimum number of piles that is achievable.
        //
        // Important idea:
        // We do NOT explicitly store the number of piles in this 2D version.
        // Instead, we rely on the structure of the problem:
        //
        // For an interval length len, after valid internal merges,
        // the minimum achievable pile count is determined by:
        // piles = (len - 1) % (k - 1) + 1
        //
        // If that pile count becomes 1, then this interval can be fully merged,
        // and we add the interval sum once at the end.
        //
        // Initialize all entries to a very large number, because we are
        // looking for minimum costs.
        // ------------------------------------------------------------
        int[,] dp = new int[n, n];
        const int INF = int.MaxValue / 4;

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                dp[i, j] = 0;
            }
        }

        // ------------------------------------------------------------
        // Step 4: Fill DP by increasing interval length.
        //
        // Why increasing length?
        // Because dp[i, j] depends on smaller intervals such as dp[i, m]
        // and dp[m + 1, j]. Those smaller answers must already be known.
        // ------------------------------------------------------------
        for (int len = 2; len <= n; len++)
        {
            for (int i = 0; i + len - 1 < n; i++)
            {
                int j = i + len - 1;

                // Start with "infinity" because we want the minimum.
                dp[i, j] = INF;

                // ----------------------------------------------------
                // Step 4a: Try splitting interval [i..j] into:
                // [i..m] and [m+1..j]
                //
                // Very important optimization:
                // We only try m in steps of (k - 1).
                //
                // Why is that valid?
                // Because only certain pile counts are achievable after
                // repeated merges, and this stepping aligns with valid states.
                //
                // This is a standard optimization for this problem and
                // preserves correctness.
                // ----------------------------------------------------
                for (int m = i; m < j; m += (k - 1))
                {
                    dp[i, j] = Math.Min(dp[i, j], dp[i, m] + dp[m + 1, j]);
                }

                // ----------------------------------------------------
                // Step 4b: Decide whether [i..j] can now be merged into 1 pile.
                //
                // After combining subproblems, the interval may be reduced
                // to exactly k piles, and then one final merge can turn those
                // k piles into 1 pile.
                //
                // This is possible exactly when:
                // (len - 1) % (k - 1) == 0
                //
                // If true, we add the sum of the whole interval once.
                //
                // Why only once?
                // Because the previous dp[i, j] value already includes the
                // cost of internal merges needed to reduce the interval to
                // the appropriate number of piles. This final addition is the
                // cost of the last merge that combines those piles into 1.
                // ----------------------------------------------------
                if ((len - 1) % (k - 1) == 0)
                {
                    dp[i, j] += GetRangeSum(prefix, i, j);
                }
            }
        }

        // The answer for the whole array is stored in dp[0, n - 1].
        return dp[0, n - 1];
    }

    // Helper method to get the sum of energy[left..right] in O(1) time.
    private int GetRangeSum(int[] prefix, int left, int right)
    {
        return prefix[right + 1] - prefix[left];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] energy1 = { 3, 2, 4, 1 };
int k1 = 2;
int result1 = solution.MinFatigue(energy1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 20

// Example 2
int[] energy2 = { 3, 2, 4, 1 };
int k2 = 3;
int result2 = solution.MinFatigue(energy2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: -1

// Example 3
int[] energy3 = { 3, 5, 1, 2, 6 };
int k3 = 3;
int result3 = solution.MinFatigue(energy3, k3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 25